package com.yywl.projectT.vo;
/**
 * 活动结束后评价的用户列表
 * @author jphil
 *
 */
public class UserForEvaluatingVo extends UserVo{
	private boolean isScoring=true;
	public UserForEvaluatingVo(Long id, String nickname, String avatarSignature) {
		super(id,nickname,avatarSignature);
	}

	public boolean isScoring() {
		return isScoring;
	}

	public void setScoring(boolean isScoring) {
		this.isScoring = isScoring;
	}
	
	
}
