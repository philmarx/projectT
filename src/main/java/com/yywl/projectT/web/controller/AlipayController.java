package com.yywl.projectT.web.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.MD5Util;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bo.PayOrderBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.PayOrderDao;
import com.yywl.projectT.dmo.PayOrderDmo;
import com.yywl.projectT.dmo.UserDmo;

@SuppressWarnings("deprecation")
@RestController
@RequestMapping("alipay")
public class AlipayController {

	@Autowired
	UserBo userBo;

	/**
	 * 参考文档https://docs.open.alipay.com/api_1/alipay.trade.fastpay.refund.query
	 * 
	 * @param id
	 * @param userId
	 * @param token
	 * @param refundAmount
	 * @return
	 */
	@PostMapping("refund")
	public Callable<ResultModel> refund(long id, long userId, String token, int refundAmount) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			int amount = Integer.valueOf(refundAmount);
			if (user.getAmount().intValue() < amount) {
				log.error("余额不足");
				throw new Exception("余额不足");
			}
			PayOrderDmo order = this.payOrderDao.findOne(id);
			if (order == null) {
				log.error("订单不存在");
				throw new Exception("订单不存在");
			}
			if (order.getTotalAmount() < (order.getRefundAmount() + refundAmount)) {
				throw new Exception("退款金额超过可退金额");
			}
			if ((System.currentTimeMillis() - order.getNotifyTime().getTime()) > 180 * 24 * 3600 * 1000L) {
				throw new Exception("半年前的订单无法退款");
			}
			String outTradeNo = order.getOutTradeNo();
			String tradeNo = order.getTradeNo();
			// 支付宝退款
			if (Keys.ALIPAY_TYPE.equals(order.getType())) {
				// 将分改为元
				String tmp = amount / 100.0 + "";
				tmp = String.format("%.2f", Float.valueOf(tmp));
				AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
						Keys.Alipay.APP_ID, Keys.Alipay.APP_PRIVATE_KEY, "json", "utf8", Keys.Alipay.ALIPAY_PUBLIC_KEY,
						"RSA2");
				String outTradeNo2 = userId + "b" + System.currentTimeMillis();
				AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
				request.setBizContent("{" + "\"out_trade_no\":\"" + outTradeNo2 + "\"," + "\"trade_no\":\"" + tradeNo
						+ "\"," + "\"refund_amount\":" + tmp + "," + "\"refund_reason\":\"正常退款\","
						+ "\"out_request_no\":\"HZ01RF001\"," + "\"operator_id\":\"OP001\","
						+ "\"store_id\":\"NJ_S_001\"," + "\"terminal_id\":\"NJ_T_001\"" + ",\"out_request_no\":\""
						+ userId + System.currentTimeMillis() + new Random(System.currentTimeMillis()).nextInt()
						+ "\"  }");
				AlipayTradeRefundResponse response = alipayClient.execute(request);
				if (response.isSuccess()&&"10000".equals(response.getCode())) {
					this.payOrderBo.refundAlipay(id, outTradeNo, tradeNo, refundAmount, outTradeNo2);
					return new ResultModel(true);
				} else {
					if ("ACQ.TRADE_HAS_FINISHED".equals(response.getSubCode())) {
						this.payOrderBo.refundAlipay(id, outTradeNo, tradeNo, refundAmount, outTradeNo2);
						return new ResultModel();
					}
					/*AlipayClient alipayClient2 = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
							Keys.Alipay.APP_ID, Keys.Alipay.APP_PRIVATE_KEY, "json", "utf8",
							Keys.Alipay.ALIPAY_PUBLIC_KEY, "RSA2");
					AlipayTradeFastpayRefundQueryRequest request2 = new AlipayTradeFastpayRefundQueryRequest();
					request2.setBizContent("{" + "\"trade_no\":\"" + order.getTradeNo() + "\"," + "\"out_trade_no\":\""
							+ order.getOutTradeNo() + "\"," + "\"out_request_no\":\"" + userId
							+ System.currentTimeMillis() + new Random(System.currentTimeMillis()).nextInt() + "\""
							+ "  }");
					AlipayTradeFastpayRefundQueryResponse response2 = alipayClient2.execute(request2);
					log.info(response2.isSuccess()+"\n"+response2.getBody()+"\n"+response2.getRefundAmount());
					if (response2.isSuccess()&&"10000".equals(response2.getCode())) {
						this.payOrderBo.refundAlipay(id, outTradeNo, tradeNo, refundAmount, outTradeNo2);
						return new ResultModel();*/
					log.error("退款失败，错误码：" + response.getBody());
					return new ResultModel(false, "退款失败，请重试或联系客服。", null);
				}
				// 微信退款
			} else if (Keys.WEIXIN_TYPE.equals(order.getType())) {
				StringBuilder sb = new StringBuilder();
				KeyStore keyStore = KeyStore.getInstance("PKCS12");
				Resource resource = new ClassPathResource("apiclient_cert.p12");
				InputStream instream = resource.getInputStream();
				try {
					keyStore.load(instream, Keys.Weixin.MCH_ID.toCharArray());
				} finally {
					instream.close();
				}

				// Trust own CA and all self-signed certs
				SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, Keys.Weixin.MCH_ID.toCharArray())
						.build();
				// Allow TLSv1 protocol only
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" },
						null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
				CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
				try {

					HttpPost httppost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
					httppost.setHeader("Content-Type", "text/xml");
					httppost.setHeader("charset", "utf-8");
					Document document = DocumentHelper.createDocument();
					Element root = document.addElement("xml");
					root.addElement("appid").addText(Keys.Weixin.APP_PAY_ID);
					root.addElement("mch_id").addText(Keys.Weixin.MCH_ID);
					String uuid = UUID.randomUUID().toString().replaceAll("-", "");
					root.addElement("nonce_str").addText(uuid);
					String out_refund_no = UUID.randomUUID().toString().replaceAll("-", "");
					root.addElement("out_refund_no").addText(out_refund_no);
					root.addElement("out_trade_no").addText(order.getOutTradeNo());
					root.addElement("refund_fee").addText(refundAmount + "");
					root.addElement("total_fee").addText(order.getTotalAmount() + "");
					String signString = "appid=" + Keys.Weixin.APP_PAY_ID + "&mch_id=" + Keys.Weixin.MCH_ID
							+ "&nonce_str=" + uuid + "&out_refund_no=" + root.elementText("out_refund_no")
							+ "&out_trade_no=" + root.elementText("out_trade_no") + "&refund_fee="
							+ root.elementText("refund_fee") + "&total_fee=" + root.elementText("total_fee") + "&key="
							+ Keys.Weixin.APP_PAY_KEY;
					String sign = MD5Util.getSecurityCode(signString);
					root.addElement("sign").addText(sign);
					String params = document.asXML();
					httppost.setEntity(new StringEntity(params));
					CloseableHttpResponse response = httpclient.execute(httppost);
					try {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							BufferedReader bufferedReader = new BufferedReader(
									new InputStreamReader(entity.getContent()));
							String text;
							while ((text = bufferedReader.readLine()) != null) {
								sb.append(text);
							}
						}
						EntityUtils.consume(entity);
					} finally {
						response.close();
					}
				} finally {
					httpclient.close();
				}

				Document doc = DocumentHelper.parseText(sb.toString());
				Element root = doc.getRootElement();
				if (!"SUCCESS".equals(root.elementText("return_code"))) {
					return new ResultModel(false, "退款失败", root.elementText("return_msg"));
				}
				if (!StringUtils.isEmpty(root.elementText("err_code"))) {
					throw new Exception(root.elementText("err_code") + ":" + root.elementText("err_code_des"));
				}
				this.payOrderBo.refundWeixin(id, user, root);
				return new ResultModel(true);
			} else {
				throw new Exception("订单类型不正确");
			}
		};
	}

	@PostMapping("findOrders")
	public Callable<ResultModel> findOrders(String token, long userId, int page, int size) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			String out_trade_no = userId + "a%";
			Pageable pageable = new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC,
					"notifyTime");
			Page<PayOrderDmo> p = this.payOrderDao.findByOutTradeNoLikeAndTotalAmountNot(out_trade_no, 0, pageable);
			LinkedList<Map<String, Object>> list = new LinkedList<>();
			for (PayOrderDmo o : p.getContent()) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", o.getId());
				map.put("amount", o.getTotalAmount());
				map.put("time", o.getNotifyTime());
				map.put("trade_no", o.getTradeNo());
				map.put("out_trade_no", o.getOutTradeNo());
				map.put("refundAmount", o.getRefundAmount());
				map.put("type", o.getType());
				list.add(map);
			}
			return new ResultModel(true, "", list);
		};
	}

	/**
	 * 生成订单
	 * 
	 * @param body
	 * @param subject
	 * @param totalAmount
	 * @return
	 */
	@PostMapping("createOrder")
	public Callable<ResultModel> createOrder(long userId, String token, int totalAmount) {
		return () -> {
			// 将金额的单位改为元
			double amount = totalAmount;
			amount = amount / 100.0;
			String tmp = amount + "";
			tmp = String.format("%.2f", Float.valueOf(tmp));
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			ResultModel resultModel = new ResultModel();
			// 实例化客户端
			AlipayClient alipayClient = new DefaultAlipayClient("http://openapi.alipay.com/gateway.do",
					Keys.Alipay.APP_ID, Keys.Alipay.APP_PRIVATE_KEY, Keys.Alipay.FORMAT, Keys.Alipay.CHARSET,
					Keys.Alipay.APP_PUBLIC_KEY, "RSA2");
			// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
			AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
			// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
			AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
			model.setBody("支付宝充值");
			model.setSubject("保证金充值" + tmp + "元");
			model.setOutTradeNo(userId + "a" + System.currentTimeMillis());
			model.setTimeoutExpress("30m");
			model.setTotalAmount(tmp);
			model.setProductCode("QUICK_MSECURITY_PAY");
			request.setBizModel(model);
			request.setNotifyUrl(Keys.Alipay.CALL);
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

	@RequestMapping("gateway")
	public Callable<Object> gateway(@RequestBody String params) {
		return () -> {
			log.info("调用gateway");
			log.info(params);
			File file = new File("gateway.log");
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.append(Formatter.dateTimeFormatter.format(new Date()));
			writer.newLine();
			writer.append(params);
			writer.newLine();
			writer.close();
			return new ResultModel();
		};
	}

	@Autowired
	PayOrderDao payOrderDao;

	/**
	 * 回调
	 */
	@PostMapping("call")
	public Callable<ResultModel> call(HttpServletRequest request) {
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
				// 乱码解决，这段代码在出现乱码时使用。
				// valueStr = new String(valueStr.getBytes("ISO-8859-1"),
				// "utf-8");
				params.put(name, valueStr);
			}
			// 切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
			// boolean AlipaySignature.rsaCheckV1(Map<String, String> params,
			// String publicKey, String charset, String sign_type)
			boolean flag = AlipaySignature.rsaCheckV1(params, Keys.Alipay.ALIPAY_PUBLIC_KEY, "utf-8", "RSA2");
			if (!flag) {
				return new ResultModel(false);
			}
			this.payOrderBo.saveAlipayAmount(params);
			return new ResultModel(flag);
		};
	}

	private static final Log log = LogFactory.getLog(AlipayController.class);

	@Autowired
	PayOrderBo payOrderBo;

}
