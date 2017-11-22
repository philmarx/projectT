package com.pay;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.UUID;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.MD5Util;

public class TestWeiXinPay {
	@Test
	public void createOrder() throws Exception {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "xml", Charset.forName("UTF-8")));
		headers.add("Accept", "application/xml");
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("xml");
		String appid = Keys.Weixin.APP_PAY_ID;
		String body = "微信充值";
		String mch_id = Keys.Weixin.MCH_ID;
		String nonce_str =  UUID.randomUUID().toString().replaceAll("-", "");;
		String notify_url = "http://localhost:8080";
		String out_trade_no = "a"+System.currentTimeMillis();;
		String spbill_create_ip = "192.168.0.195";
		String total_fee = "100";
		String trade_type = "APP";
		String stringA = "appid=" + appid + "&body=" + body + "&mch_id=" + mch_id + "&nonce_str=" + nonce_str
				+ "&notify_url=" + notify_url + "&out_trade_no=" + out_trade_no + "&spbill_create_ip="
				+ spbill_create_ip + "&total_fee=" + total_fee+"&trade_type="+trade_type;
		String stringSignTemp=stringA+"&key="+Keys.Weixin.APP_PAY_KEY;
		String sign=MD5Util.getSecurityCode(stringSignTemp);
		root.addElement("appid").addText(appid);
		root.addElement("body").addText(body);
		root.addElement("mch_id").addText(mch_id);
		root.addElement("nonce_str").addText(nonce_str);
		root.addElement("notify_url").addText(notify_url);
		root.addElement("out_trade_no").addText(out_trade_no);
		root.addElement("sign").addText(sign);
		root.addElement("spbill_create_ip").addText(spbill_create_ip);
		root.addElement("total_fee").addText(total_fee);
		root.addElement("trade_type").addText(trade_type);
		HttpEntity<String> formEntity = new HttpEntity<String>(document.asXML(), headers);
		String str = restTemplate.postForObject("https://api.mch.weixin.qq.com/pay/unifiedorder", formEntity,
				String.class);
		String resultString=new String(str.getBytes("iso8859-1"), "UTF-8");
		//System.out.println(resultString);
		Document resultDocument= DocumentHelper.parseText(resultString);
		Element root2=resultDocument.getRootElement();
		System.out.println(root2.elementText("mch_id"));
		/*@SuppressWarnings("unchecked")
		Iterator<Element> rs= resultRootElement.elementIterator();
		while(rs.hasNext()){
			Element e= rs.next();
			System.out.println(e.getName()+":"+e.getText());
		}*/
	}

	@Test
	public void testFindCharset() {
		System.out.println(Charset.isSupported("UTF-8"));
	}

	@Test
	public void testMediaType() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		MediaType mediaType = MediaType.APPLICATION_JSON_UTF8;
		Class<?> c = MediaType.class;
		Method m = c.getMethod("getSubtype");
		System.out.println(m.invoke(mediaType));
		m = c.getMethod("getType");
		System.out.println(m.invoke(mediaType));
		// System.out.println(new MediaType(MediaType.APPLICATION_XML_VALUE,
		// null, Charset.forName("UTF-8")));
	}

	@Test
	public void testDom4j() {
		Document document = DocumentHelper.createDocument();
		// 创建根节点
		Element root = document.addElement("xml");
		root.addElement("appid", "dsfsdf");
		root.addElement("a").addText("b");
		System.out.println(document.asXML());
	}
}
