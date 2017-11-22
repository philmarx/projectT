package com.yywl.projectT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.yywl.projectT.bean.DistanceConverter;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.Keys;

public class TestVithoutSpring {

	@Test
	public void location() {
		// 房间经纬度
		double lon0 = 120.075389, lat0 = 30.316934;
		// 成员经纬度
		double lon = 120.072692, lat = 30.314167;
		double distance = DistanceConverter.getDistance(lon0, lat0, lon, lat);
		double x = lon - lon0, y = lat - lat0;
		double angle = Math.toDegrees(FastMath.atan2(x, y));
		System.out.println("(距离:" + distance + "米,角度:" + angle + ")");

	}

	@Test
	public void alipay() throws AlipayApiException {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", Keys.Alipay.APP_ID,
				Keys.Alipay.APP_PRIVATE_KEY, "json", "UTF-8", Keys.Alipay.ALIPAY_PUBLIC_KEY, "RSA2");
		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
		request.setBizContent("{" + "\"out_biz_no\":\"3142321423432\"," + "\"payee_type\":\"ALIPAY_LOGONID\","
				+ "\"payee_account\":\"17702525841\"," + "\"amount\":\"0.1\"," + "\"payer_show_name\":\"后会有期\","
				+ "\"payee_real_name\":\"马健原\"," + "\"remark\":\"后会有期app提现\"" + "  }");
		AlipayFundTransToaccountTransferResponse response = alipayClient.execute(request);
		if (response.isSuccess()) {
			System.out.println("调用成功");
		} else {
			System.out.println("调用失败");
			System.out.println(response.getCode());
			System.out.println(response.getSubCode());
			System.out.println(response.getSubMsg());
		}
	}

	@Test
	public void wxRoomPhoto1() throws Exception {
		RestTemplate rest = new RestTemplate(new SimpleClientHttpRequestFactory());
		rest.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		StringBuilder url = new StringBuilder();
		url.append("https://api.weixin.qq.com/cgi-bin/token?");
		url.append("appid=" + Keys.Weixin.LITTLE_APP_ID + "&secret=" + Keys.Weixin.LITTLE_APP_SECRET);
		url.append("&grant_type=client_credential");
		String resultStr = rest.getForObject(url.toString(), String.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> result = Formatter.gson.fromJson(resultStr, Map.class);
		String accessToken = (String) result.get("access_token");
		url = new StringBuilder();
		url.append("https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=" + accessToken);
		Map<String, String> params = new HashMap<>();
		params.put("path", "pages/room/room?roomId=1000000000855");
		String paramsBody = Formatter.gson.toJson(params);
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsBody, headers);
		byte[] bytes = rest.postForObject(url.toString(), httpEntity, byte[].class);
		File file = new File("e:/img.jpg");
		if (!file.exists()) {
			file.createNewFile();
		}
		InputStream inputStream = new ByteArrayInputStream(bytes);
		OutputStream outputStream = new FileOutputStream(file);
		int len = 0;
		byte[] buf = new byte[1024];
		while ((len = inputStream.read(buf, 0, 1024)) != -1) {
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		inputStream.close();
		outputStream.close();
	}

	@Test
	public void wxRoomPhoto2() throws Exception {
		RestTemplate rest = new RestTemplate(new SimpleClientHttpRequestFactory());
		rest.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		StringBuilder url = new StringBuilder();
		url.append("https://api.weixin.qq.com/cgi-bin/token?");
		url.append("appid=" + Keys.Weixin.LITTLE_APP_ID + "&secret=" + Keys.Weixin.LITTLE_APP_SECRET);
		url.append("&grant_type=client_credential");
		String resultStr = rest.getForObject(url.toString(), String.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> result = Formatter.gson.fromJson(resultStr, Map.class);
		String accessToken = (String) result.get("access_token");
		url = new StringBuilder();
		url.append("https://api.weixin.qq.com/wxa/getwxacode?access_token=" + accessToken);
		Map<String, String> params = new HashMap<>();
		params.put("path", "pages/roomturn/roomturn?roomId=" + 1000000000862L);
		String paramsBody = Formatter.gson.toJson(params);
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsBody, headers);
		byte[] bytes = rest.postForObject(url.toString(), httpEntity, byte[].class);
		File file = new File("e:/room.jpg");
		if (!file.exists()) {
			file.createNewFile();
		}
		InputStream inputStream = new ByteArrayInputStream(bytes);
		OutputStream outputStream = new FileOutputStream(file);
		int len = 0;
		byte[] buf = new byte[1024];
		while ((len = inputStream.read(buf, 0, 1024)) != -1) {
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		inputStream.close();
		outputStream.close();
	}

	@Test
	public void wxUserPhoto() throws Exception {
		RestTemplate rest = new RestTemplate();
		byte[] bytes = rest.getForObject(
				"https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83eovdXXqyDLg4H36HoBPKC4SQfOzOEiaiaMI0pK4L8J60DannE9XbYRjWMmm8WlgzSsxXh1vI5Xo5ufg/0",
				 byte[].class);
		File file = new File("e:/user.jpg");
		if (!file.exists()) {
			file.createNewFile();
		}
		InputStream inputStream = new ByteArrayInputStream(bytes);
		OutputStream outputStream = new FileOutputStream(file);
		int len = 0;
		byte[] buf = new byte[1024];
		while ((len = inputStream.read(buf, 0, 1024)) != -1) {
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		inputStream.close();
		outputStream.close();
	}
}
