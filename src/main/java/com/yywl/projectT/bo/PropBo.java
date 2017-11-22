package com.yywl.projectT.bo;

import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.UserDmo;

public interface PropBo {

	PropDmo findByUser_Id(long userId);

	void save(PropDmo prop);

	/**
	 * 购买小纸条
	 * 
	 * @param userId
	 * @param token
	 * @param count
	 * @throws Exception
	 */
	void buyNote(UserDmo user, int count) throws Exception;

	/**
	 * 购买标签消除卡
	 * 
	 * @param userId
	 * @param token
	 * @param count
	 * @throws Exception
	 */
	void buyLabelClearCard(UserDmo user, int count) throws Exception;

	/**
	 * 购买改名卡
	 * 
	 * @param userId
	 * @param token
	 * @param count
	 * @throws Exception
	 */
	void buyChangeNameCard(UserDmo user, int count) throws Exception;

	/**
	 * 购买补签卡
	 * 
	 * @param userId
	 * @param token
	 * @param count
	 * @throws Exception
	 */
	void buySignAgainCard(UserDmo user, int count) throws Exception;

	/**
	 * 购买一个月的vip会员
	 * 
	 * @param userId
	 * @param token
	 * @throws Exception
	 */
	void buyVipOneMonth(UserDmo user,int count) throws Exception;

	/**
	 * 购买三个月的vip会员
	 * 
	 * @param userId
	 * @param token
	 * @throws Exception
	 */
	void buyVipThreeMonth(UserDmo user,int count) throws Exception;

	/**
	 * 购买一年的vip会员
	 * 
	 * @param userId
	 * @param token
	 * @throws Exception 
	 */
	void buyVipOneYear(UserDmo user,int count) throws Exception;

	void buyProp(String outTradeNo) throws Exception;

	/**
	 * 使用同伴卡
	 * @param user
	 * @param roomId
	 * @param count
	 * @throws Exception 
	 */
	void useFriendCard(UserDmo user, long roomId, int count) throws Exception;

}
