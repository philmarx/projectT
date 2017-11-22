package com.yywl.projectT.web.controller;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.Keys.Alipay;
import com.yywl.projectT.bean.Keys.Weixin;
import com.yywl.projectT.bean.MD5Util;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bo.BadgeBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.BadgeDetailsDao;
import com.yywl.projectT.dmo.BadgeDetailsDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.BadgeVo;
import com.yywl.projectT.vo.XmlWeixinResultModel;
import com.yywl.projectT.vo.BadgeVo.UserVo;

@RestController
@RequestMapping("badge")
public class BadgeController {
	@Autowired
	private BadgeDetailsDao badgeDetailsDao;

	@PostMapping("findBadgeDetails")
	public Callable<ResultModel> findBadgeDetails(int page, int size, long userId, String token) {
		return () -> {
			List<BadgeVo> list=new LinkedList<>();
			Page<BadgeDetailsDmo> p = this.badgeDetailsDao.findByUser_Id(userId,
					new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC, "createTime"));
			for (BadgeDetailsDmo dmo : p.getContent()) {
				UserVo user=new UserVo(dmo.getUser().getId(), dmo.getUser().getNickname());
				BadgeVo badgeVo=new BadgeVo(dmo.getId(), user, dmo.getDescription(), dmo.getCreateTime(),dmo.getBadge());
				list.add(badgeVo);
			}
			return new ResultModel(true,"",list);
		};
	}

	@Autowired
	UserBo userBo;
	
	private static final Log log=LogFactory.getLog(BadgeController.class);
	/**
	 * 生成订单
	 * 
	 * @param body
	 * @param subject
	 * @param totalAmount
	 * @return
	 */
	@PostMapping("alipay/createOrder")
	public Callable<ResultModel> alipayCreateOrder(long userId, String token, int totalAmount) {
		return () -> {
			UserDmo user= this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			int badge=this.getBadgeFromMoney(totalAmount); 
			if (badge==0) {
				log.error("充值金额不正确");
				return new ResultModel(false, "充值金额不正确", null);
			}
			double amount=totalAmount;
			amount=amount/100.0;
			String tmp=amount+"";
			tmp=String.format("%.2f", Float.valueOf(tmp));
			ResultModel resultModel = new ResultModel();
			// 实例化客户端
			AlipayClient alipayClient = new DefaultAlipayClient("http://openapi.alipay.com/gateway.do",
					Keys.Alipay.APP_ID, Keys.Alipay.APP_PRIVATE_KEY, Keys.Alipay.FORMAT, Keys.Alipay.CHARSET,
					Keys.Alipay.APP_PUBLIC_KEY, "RSA2");
			// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
			AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
			// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
			AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
			model.setBody("购买"+badge+"个叶子");
			model.setSubject("购买"+badge+"个叶子");
			model.setOutTradeNo(userId + "a" + System.currentTimeMillis());
			model.setTimeoutExpress("30m");
			model.setTotalAmount(tmp);
			model.setProductCode("QUICK_MSECURITY_PAY");
			request.setBizModel(model);
			request.setNotifyUrl(Alipay.BUY_BADGE_CALL);
			try {
				// 这里和普通的接口调用不同，使用的是sdkExecute
				AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
				resultModel.setData(response.getBody());
				resultModel.setSuccess(response.isSuccess());
			} catch (AlipayApiException e) {
				resultModel.setMsg(e.getMessage());
				resultModel.setSuccess(false);
			}
			return resultModel;
		};
	}
	
	/**
	 * 回调
	 */
	@PostMapping("alipay/call")
	public Callable<ResultModel> alipayCall(HttpServletRequest request) {
		return () -> {
			// 获取支付宝POST过来反馈信息
			Map<String, String> params = new HashMap<String, String>();
			Map<String, String[]> requestParams = request.getParameterMap();
			for (Iterator<?> iter = requestParams.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
				}
				params.put(name, valueStr);
			}
			boolean flag = AlipaySignature.rsaCheckV1(params, Keys.Alipay.ALIPAY_PUBLIC_KEY, "utf-8", "RSA2");
			if (!flag) {
				return new ResultModel(false);
			}
			String outTradeNo=params.get("out_trade_no");
			Long userId=Long.valueOf(outTradeNo.split("a")[0]);
			int badge=0;
			try {
				String total_money =params.get("total_amount");
				if (total_money.contains(".")) {
					total_money=total_money.split("\\.")[0];
				}
				int money = Integer.valueOf(total_money)*100 ;
				badge = this.getBadgeFromMoney(money);
			} catch (NumberFormatException e) {
				log.error(e.getMessage());
				return new ResultModel(false,"数字转换出错",null);
			}
			String tradeNo=params.get("trade_no");
			int spendMoney=0;
			try {
				spendMoney = (int) Math.floor(Double.valueOf(params.get("total_amount")) * 100);
			} catch (NumberFormatException e) {
				log.error(e.getMessage());
				return new ResultModel(false);
			}
			this.badgeBo.buy(userId,badge,outTradeNo,tradeNo,spendMoney,"支付宝");
			return new ResultModel(flag);
		};
	}
	@Autowired
	BadgeBo badgeBo;
	
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss");
	
	private int getBadgeFromMoney(int money) throws Exception{
		int badge=0; 
		switch (money) {
		case 600:
			badge=6;
			break;
		case 1800:
			badge=18;
			break;
		case 3800:
			badge=38;
			break;
		case 6800:
			badge=68;
			break;
		case 12800:
			badge=150;
			break;
		case 38800:
			badge=500;
			break;
		default:
			log.error("充值金额不正确");
			throw new Exception("充值金额不正确");
		}
		return badge;
	}
	
	@RequestMapping("weixin/createOrder")
	public Callable<ResultModel> weixinCreateOrder(long userId, String token, int totalFee, HttpServletRequest request) {
		return () -> {
			UserDmo user= userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			int badge=this.getBadgeFromMoney(totalFee);
			if (badge==0) {
				log.error("充值金额不正确");
				return new ResultModel(false, "充值金额不正确", null);
			}
			RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "xml", Charset.forName("UTF-8")));
			headers.add("Accept", "application/xml");
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("xml");
			String appid = Weixin.APP_PAY_ID;
			String body = "购买"+badge+"片叶子";
			String mch_id = Weixin.MCH_ID;
			String nonce_str = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
			String notify_url = Weixin.BUY_BADGE_CALL;
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
	
	@PostMapping(value = "weixin/call")
	public Callable<XmlWeixinResultModel> weixinCall(@RequestBody String xmlString) {
		return () -> {
			log.info(xmlString);
			Document resultDocument = DocumentHelper.parseText(xmlString);
			Element root = resultDocument.getRootElement();
			String outTradeNo = root.elementText("out_trade_no");
			long userId = Long.valueOf(outTradeNo.split("a")[0]);
			String transactionId = root.elementText("transaction_id");
			int badge=0;
			int spendMoney=0;
			try {
				spendMoney = Integer.valueOf(root.elementText("total_fee"));
				badge=this.getBadgeFromMoney(spendMoney);
			} catch (NumberFormatException e) {
				log.error(e.getMessage());
				return new XmlWeixinResultModel(XmlWeixinResultModel.CODE_FAIL, "");
			}
			this.badgeBo.buy(userId, badge, outTradeNo, transactionId,spendMoney, "微信");
			return new XmlWeixinResultModel(XmlWeixinResultModel.CODE_SUCCESS, "");
		};
	}
	
}
