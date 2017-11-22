package com.yywl.projectT.vo;

public class FriendVo {
	private Long id;
	private int point;
	private String nickname;
	private String avatarSignature;
	private boolean isVip;
	
	public boolean isVip() {
		return isVip;
	}

	public void setVip(boolean isVip) {
		this.isVip = isVip;
	}

	public String getAvatarSignature() {
		return avatarSignature;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}


	public FriendVo() {
		super();
	}

	public FriendVo(Long id, int point, String nickname,  String avatarSignature,boolean isVip) {
		super();
		this.id = id;
		this.point = point;
		this.nickname = nickname;
		this.isVip=isVip;
		this.avatarSignature = avatarSignature;
	}

}
