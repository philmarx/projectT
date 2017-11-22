package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yywl.projectT.vo.RoomMemberVo;

@Entity
@Table(name = "room")
@JsonIgnoreProperties(value = "password")
	public class RoomDmo implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	/**
	 * 是否已评价过,从RoomMemberDmo中获取
	 */
	@Transient
	private boolean isEvaluated;
	
	/**
	 * 是否匿名
	 */
	private boolean isAnonymous=true;
	
	public boolean isAnonymous() {
		return isAnonymous;
	}
	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}

	/**
	 * 活动结束后剩余的资金(从缺席的人中扣除)
	 */
	private int remainingMoney;
	
	private String place;

	/**
	 * 游戏模式，0为娱乐模式，1为竞技模式。
	 */
	private int gameMode;

	@ManyToOne
	@JoinColumn(name = "manager")
	private UserDmo manager;

	private boolean open = true;

	@ManyToOne(optional = true)
	@JoinColumn(name = "belong_circle", nullable = true)
	private CircleDmo belongCircle;
	@Column(name = "begin_time")
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date beginTime;

	@Column(name = "end_time")
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date endTime;

	@Column(name = "create_time")
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date createTime;

	/**
	 * 活动结束后评价时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date evaluateTime;

	/**
	 * 0新建,1准备中,2进行中,3待评价,4已结束
	 */
	private int state=0;

	@Column(name = "locked")
	private boolean locked;

	private String password;

	@ManyToOne
	@JoinColumn(name = "game")
	private GameDmo game;

	private int money = 0;
	/**
	 * 已加入的人数
	 */
	@Column(name = "join_member")
	private Integer joinMember = 0;
	
	/**
	 * 已加入的男生人数
	 */
	@Column(name = "join_man_member")
	private Integer joinManMember = 0;

	/**
	 * 已加入的女生人数
	 */
	@Column(name = "join_woman_member")
	private Integer joinWomanMember = 0;

	/**
	 * 房间总人数
	 */
	@Column(name = "member_count")
	private Integer memberCount = 0;

	/**
	 * 房间男生人数
	 */
	@Column(name = "man_count")
	private Integer manCount = 0;

	/**
	 * 房间女生人数
	 */
	@Column(name = "woman_count")
	private Integer womanCount = 0;

	private String description;

	/**
	 * 经度
	 */
	private Double longitude;
	/**
	 * 纬度
	 */
	private Double latitude;

	/**
	 * 房主点击准备的时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date prepareTime;

	@Transient
	private List<RoomMemberVo> joinMembers = new ArrayList<>();

	private String city;

	public RoomDmo() {
		super();
	}
	public RoomDmo(Long id) {
		super();
		this.id = id;
	}

	public Date getBeginTime() {
		return beginTime;
	}
	public CircleDmo getBelongCircle() {
		return belongCircle;
	}
	public String getCity() {
		return city;
	}
	public Date getCreateTime() {
		return createTime;
	}

	public String getDescription() {
		return description;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Date getEvaluateTime() {
		return evaluateTime;
	}

	public GameDmo getGame() {
		return game;
	}

	public int getGameMode() {
		return gameMode;
	}

	public Long getId() {
		return id;
	}

	public Integer getJoinManMember() {
		return this.joinManMember;
	}

	public Integer getJoinMember() {
		return joinMember;
	}

	public List<RoomMemberVo> getJoinMembers() {
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

	public UserDmo getManager() {
		return manager;
	}

	public Integer getManCount() {
		return manCount;
	}

	public Integer getMemberCount() {
		return memberCount;
	}

	public int getMoney() {
		return money;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getPlace() {
		return place;
	}

	public Date getPrepareTime() {
		return prepareTime;
	}

	public int getRemainingMoney() {
		return remainingMoney;
	}

	public int getState() {
		return state;
	}

	public Integer getWomanCount() {
		return womanCount;
	}

	public boolean isEvaluated() {
		return isEvaluated;
	}


	public boolean isLocked() {
		return locked;
	}

	public boolean isOpen() {
		return open;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public void setBelongCircle(CircleDmo belongCircle) {
		this.belongCircle = belongCircle;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setEvaluated(boolean isEvaluated) {
		this.isEvaluated = isEvaluated;
	}

	public void setEvaluateTime(Date evaluateTime) {
		this.evaluateTime = evaluateTime;
	}

	public void setGame(GameDmo game) {
		this.game = game;
	}

	public void setGameMode(int gameMode) {
		this.gameMode = gameMode;
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

	public void setJoinMembers(List<RoomMemberVo> joinMembers) {
		this.joinMembers = joinMembers;
	}

	public void setJoinWomanMember(Integer joinWomanMember) {
		this.joinWomanMember = joinWomanMember;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public void setManager(UserDmo manager) {
		this.manager = manager;
	}

	public void setManCount(Integer manCount) {
		this.manCount = manCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public void setPrepareTime(Date prepareTime) {
		this.prepareTime = prepareTime;
	}

	public void setRemainingMoney(int remainingMoney) {
		this.remainingMoney = remainingMoney;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setWomanCount(Integer womanCount) {
		this.womanCount = womanCount;
	}

}
