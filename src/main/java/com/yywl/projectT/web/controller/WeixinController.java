package com.yywl.projectT.web.controller;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.MD5Util;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bo.PayOrderBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.XmlWeixinResultModel;

@RestController
@RequestMapping("weixin")
public class WeixinController {
	@Autowired
	UserBo userBo;

	@RequestMapping("createOrder")
	public Callable<ResultModel> createOrder(long userId, String token, int totalFee, HttpServletRequest request) {
		return () -> {
			UserDmo user= userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "xml", Charset.forName("UTF-8")));
			headers.add("Accept", "application/xml");
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("xml");
			String appid = Keys.Weixin.APP_PAY_ID;
			String body = "微信充值";
			String mch_id = Keys.Weixin.MCH_ID;
			String nonce_str = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
			String notify_url = Keys.Weixin.CALL;
			String out_trade_no = userId + "a" + System.currentTimeMillis();
			String spbill_create_ip = request.getRemoteAddr();
			String sign_type = "MD5";
			long time = System.currentTimeMillis();
			String time_start = this.dateFormatter.format(new Date(time));
			String total_fee = totalFee+"";
			String trade_type = "APP";
			String stringA = "appid=" + appid + "&body=" + body + "&mch_id=" + mch_id + "&nonce_str=" + nonce_str
					+ "&notify_url=" + notify_url + "&out_trade_no=" + out_trade_no + "&sign_type=" + sign_type
					+ "&spbill_create_ip=" + spbill_create_ip + "&time_start=" + time_start + "&total_fee=" + total_fee
					+ "&trade_type=" + trade_type;
			String stringSignTemp = stringA + "&key=" + Keys.Weixin.APP_PAY_KEY;
			String sign = MD5Util.getSecurityCode(stringSignTemp);
			root.addElement("appid").addText(appid);
			root.addElement("body").addText(body);
			root.addElement("mch_id").addText(mch_id);
			root.addElement("nonce_str").addText(nonce_str);
			root.addElement("notify_url").addText(notify_url);
			root.addElement("out_trade_no").addText(out_trade_no);
			root.addElement("sign").addText(sign);
			root.addElement("spbill_create_ip").addText(spbill_create_ip);
			root.addElement("time_start").addText(time_start);
			root.addElement("total_fee").addText(total_fee);
			root.addElement("trade_type").addText(trade_type);
			root.addElement("sign_type").addText(sign_type);
			HttpEntity<String> formEntity = new HttpEntity<String>(document.asXML(), headers);
			String str = restTemplate.postForObject("https://api.mch.weixin.qq.com/pay/unifiedorder", formEntity,
					String.class);
			String resultString = str;
			Document resultDocument = DocumentHelper.parseText(resultString);
			Element resultRootElement = resultDocument.getRootElement();
			if (!"SUCCESS".equals(resultRootElement.elementText("return_code"))) {
				return new ResultModel(false, "生成订单失败，错误码："+new String(resultRootElement.elementText("return_msg").getBytes("ISO-8859-1"),"UTF-8"), null);
			}
			String timeStamp = time / 1000 + "";
			String prepayId = resultRootElement.elementText("prepay_id");
			String signString = "appid=" + Keys.Weixin.APP_PAY_ID + "&noncestr=" + nonce_str + "&package=Sign=WXPay"
					+ "&partnerid=" + Keys.Weixin.MCH_ID + "&prepayid=" + prepayId + "&timestamp=" + timeStamp + "&key="
					+ Keys.Weixin.APP_PAY_KEY;
			String sign2 = MD5Util.getSecurityCode(signString);
			Map<String, String> resultMap = new HashMap<>();
			resultMap.put("sign", sign2);
			resultMap.put("timeStamp", timeStamp);
			resultMap.put("prepayId", prepayId);
			resultMap.put("nonceStr", nonce_str);
			return new ResultModel(true, "", resultMap);
		};
	}

	private final static Log log = LogFactory.getLog(WeixinController.class);
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss");

	@PostMapping(value = "call")
	public Callable<XmlWeixinResultModel> call(@RequestBody String xmlString) {
		return () -> {
			Document resultDocument = DocumentHelper.parseText(xmlString);
			Element root = resultDocument.getRootElement();
			this.payOrderBo.saveWeiXinAmount(root);
			return new XmlWeixinResultModel(XmlWeixinResultModel.CODE_SUCCESS, "");
		};
	}

	@Autowired
	PayOrderBo payOrderBo;

}
