package com.yywl.projectT.bo;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;

public interface AliyunStsBo {
	/**
	 * 查找阿里云OSS的token
	 * @param userId
	 * @return
	 * @throws ClientException
	 */
	Credentials getStsTokenByUserId(Long userId) throws ClientException;
	
	Credentials getStsTokenForRoom() throws ClientException;
}
