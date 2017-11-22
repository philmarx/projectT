package com.yywl.projectT.vo;

import java.util.Date;

public class PropVo {

	/**
	 * 小纸条数量
	 */
	private int noteCount;

	/**
	 * 标签清除卡数量
	 */
	private int labelClearCount;

	/**
	 * 改名卡数量
	 */
	private int changeNicknameCount;

	/**
	 * 再推荐多少人可获得一张观影券
	 */
	private int needRecommends;
	
	
	/**
	 * 会员失效时间
	 */
	private Date vipExpireDate = new Date();

	/**
	 * 剩余观影券
	 */
	private int movieTicket;

	/**
	 * 补签卡数量
	 */
	private int signCount;
	
	
	/**
	 * 同伴卡
	 */
	private int friendCard;
	
	/**
	 * 入场券
	 */
	private int roomTicket;
	
	
	public int getFriendCard() {
		return friendCard;
	}


	public void setFriendCard(int friendCard) {
		this.friendCard = friendCard;
	}


	public int getRoomTicket() {
		return roomTicket;
	}


	public void setRoomTicket(int roomTicket) {
		this.roomTicket = roomTicket;
	}


	public PropVo() {
		super();
	}
	
	
	public PropVo(int noteCount, int labelClearCount, int changeNicknameCount, int signCount,Date vipExpireDate) {
		super();
		this.signCount=signCount;
		this.noteCount = noteCount;
		this.labelClearCount = labelClearCount;
		this.changeNicknameCount = changeNicknameCount;
		this.vipExpireDate = vipExpireDate;
	}

	public int getChangeNicknameCount() {
		return changeNicknameCount;
	}

	public int getLabelClearCount() {
		return labelClearCount;
	}
	
	public int getMovieTicket() {
		return movieTicket;
	}

	public int getNeedRecommends() {
		return needRecommends;
	}

	public int getNoteCount() {
		return noteCount;
	}

	public int getSignCount() {
		return signCount;
	}

	public Date getVipExpireDate() {
		return vipExpireDate;
	}

	public void setChangeNicknameCount(int changeNicknameCount) {
		this.changeNicknameCount = changeNicknameCount;
	}

	public void setLabelClearCount(int labelClearCount) {
		this.labelClearCount = labelClearCount;
	}

	public void setMovieTicket(int movieTicket) {
		this.movieTicket = movieTicket;
	}

	public void setNeedRecommends(int needRecommends) {
		this.needRecommends = needRecommends;
	}

	public void setNoteCount(int noteCount) {
		this.noteCount = noteCount;
	}

	public void setSignCount(int signCount) {
		this.signCount = signCount;
	}

	public void setVipExpireDate(Date vipExpireDate) {
		this.vipExpireDate = vipExpireDate;
	}

}
