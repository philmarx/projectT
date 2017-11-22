package com.yywl.projectT.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PersonalCenterRoomVo {
	
	private boolean isVip;
	
	public boolean isVip() {
		return isVip;
	}
	public void setVip(boolean isVip) {
		this.isVip = isVip;
	}
	public static class UserVo {
		private Long id;
		private String avatarSignature;

		public UserVo() {
			super();
		}

		public String getAvatarSignature() {
			return avatarSignature;
		}

		public Long getId() {
			return id;
		}

		public void setAvatarSignature(String avatarSignature) {
			this.avatarSignature = avatarSignature;
		}

		public void setId(Long id) {
			this.id = id;
		}

	}
	
	protected boolean isAnonymous;
	
	public boolean isAnonymous() {
		return isAnonymous;
	}

	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date beginTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date endTime;

	/**
	 * 是否评价过
	 */
	private boolean evaluated;
	
	public boolean isEvaluated() {
		return evaluated;
	}
	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}
	private GameVo game;

	private Long id;

	private int state;
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	private double longitude;

	private double latitude;

	private boolean locked;

	private int money;

	private String name;

	private String place;

	private int joinMember;

	private int joinWomanMember;

	private int joinManMember;

	private List<UserVo> joinMembers;

	private int manCount;
	
	private int womanCount;
	
	private int memberCount;
	
	public Date getBeginTime() {
		return beginTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public GameVo getGame() {
		return game;
	}
	public Long getId() {
		return id;
	}
	public int getJoinManMember() {
		return joinManMember;
	}
	public int getJoinMember() {
		return joinMember;
	}
	public List<UserVo> getJoinMembers() {
		return joinMembers;
	}
	public int getJoinWomanMember() {
		return joinWomanMember;
	}
	public double getLatitude() {
		return latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public int getManCount() {
		return manCount;
	}
	public int getMemberCount() {
		return memberCount;
	}
	public int getMoney() {
		return money;
	}
	public String getName() {
		return name;
	}
	public String getPlace() {
		return place;
	}
	public int getWomanCount() {
		return womanCount;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public void setGame(GameVo game) {
		this.game = game;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setJoinManMember(int joinManMember) {
		this.joinManMember = joinManMember;
	}
	public void setJoinMember(int joinMember) {
		this.joinMember = joinMember;
	}
	public void setJoinMembers(List<UserVo> joinMembers) {
		this.joinMembers = joinMembers;
	}
	public void setJoinWomanMember(int joinWomanMember) {
		this.joinWomanMember = joinWomanMember;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public void setManCount(int manCount) {
		this.manCount = manCount;
	}
	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public void setWomanCount(int womanCount) {
		this.womanCount = womanCount;
	}
	public PersonalCenterRoomVo(Long id, Date beginTime, Date endTime, GameVo game, double longitude, double latitude,
			boolean locked, int money, String name, String place, int joinMember, int joinWomanMember,
			int joinManMember, List<UserVo> joinMembers, int manCount, int womanCount, int memberCount,boolean isVip) {
		super();
		this.id = id;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.game = game;
		this.longitude = longitude;
		this.latitude = latitude;
		this.locked = locked;
		this.money = money;
		this.name = name;
		this.place = place;
		this.joinMember = joinMember;
		this.joinWomanMember = joinWomanMember;
		this.joinManMember = joinManMember;
		this.joinMembers = joinMembers;
		this.manCount = manCount;
		this.womanCount = womanCount;
		this.memberCount = memberCount;
		this.isVip=isVip;
	}
	public PersonalCenterRoomVo() {
		super();
	}
	
}
