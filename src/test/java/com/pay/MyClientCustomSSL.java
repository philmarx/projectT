/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.pay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.MD5Util;

/**
 * This example demonstrates how to create secure connections with a custom SSL
 * context.
 */
public class MyClientCustomSSL {

	@Test
	public void main() throws Exception {
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
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
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
			root.addElement("out_trade_no").addText("10000000004a1497322581110");
			root.addElement("refund_fee").addText("1");
			root.addElement("total_fee").addText("2");
			String signString = "appid=" + Keys.Weixin.APP_PAY_ID + "&mch_id=" + Keys.Weixin.MCH_ID + "&nonce_str="
					+ uuid + "&out_refund_no=" + root.elementText("out_refund_no") + "&out_trade_no="
					+ root.elementText("out_trade_no") + "&refund_fee=" + root.elementText("refund_fee") + "&total_fee="
					+ root.elementText("total_fee") + "&key=" + Keys.Weixin.APP_PAY_KEY;
			String sign=MD5Util.getSecurityCode(signString);
			root.addElement("sign").addText(sign);
			String params = document.asXML();
			httppost.setEntity(new StringEntity(params));
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
					String text;
					StringBuilder sb = new StringBuilder();
					while ((text = bufferedReader.readLine()) != null) {
						sb.append(text);
						System.out.println(text);
					}
					// System.out.println(sb.toString());
				}
				EntityUtils.consume(entity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

}
