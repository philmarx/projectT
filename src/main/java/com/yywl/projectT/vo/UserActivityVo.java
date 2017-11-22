package com.yywl.projectT.vo;

import java.io.Serializable;

public class UserActivityVo implements Serializable{

	private static final long serialVersionUID = -7686260775451237467L;
	
	private Long userId;
	
	private String nickname;
	
	private int point;
	
	private long ranking;
	
	private int gameId;
	
	private String gameName;
	
	private String avatarSignature;
	
	private int count;

	private Boolean gender;
	
	public Boolean isGender() {
		return gender;
	}

	public void setGender(Boolean gender) {
		this.gender = gender;
	}

	public void setRanking(long ranking) {
		this.ranking = ranking;
	}

	public UserActivityVo() {
		super();
	}

	public UserActivityVo(Long userId, String nickname, int point, long ranking, int gameId, String gameName,
			String avatarSignature, int count) {
		super();
		this.userId = userId;
		this.nickname = nickname;
		this.point = point;
		this.ranking = ranking;
		this.gameId = gameId;
		this.gameName = gameName;
		this.avatarSignature = avatarSignature;
		this.count = count;
	}
	
	public String getAvatarSignature() {
		return avatarSignature;
	}

	public int getCount() {
		return count;
	}

	public int getGameId() {
		return gameId;
	}

	public String getGameName() {
		return gameName;
	}

	public String getNickname() {
		return nickname;
	}

	public int getPoint() {
		return point;
	}

	public long getRanking() {
		return ranking;
	}

	public Long getUserId() {
		return userId;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	
}
