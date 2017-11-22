package com.yywl.projectT.web.controller;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import com.yywl.projectT.bean.enums.PropType;
import com.yywl.projectT.bo.PropBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.ApplicationDao;
import com.yywl.projectT.dao.PropOrderDao;
import com.yywl.projectT.dao.PropTypeDao;
import com.yywl.projectT.dao.SeptActivityHelpDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.ApplicationDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.PropOrderDmo;
import com.yywl.projectT.dmo.PropTypeDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.PropVo;
import com.yywl.projectT.vo.XmlWeixinResultModel;

@RequestMapping("prop")
@RestController
public class PropController {
	@Autowired
	PropBo propBo;
	@Autowired
	UserBo userBo;

	private static final Log log = LogFactory.getLog(PropController.class);

	@PostMapping("findProp")
	public Callable<ResultModel> findProp(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			PropDmo prop = this.propBo.findByUser_Id(userId);
			if (prop == null) {
				return new ResultModel(true, "", new PropVo());
			}
			PropVo propVo = new PropVo(prop.getNoteCount(), prop.getLabelClearCount(), prop.getChangeNicknameCount(),
					prop.getSignCount(), prop.getVipExpireDate());
			propVo.setMovieTicket(prop.getRemainMovieTicket());
			int helpers=this.septActivityHelpDao.countByHelper_Id(userId);
			int needRecommends=30-(helpers%30);
			needRecommends=needRecommends==0?30:needRecommends;
			propVo.setNeedRecommends(needRecommends);
			propVo.setFriendCard(prop.getFriendCard());
			propVo.setRoomTicket(prop.getRoomTicket());
			return new ResultModel(true, "", propVo);
		};
	}

	@Autowired
	SeptActivityHelpDao septActivityHelpDao; 
	
	@PostMapping("buyProp")
	public Callable<ResultModel> buyProp(long userId, String token, int type, int count) {
		return () -> {
			UserDmo user=this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			if (type == PropType.note.ordinal()) {
				this.propBo.buyNote(user, count);
			} else if (type == PropType.clearLabelCard.ordinal()) {
				this.propBo.buyLabelClearCard(user, count);
			} else if (type == PropType.signAgainCard.ordinal()) {
				this.propBo.buySignAgainCard(user, count);
			} else if (type == PropType.changeNameCard.ordinal()) {
				this.propBo.buyChangeNameCard(user, count);
			} else if (type == PropType.vip1.ordinal()) {
				this.propBo.buyVipOneMonth(user, count);
			} else if (type == PropType.vip3.ordinal()) {
				this.propBo.buyVipThreeMonth(user, count);
			} else if (type == PropType.vip12.ordinal()) {
				this.propBo.buyVipOneYear(user, count);
			} else {
				log.error("道具类型不正确");
				throw new Exception("道具类型不正确");
			}
			return new ResultModel();
		};
	}

	@PostMapping("alipay/createOrder")
	public Callable<ResultModel> alipayCreateOrder(long userId, String token, int propType, int count) {
		return () -> {
			UserDmo user= this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			Map<String, Object> map = this.getPropAlipayAmount(propType, count);
			double amount = Double.valueOf(map.get("totalAmount") + "");
			amount = amount / 100.0;
			String tmp = amount + "";
			tmp = String.format("%.2f", Float.valueOf(tmp));
			ResultModel resultModel = new ResultModel();
			// 实例化客户端
			AlipayClient alipayClient = new DefaultAlipayClient("http://openapi.alipay.com/gateway.do",
					Keys.Alipay.APP_ID, Keys.Alipay.APP_PRIVATE_KEY, Keys.Alipay.FORMAT, Keys.Alipay.CHARSET,
					Keys.Alipay.APP_PUBLIC_KEY, "RSA2");
			// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
			AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
			// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
			AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
			model.setBody(propType + "-" + count);
			model.setSubject(map.get("subject") + "");
			model.setOutTradeNo(userId + "a" + System.currentTimeMillis());
			PropOrderDmo propOrderDmo = new PropOrderDmo(null, model.getOutTradeNo(), propType, count, userId, new Date(), "支付宝");
			this.propOrderDao.save(propOrderDmo);
			model.setTimeoutExpress("30m");
			model.setTotalAmount(tmp);
			model.setProductCode("QUICK_MSECURITY_PAY");
			request.setBizModel(model);
			request.setNotifyUrl(Alipay.BUY_PROP_CALL);
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

	@Autowired
	PropOrderDao propOrderDao;

	private Map<String, Object> getPropAlipayAmount(int type, int count) throws Exception {
		if (count < 1) {
			throw new Exception("购买数量不能小于1");
		}
		int totalAmount = -1;
		String subject = null;
		PropTypeDmo typeDmo=this.propTypeDao.findByUniqueId(type);
		if (typeDmo.getMoney()<=0) {
			throw new Exception("请用叶子购买");
		}
		if (type == PropType.note.ordinal()) {
			totalAmount = count * typeDmo.getMoney();
			subject = "购买小纸条" + count + "个";
		} else if (type == PropType.clearLabelCard.ordinal()) {
			totalAmount = count * typeDmo.getMoney();
			subject = "购买标签消除卡" + count + "个";
		} else if (type == PropType.signAgainCard.ordinal()) {
			totalAmount = count * typeDmo.getMoney();
			subject = "购买补签卡" + count + "个";
		} else if (type == PropType.changeNameCard.ordinal()) {
			totalAmount = count * typeDmo.getMoney();
			subject = "购买改名卡" + count + "个";
		} else if (type == PropType.vip1.ordinal()) {
			totalAmount = count * typeDmo.getMoney();
			subject = "购买vip会员一个月" + count + "次";
		} else if (type == PropType.vip3.ordinal()) {
			totalAmount = count * typeDmo.getMoney();
			subject = "购买vip会员3个月" + count + "次";
		} else if (type == PropType.vip12.ordinal()) {
			totalAmount = count * typeDmo.getMoney();
			subject = "购买vip会员1年" + count + "次";
		} else {
			log.error("道具类型不正确");
			throw new Exception("道具类型不正确");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("totalAmount", totalAmount);
		map.put("subject", subject);
		return map;
	}

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
			String outTradeNo = params.get("out_trade_no");
			this.propBo.buyProp(outTradeNo);
			return new ResultModel(flag);
		};
	}

	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss");

	@RequestMapping("weixin/createOrder")
	public Callable<ResultModel> weixinCreateOrder(long userId, String token, int propType, int count,
			HttpServletRequest request) {
		return () -> {
			UserDmo user= userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			Map<String, Object> map = this.getPropAlipayAmount(propType, count);
			RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "xml", Charset.forName("UTF-8")));
			headers.add("Accept", "application/xml");
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("xml");
			String appid = Weixin.APP_PAY_ID;
			String body = map.get("subject")+"";
			String mch_id = Weixin.MCH_ID;
			String nonce_str = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
			String notify_url = Weixin.BUY_PROP_CALL;
			String out_trade_no = userId + "a" + System.currentTimeMillis();
			String spbill_create_ip = request.getRemoteAddr();
			String sign_type = "MD5";
			long time = System.currentTimeMillis();
			String time_start = this.dateFormatter.format(new Date(time));
			String total_fee = map.get("totalAmount") + "";
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
				return new ResultModel(false, "生成订单失败，错误码："
						+ new String(resultRootElement.elementText("return_msg").getBytes("ISO-8859-1"), "UTF-8"),
						null);
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
			PropOrderDmo propOrderDmo = new PropOrderDmo(null, out_trade_no, propType, count, userId, new Date(), "微信");
			this.propOrderDao.save(propOrderDmo);
			return new ResultModel(true, "", resultMap);
		};
	}

	@PostMapping(value = "weixin/call")
	public Callable<XmlWeixinResultModel> weixinCall(@RequestBody String xmlString) {
		return () -> {
			Document resultDocument = DocumentHelper.parseText(xmlString);
			Element root = resultDocument.getRootElement();
			String outTradeNo = root.elementText("out_trade_no");
			this.propBo.buyProp(outTradeNo);
			return new XmlWeixinResultModel(XmlWeixinResultModel.CODE_SUCCESS, "");
		};
	}

	@Autowired
	PropTypeDao propTypeDao;
	
	@PostMapping("findPropTypes")
	public Callable<ResultModel> findPropTypes(){
		return ()->{
			ApplicationDmo applicationDmo=this.applicationDao.findByPlatform("prop");
			String msg="";
			if (applicationDmo!=null) {
				msg=applicationDmo.getVersion();
			}
			return new ResultModel(true, msg , this.propTypeDao.findAll());
		};
	}

	@Autowired
	ApplicationDao applicationDao;
	
	@Autowired
	UserDao userDao;
	
	@PostMapping("useFriendCard")
	public Callable<ResultModel> useFriendCard(long userId,String token,long roomId,int count){
		return ()->{
			UserDmo user=this.userBo.loginByToken(userId, token);
			this.propBo.useFriendCard(user,roomId,count);
			return new ResultModel();
		};
	}
}
