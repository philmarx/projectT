package com.yywl.projectT.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.yywl.projectT.ProjectTApplication;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.dmo.ApplicationDmo;

@SpringBootTest(classes = ProjectTApplication.class)
@RunWith(SpringRunner.class)
public class TestRestTemplate {

	@Test
	public void getCurrentVersion() {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		String version = restTemplate.getForObject("http://localhost/application/versions/current", String.class);
		System.out.println(version);
	}

	@Test
	public void getVersions() {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		List<Object> list = restTemplate.getForObject("http://localhost/application/versions", List.class);
		for (Object o : list) {
			ApplicationDmo a = gson.fromJson(o.toString(), ApplicationDmo.class);
			System.out.println(a.getVersion());
		}
	}

	@Test
	public void postLogin() throws JSONException {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<String> formEntity = new HttpEntity<String>(headers);
		ResultModel rm = restTemplate.postForObject("http://localhost/user/login?phone={phone}&password={password}",
				formEntity, ResultModel.class, "17702525841", "000000");
		System.out.println(rm.getData());
	}

	@Test
	public void postLogin2() throws JSONException {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		JSONObject json = new JSONObject();
		json.put("phone", "17702525841");
		json.put("password", "000000");
		HttpEntity<String> formEntity = new HttpEntity<String>(json.toString(), headers);
		ResultModel rm = restTemplate.postForObject("http://localhost/user/login", formEntity, ResultModel.class);
		System.out.println(rm.getMsg());
	}

	@Test
	public void postMsg() {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		ResultModel rm = restTemplate.postForObject("http://localhost/testAsync/postMsg", "hello", ResultModel.class);
		System.out.println(rm.getMsg());
	}

	@Test
	public void postMap() throws JSONException {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		Map<String, Object> map = new HashMap<>();
		map.put("msg", "hello world");
		ResultModel rm = restTemplate.postForObject("http://localhost/testAsync/postMap", map, ResultModel.class);
		System.out.println(rm.getData());
	}

	@Test
	public void postMap2() throws JSONException {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		JSONObject json = new JSONObject();
		json.put("phone", "17702525841");
		json.put("password", "000000");
		HttpEntity<String> formEntity = new HttpEntity<String>(json.toString(), headers);
		ResultModel rm = restTemplate.postForObject("http://localhost/testAsync/postMap", formEntity,
				ResultModel.class);
		System.out.println(rm.getData());
	}

	@Autowired
	Keys keys;

	@Test
	public void sendSmsCode() throws JSONException {
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Authorization",Keys.JPhsh.AUTHORIZATION);
		JSONObject json = new JSONObject(), temp = new JSONObject();
		json.put("mobile", "17702525841");
		json.put("temp_id",Keys.JPhsh.TEMP_ID);
		temp.put("code", "123456");
		json.put("temp_para", temp);
		HttpEntity<String> formEntity = new HttpEntity<String>(json.toString(), headers);
		String str = restTemplate.postForObject("https://api.sms.jpush.cn/v1/messages", formEntity, String.class);
		System.out.println(str);
	}

}
