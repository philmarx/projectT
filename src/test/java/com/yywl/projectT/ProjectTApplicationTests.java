package com.yywl.projectT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bo.AliyunStsBo;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.UserDmo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProjectTApplicationTests {

	@Autowired
	UserDao userDao;

	@Test
	public void test() throws IOException {
		File file = new File("E:/update.sql");
		if (file.exists()) {
			file.delete();
		}
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file);
		PrintWriter writer = new PrintWriter(out);
		List<UserDmo> list = this.userDao.findByQqUidIsNotNullOrWxUidIsNotNull();
		for (UserDmo user : list) {
			String nickname = user.getNickname();
			String sql = null;
			if (nickname.contains(".")) {
				if (!StringUtils.isEmpty(user.getQqUid()) && !StringUtils.isEmpty(user.getWxUid())) {
					sql = "update user set qq_uid='" + user.getQqUid() + "',wx_uid='" + user.getWxUid()
							+ "' where nickname like '%" + user.getNickname().replaceAll("'", "") + "%';";
				} else if (StringUtils.isEmpty(user.getQqUid()) && !StringUtils.isEmpty(user.getWxUid())) {
					sql = "update user set wx_uid='" + user.getWxUid() + "' where nickname like '%"
							+ user.getNickname().replaceAll("'", "") + "%';";
				} else if (StringUtils.isEmpty(user.getWxUid()) && !StringUtils.isEmpty(user.getQqUid())) {
					sql = "update user set qq_uid='" + user.getQqUid() + "' where nickname like '%"
							+ user.getNickname().replaceAll("'", "") + "%';";
				}

			} else {
				if (!StringUtils.isEmpty(user.getQqUid()) && !StringUtils.isEmpty(user.getWxUid())) {
					sql = "update user set qq_uid='" + user.getQqUid() + "',wx_uid='" + user.getWxUid()
							+ "' where nickname = '" + user.getNickname().replaceAll("'", "") + "';";
				} else if (StringUtils.isEmpty(user.getQqUid()) && !StringUtils.isEmpty(user.getWxUid())) {
					sql = "update user set wx_uid='" + user.getWxUid() + "' where nickname = '"
							+ user.getNickname().replaceAll("'", "") + "';";
				} else if (StringUtils.isEmpty(user.getWxUid()) && !StringUtils.isEmpty(user.getQqUid())) {
					sql = "update user set qq_uid='" + user.getQqUid() + "' where nickname = '"
							+ user.getNickname().replaceAll("'", "") + "';";
				}
			}
			if (!StringUtils.isEmpty(sql)) {
				writer.println(sql);
			} else {
				System.out.println(
						user.getId() + "," + user.getNickname() + "," + user.getWxUid() + "," + user.getQqUid() + "");
			}
		}
		out.close();
	}

	@Autowired
	AliyunStsBo aliyunStsBo;

	@Test
	public void testGetStsTokenByUserId() throws ClientException {
		//
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
		// 创建OSSClient实例
		Credentials credentials = aliyunStsBo.getStsTokenByUserId(10000000004L);
		CredentialsProvider credentialsProvider = new DefaultCredentialProvider(new DefaultCredentials(
				credentials.getAccessKeyId(), credentials.getAccessKeySecret(), credentials.getSecurityToken()));
		OSSClient ossClient = new OSSClient(Keys.Aliyun.STS_ENDPOINT, credentialsProvider);
		// 上传
		ossClient.putObject(Keys.Aliyun.STS_BUCKET_NAME, "user/10000000004/avatar", new ByteArrayInputStream(bytes));
		// 关闭client
		ossClient.shutdown();
	}

	@Test
	public void ossClientPut() throws Exception {
		Credentials credentials = aliyunStsBo.getStsTokenForRoom();
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
		params.put("path", "pages/roomturn/roomturn?roomId=1000000000634");
		String paramsBody = Formatter.gson.toJson(params);
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsBody, headers);
		byte[] bytes = rest.postForObject(url.toString(), httpEntity, byte[].class);
		// 创建OSSClient实例
		CredentialsProvider credentialsProvider = new DefaultCredentialProvider(new DefaultCredentials(
				credentials.getAccessKeyId(), credentials.getAccessKeySecret(), credentials.getSecurityToken()));
		OSSClient ossClient = new OSSClient(Keys.Aliyun.STS_ENDPOINT,credentialsProvider);
		// 上传
		ossClient.putObject(Keys.Aliyun.STS_BUCKET_NAME, "roomQRcode/1000000000634", new ByteArrayInputStream(bytes));
		// 关闭client
		ossClient.shutdown();
	}
}
