package com.yywl.projectT.vo;

public class RoomMemberVo {
	
	/**
	 * 房间成员的ID
	 */
	private long id;
	private String nickname="";
	private boolean ready;
	private String avatarSignature="";
	private boolean isVip;
	private boolean isOnline;
	private Boolean gender;
	
	public Boolean isGender() {
		return gender;
	}
	public void setGender(Boolean gender) {
		this.gender = gender;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public boolean isVip() {
		return isVip;
	}
	public void setVip(boolean isVip) {
		this.isVip = isVip;
	}
	/**
	 * 是否出席
	 */
	private boolean isAttend;
	
	/**
	 * 是否签到过
	 */
	private boolean isSigned;
	
	
	public boolean isAttend() {
		return isAttend;
	}
	public void setAttend(boolean isAttend) {
		this.isAttend = isAttend;
	}
	public boolean isSigned() {
		return isSigned;
	}
	public void setSigned(boolean isSigned) {
		this.isSigned = isSigned;
	}
	public String getAvatarSignature() {
		return avatarSignature;
	}
	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}
	public boolean isReady() {
		return ready;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public RoomMemberVo(long id, String nickname, boolean ready, String avatarSignature) {
		super();
		this.id = id;
		this.nickname = nickname;
		this.ready = ready;
		this.avatarSignature = avatarSignature;
	}
	
}
