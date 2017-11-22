package com.yywl.projectT.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RoomVo {
	protected Long id;

	protected String name;

	protected boolean isAnonymous;
	
	public boolean isAnonymous() {
		return isAnonymous;
	}

	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}

	protected String place;

	protected UserVo manager;

	protected boolean open = true;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	protected Date beginTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	protected Date endTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	protected Date createTime;

	/**
	 * 0新建,1准备中,2进行中,3待评价,4已结束
	 */
	protected Integer state;

	protected boolean locked;

	protected GameVo game;

	protected int money = 0;
	/**
	 * 已加入的人数
	 */
	protected Integer joinMember = 0;

	/**
	 * 已加入的男生人数
	 */
	protected Integer joinManMember = 0;

	/**
	 * 已加入的女生人数
	 */
	protected Integer joinWomanMember = 0;

	/**
	 * 房间总人数
	 */
	protected Integer memberCount = 0;

	/**
	 * 房间男生人数
	 */
	protected Integer manCount = 0;
	/**
	 * 房间女生人数
	 */
	protected Integer womanCount = 0;

	protected String description;
	/**
	 * 经度
	 */
	protected Double longitude;
	/**
	 * 纬度
	 */
	protected Double latitude;
	/**
	 * 房主点击准备的时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	protected Date prepareTime;

	@Transient
	protected List<RoomMemberVo> joinMembers = new ArrayList<>();

	protected String city;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public UserVo getManager() {
		return manager;
	}

	public void setManager(UserVo manager) {
		this.manager = manager;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public GameVo getGame() {
		return game;
	}

	public void setGame(GameVo game) {
		this.game = game;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public Integer getJoinMember() {
		return joinMember;
	}

	public void setJoinMember(Integer joinMember) {
		this.joinMember = joinMember;
	}

	public Integer getJoinManMember() {
		return joinManMember;
	}

	public void setJoinManMember(Integer joinManMember) {
		this.joinManMember = joinManMember;
	}

	public Integer getJoinWomanMember() {
		return joinWomanMember;
	}

	public void setJoinWomanMember(Integer joinWomanMember) {
		this.joinWomanMember = joinWomanMember;
	}

	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	public Integer getManCount() {
		return manCount;
	}

	public void setManCount(Integer manCount) {
		this.manCount = manCount;
	}

	public Integer getWomanCount() {
		return womanCount;
	}

	public void setWomanCount(Integer womanCount) {
		this.womanCount = womanCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Date getPrepareTime() {
		return prepareTime;
	}

	public void setPrepareTime(Date prepareTime) {
		this.prepareTime = prepareTime;
	}

	public List<RoomMemberVo> getJoinMembers() {
		return joinMembers;
	}

	public void setJoinMembers(List<RoomMemberVo> joinMembers) {
		this.joinMembers = joinMembers;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public RoomVo() {
		super();
	}

	public RoomVo(Long id, String name, String place, UserVo manager, boolean open, 
			Date beginTime, Date endTime, Date createTime, Integer state, boolean locked, GameVo game, int money,
			Integer joinMember, Integer joinManMember, Integer joinWomanMember, Integer memberCount,
			Integer manCount, Integer womanCount, String description, Double longitude, Double latitude,
			Date prepareTime, List<RoomMemberVo> joinMembers, String city) {
		super();
		this.id = id;
		this.name = name;
		this.place = place;
		this.manager = manager;
		this.open = open;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.createTime = createTime;
		this.state = state;
		this.locked = locked;
		this.game = game;
		this.money = money;
		this.joinMember = joinMember;
		this.joinManMember = joinManMember;
		this.joinWomanMember = joinWomanMember;
		this.memberCount = memberCount;
		this.manCount = manCount;
		this.womanCount = womanCount;
		this.description = description;
		this.longitude = longitude;
		this.latitude = latitude;
		this.prepareTime = prepareTime;
		this.joinMembers = joinMembers;
		this.city = city;
	}

}
