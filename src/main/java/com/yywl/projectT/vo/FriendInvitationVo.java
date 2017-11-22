package com.yywl.projectT.vo;

import java.util.Date;

public class FriendInvitationVo {
	private Long id;
	private String nickname;
	private Date createTime;
	private Boolean gender;
	private String avatarSignature;
	private int state;
	private long userId;
	private String origin;
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Boolean getGender() {
		return gender;
	}
	public void setGender(Boolean gender) {
		this.gender = gender;
	}
	public String getAvatarSignature() {
		return avatarSignature;
	}
	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}
	public FriendInvitationVo() {
		super();
	}
	
	
	
}
