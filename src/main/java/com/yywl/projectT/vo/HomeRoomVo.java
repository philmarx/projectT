package com.yywl.projectT.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 首页 findRoomsByGameOrder显示房间的类
 *
 */
public class HomeRoomVo {
	protected Boolean isAnonymous;
	
	public Boolean isAnonymous() {
		return isAnonymous;
	}

	public void setAnonymous(Boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	public static class UserVo {
		private Long id;
		private String avatarSignature;
		private Boolean gender;
		
		public Boolean isGender() {
			return gender;
		}

		public void setGender(Boolean gender) {
			this.gender = gender;
		}

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date beginTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date endTime;
	
	private Date prepareTime;
	
	public Date getPrepareTime() {
		return prepareTime;
	}
	public void setPrepareTime(Date prepareTime) {
		this.prepareTime = prepareTime;
	}
	
	private GameVo game;

	private Long id;

	private Double longitude;

	private Double latitude;

	private Boolean locked;

	private Integer money;

	private String name;

	private String place;

	private Integer joinMember;

	private Integer joinWomanMember;

	private Integer joinManMember;

	private List<UserVo> joinMembers;

	private Integer manCount;
	
	private Integer womanCount;
	
	private Integer memberCount;
	
	private Integer state;
	
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	private Boolean isVip;
	
	public Boolean isVip() {
		return isVip;
	}
	public void setVip(Boolean isVip) {
		this.isVip = isVip;
	}
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
	public Integer getJoinManMember() {
		return joinManMember;
	}
	public Integer getJoinMember() {
		return joinMember;
	}
	public List<UserVo> getJoinMembers() {
		return joinMembers;
	}
	public Integer getJoinWomanMember() {
		return joinWomanMember;
	}
	public Double getLatitude() {
		return latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public Integer getManCount() {
		return manCount;
	}
	public Integer getMemberCount() {
		return memberCount;
	}
	public Integer getMoney() {
		return money;
	}
	public String getName() {
		return name;
	}
	public String getPlace() {
		return place;
	}
	public Integer getWomanCount() {
		return womanCount;
	}
	public Boolean isLocked() {
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
	public void setJoinManMember(Integer joinManMember) {
		this.joinManMember = joinManMember;
	}
	public void setJoinMember(Integer joinMember) {
		this.joinMember = joinMember;
	}
	public void setJoinMembers(List<UserVo> joinMembers) {
		this.joinMembers = joinMembers;
	}
	public void setJoinWomanMember(Integer joinWomanMember) {
		this.joinWomanMember = joinWomanMember;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public void setManCount(Integer manCount) {
		this.manCount = manCount;
	}
	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}
	public void setMoney(Integer money) {
		this.money = money;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public void setWomanCount(Integer womanCount) {
		this.womanCount = womanCount;
	}
	public HomeRoomVo(Long id, Date beginTime, Date endTime, GameVo game, Double longitude, Double latitude,
			Boolean locked, Integer money, String name, String place, Integer joinMember, Integer joinWomanMember,
			Integer joinManMember, List<UserVo> joinMembers, Integer manCount, Integer womanCount, Integer memberCount,Integer state) {
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
		this.state=state;
	}
	public HomeRoomVo() {
		super();
	}
	
}
