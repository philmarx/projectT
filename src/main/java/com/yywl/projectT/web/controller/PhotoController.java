package com.yywl.projectT.web.controller;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bo.AliyunStsBo;
import com.yywl.projectT.bo.ImageBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dmo.UserDmo;

@RestController
@RequestMapping("photo")
public class PhotoController {
	@Autowired
	AliyunStsBo aliyunStsBo;
	@Autowired
	UserBo userBo;
	@Autowired
	ImageBo imageBo;

	/**
	 * 获取阿里云OSS服务器的token
	 * 
	 * @param userId
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "getToken")
	public Callable<ResultModel> getToken(Long userId, String token) throws Exception {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			Credentials credentials=aliyunStsBo.getStsTokenByUserId(user.getId());
			return new ResultModel(true, null, credentials);
		};
	}

	@PostMapping("save")
	public Callable<ResultModel> save(long userId, String token, String signature,
			String which) {
		return () -> {
			imageBo.save(userId, token,  signature, which);
			return new ResultModel();
		};
	}
}
