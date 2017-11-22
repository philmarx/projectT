package com.yywl.projectT.vo;

public class RoomResultMemberVo extends RoomMemberVo {

	public RoomResultMemberVo(long id, String nickname, boolean ready, String avatarSignature) {
		super(id, nickname, ready, avatarSignature);
	}

	/**
	 * 活动结束后加分情况
	 */
	private int point;

	/**
	 * 总榜排名
	 */
	private long globalRanking;
	
	public long getGlobalRanking() {
		return globalRanking;
	}

	public void setGlobalRanking(long globalRanking) {
		this.globalRanking = globalRanking;
	}

	private int badge;
	
	public int getBadge() {
		return badge;
	}

	public void setBadge(int badge) {
		this.badge = badge;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}
	
	
}
