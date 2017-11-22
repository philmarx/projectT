package com.yywl.projectT.bo;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;
import com.yywl.projectT.bean.GsonFactory;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.OssPolicyBean;
import com.yywl.projectT.bean.OssPolicyBean.StatementBean;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dmo.CircleDmo;

@Service
@Transactional(rollbackOn = Throwable.class)
public class AliyunStsBoImpl implements AliyunStsBo {

	@Autowired
	Keys keys;

	private AssumeRoleResponse assumeRole(String accessKeyId, String accessKeySecret, String roleArn,
			String roleSessionName, String policy, ProtocolType protocolType) throws ClientException {
		try {
			// 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
			IClientProfile profile = DefaultProfile.getProfile(Keys.Aliyun.REGION_CN_HANGZHOU, accessKeyId,
					accessKeySecret);
			DefaultAcsClient client = new DefaultAcsClient(profile);
			// 创建一个 AssumeRoleRequest 并设置请求参数
			final AssumeRoleRequest request = new AssumeRoleRequest();
			request.setVersion(Keys.Aliyun.STS_API_VERSION);
			request.setMethod(MethodType.POST);
			request.setProtocol(protocolType);
			request.setRoleArn(roleArn);
			request.setRoleSessionName(roleSessionName);
			request.setDurationSeconds(3600L);
			request.setPolicy(policy);
			// 发起请求，并得到response
			final AssumeRoleResponse response = client.getAcsResponse(request);
			return response;
		} catch (ClientException e) {
			log.error(e.getClass().getName() + ":" + e.getMessage());
			throw e;
		}
	}

	private final static Log log = LogFactory.getLog(AliyunStsBoImpl.class);
	@Autowired
	CircleBo circleBo;

	@Autowired
	CircleDao circleDao;

	@Override
	public Credentials getStsTokenByUserId(Long userId) throws ClientException {
		// 只有 RAM用户（子账号）才能调用 AssumeRole 接口
		// 阿里云主账号的AccessKeys不能用于发起AssumeRole请求
		// 请首先在RAM控制台创建一个RAM用户，并为这个用户创建AccessKeys
		String accessKeyId = Keys.Aliyun.STS_ACCESS_KEY_ID;
		String accessKeySecret = Keys.Aliyun.STS_ACCESS_KEY_SECRET;
		// AssumeRole API 请求参数: RoleArn, RoleSessionName, Policy, and
		// DurationSeconds
		// RoleArn 需要在 RAM 控制台上获取
		String roleArn = Keys.Aliyun.STS_ROLE_ARN;
		// RoleSessionName 是临时Token的会话名称，自己指定用于标识你的用户，主要用于审计，或者用于区分Token颁发给谁
		// 但是注意RoleSessionName的长度和规则，不要有空格，只能有'-' '_' 字母和数字等字符
		// 具体规则请参考API文档中的格式要求
		String roleSessionName = Keys.Aliyun.STS_ROLE_SESSION_NAME;
		// 如何定制你的policy?
		OssPolicyBean ossPolicyBean = new OssPolicyBean();
		ossPolicyBean.setVersion("1");
		// StatementBean statementDownload = new StatementBean();
		StatementBean statementPut = new StatementBean();

		// statementDownload.getAction().add("oss:GetObject");
		// statementDownload.setEffect("Allow").getResource().add("acs:oss:*:1200687039332836:projectt*");

		statementPut.getAction().add("oss:PutObject");
		statementPut.getAction().add("oss:DeleteObject");
		statementPut.setEffect("Allow").getResource()
				.add("acs:oss:*:1227591017927389:tomeet-app-files/user/" + userId + "/*");
		// ossPolicyBean.getStatement().add(statementDownload);
		ossPolicyBean.getStatement().add(statementPut);

		// 查询有几个圈子的管理权限
		List<CircleDmo> circles = circleDao.findByManager_Id(userId);
		if (!circles.isEmpty()) {
			for (CircleDmo circleDmo : circles) {
				long circleId = circleDmo.getId();
				/*
				 * StatementBean temp = new StatementBean();
				 * temp.getAction().add("oss:PutObject");
				 * temp.getAction().add("oss:DeleteObject");
				 * temp.setEffect("Allow").getResource()
				 * .add("acs:oss:*:1200687039332836:projectt/circle/" + circleId + "/*");
				 * ossPolicyBean.getStatement().add(temp);
				 */
				// 资源数组添加一个资源
				statementPut.getResource().add("acs:oss:*:1227591017927389:tomeet-app-files/circle/" + circleId + "/*");
			}
		}
		/*
		 * StringBuilder policy = new StringBuilder(
		 * "{\"Statement\":[{\"Action\":[\"oss:GetObject\"],\"Effect\":\"Allow\",\"Resource\":\"acs:oss:*:1200687039332836:projectt*\"},{\"Action\":[\"oss:PutObject\",\"oss:DeleteObject\"],\"Effect\":\"Allow\",\"Resource\":\"acs:oss:*:1200687039332836:projectt/user/")
		 * .append(userId).append("*\"}],\"Version\":\"1\"}");
		 */
		// 此处必须为 HTTPS
		ProtocolType protocolType = ProtocolType.HTTPS;
		final AssumeRoleResponse response = assumeRole(accessKeyId, accessKeySecret, roleArn, roleSessionName,
				GsonFactory.gson.toJson(ossPolicyBean), protocolType);
		Credentials credentials = response.getCredentials();
		return credentials;
	}

	@Override
	public Credentials getStsTokenForRoom() throws ClientException {
		// 只有 RAM用户（子账号）才能调用 AssumeRole 接口
		// 阿里云主账号的AccessKeys不能用于发起AssumeRole请求
		// 请首先在RAM控制台创建一个RAM用户，并为这个用户创建AccessKeys
		String accessKeyId = Keys.Aliyun.STS_ACCESS_KEY_ID;
		String accessKeySecret = Keys.Aliyun.STS_ACCESS_KEY_SECRET;
		// AssumeRole API 请求参数: RoleArn, RoleSessionName, Policy, and
		// DurationSeconds
		// RoleArn 需要在 RAM 控制台上获取
		String roleArn = Keys.Aliyun.STS_ROLE_ARN;
		// RoleSessionName 是临时Token的会话名称，自己指定用于标识你的用户，主要用于审计，或者用于区分Token颁发给谁
		// 但是注意RoleSessionName的长度和规则，不要有空格，只能有'-' '_' 字母和数字等字符
		// 具体规则请参考API文档中的格式要求
		String roleSessionName = Keys.Aliyun.STS_ROLE_SESSION_NAME;
		// 如何定制你的policy?
		OssPolicyBean ossPolicyBean = new OssPolicyBean();
		ossPolicyBean.setVersion("1");
		
		StatementBean statementPut = new StatementBean();

		statementPut.getAction().add("oss:PutObject");
		statementPut.getAction().add("oss:DeleteObject");
		statementPut.setEffect("Allow").getResource().add("acs:oss:*:1227591017927389:tomeet-app-files/roomQRcode/*");
		
		ossPolicyBean.getStatement().add(statementPut);
		// 此处必须为 HTTPS
		ProtocolType protocolType = ProtocolType.HTTPS;
		final AssumeRoleResponse response = assumeRole(accessKeyId, accessKeySecret, roleArn, roleSessionName,
				GsonFactory.gson.toJson(ossPolicyBean), protocolType);
		Credentials credentials = response.getCredentials();
		return credentials;
	}

}
