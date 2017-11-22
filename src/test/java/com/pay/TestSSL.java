package com.pay;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.yywl.projectT.bean.Keys;

public class TestSSL {
	@Test
	public void main() throws KeyStoreException, KeyManagementException, UnrecoverableKeyException, Exception {
		//指定读取证书格式为PKCS12
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		//读取本机存放的PKCS12证书文件
		Resource resource = new ClassPathResource("apiclient_cert.p12");
		InputStream instream=resource.getInputStream();
		try {
		//指定PKCS12的密码(商户ID)
		keyStore.load(instream, Keys.Weixin.MCH_ID.toCharArray());
		} finally {
		instream.close();
		}
		SSLContext sslcontext = SSLContexts.custom()
		.loadKeyMaterial(keyStore, Keys.Weixin.MCH_ID.toCharArray()).build();
		//指定TLS版本
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		sslcontext,new String[] { "TLSv1" },null,
		SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		//设置httpclient的SSLSocketFactory
		CloseableHttpClient httpclient = HttpClients.custom()
		.setSSLSocketFactory(sslsf)
		.build();
		
	}
}
