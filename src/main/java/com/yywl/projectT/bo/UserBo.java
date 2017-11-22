package com.yywl.projectT.bo;

import java.util.Date;

import com.yywl.projectT.dmo.UserDmo;

public interface UserBo {

	/**
	 * 根据ID和token登录
	 */
	UserDmo loginByToken(Long userId, String token) throws Exception;

	/**
	 * 根据用户名，密码，登录类型登录
	 */
	UserDmo loginByPassword(String username, String password, String type) throws Exception;

	/**
	 * 根据用户名和类型查找
	 * 
	 * @param username
	 * @param type
	 * @return
	 * @throws Exception
	 */
	UserDmo findByUsernameAndType(String username, String type) throws Exception;

	/**
	 * 所有渠道注册
	 * 
	 * @param username
	 * @param password
	 * @param type
	 * @param iconUrl
	 * @param gender
	 * @return
	 * @throws Exception
	 */
	UserDmo register(String username, String type, Long recommenderId) throws Exception;

	String newNickname() throws Exception;
	String newNickname(String nickname) throws Exception;
	/**
	 * 初始化用户信息
	 * 
	 * @param id
	 * @param token
	 * @param nickname
	 * @param password
	 * @param gender
	 * @param age
	 * @return
	 * @throws Exception
	 */
	UserDmo initInfo(long id, String token, String nickname, String password, boolean gender, Date birthday,String recommenderAccount)
			throws Exception;

	/**
	 * 建议
	 * 
	 * @param userId
	 * @param token
	 * @param content
	 * @throws Exception
	 */
	void suggest(Long userId, String token, String content,String photoUrl) throws Exception;

	/**
	 * 重新获取token
	 * 
	 * @return
	 * @throws Exception
	 */
	String requestNewToken(UserDmo user) throws Exception;

	/**
	 * 修改昵称
	 * 
	 * @param userId
	 * @param token
	 * @param nickname
	 * @throws Exception
	 */
	void updateNickname(long userId, String token, String nickname) throws Exception;

	/**
	 * 实名认证
	 * 
	 * @param userId
	 * @param token
	 * @param realName
	 * @param idCard
	 * @throws Exception
	 */
	void authorized(long userId, String token, String realName, String idCard) throws Exception;

	/**
	 * 绑定手机号
	 * 
	 * @param userId
	 * @param token
	 * @param phone
	 * @param smsCode
	 */
	void bindPhone(long userId, String token, String phone, String smsCode) throws Exception;

	/**
	 * 清除标签
	 * 
	 * @param userId
	 * @param token
	 * @param removedLable
	 * @throws Exception
	 */
	void removeLabel(long userId, String token, String removedLable) throws Exception;
	
	/**
	 * 注册
	 * @param username
	 * @param type
	 * @param nickname
	 * @param recommenderId
	 * @return
	 * @throws Exception
	 */
	UserDmo register(String username, String type, String nickname,Boolean gender,String avatarSignature,Long recommenderId) throws Exception;

	/**
	 * 合并用户
	 * @param authUser
	 * @param phoneUser
	 * @throws Exception 
	 */
	void merge(UserDmo authUser, UserDmo phoneUser) throws Exception;

	/**
	 * 解绑第三方账号
	 * @param user
	 * @param type
	 * @throws Exception
	 */
	void unbind3Part(UserDmo user, String type) throws Exception;

	/**
	 * 强制解绑
	 * @param mergeFromUser
	 * @param mergeToUser
	 * @param type
	 * @throws Exception 
	 */
	void forceUnbind(UserDmo mergeFromUser, UserDmo mergeToUser, String type) throws Exception;

	UserDmo registerAndInit(String uid, String wechat, String nickname, Boolean gender, String avatarSignature) throws Exception;
}
