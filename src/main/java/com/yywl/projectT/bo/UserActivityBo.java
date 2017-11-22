package com.yywl.projectT.bo;

import java.util.List;

import com.yywl.projectT.vo.UserActivityVo;

public interface UserActivityBo {
	List<UserActivityVo> findByGame_Id(int gameId);
	
	List<UserActivityVo> findByUser_Id(long userId);

	UserActivityVo findByMemberAndGame_Id(long member, int gameId);
}
