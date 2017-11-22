package com.yywl.projectT.bo;

import java.util.List;

import com.yywl.projectT.dmo.FriendDmo;
import com.yywl.projectT.vo.EvaluationVo;

public interface FriendBo {
	/**
	 * @param userId
	 * @return
	 */
	List<FriendDmo> findFriends(Long userId);

	FriendDmo findFriend(Long ownId, Long friendId);

	/**
	 * 根据分数获取好友的颜色
	 * @param color
	 * @return
	 */
	String getColor(double point);

	/**
	 * 给好友评分
	 * @param userId
	 * @param token
	 * @param vos
	 * @throws Exception 
	 */
	void evalute(Long userId, String token, long roomId,List<EvaluationVo> vos) throws Exception;

	/**
	 * 变成蓝色好友
	 * @param ownerId
	 * @param friendId
	 * @param origin 
	 * @throws Exception 
	 */
	void becameBlueFriends(long ownerId, long friendId, String origin) throws Exception;

	/**
	 * 邀请好友
	 * @param userId
	 * @param token
	 * @param friendId
	 * @throws Exception 
	 */
	void invitateFriend(long userId, String token, long friendId,String origin) throws Exception;

	/**
	 * 接收邀请
	 * @param userId
	 * @param token
	 * @param invitationId
	 * @throws Exception 
	 */
	void receiveInvitation(long userId, String token, long invitationId,int state) throws Exception;

	/**
	 * 删除邀请记录
	 * @param userId
	 * @param token
	 * @param invitationId
	 */
	void removeInvitation(long userId, String token, long invitationId) throws Exception;
	
}
