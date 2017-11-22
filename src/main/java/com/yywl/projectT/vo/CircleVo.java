package com.yywl.projectT.vo;

public class CircleVo {
	private Long id;

	private String name;

	private String city;

	private String place;

	private String notice;

	private UserVo manager;

	private double longitude;

	private double latitude;

	private int hot;

	private String avatarSignature;

	private String bgSignature;

	private int memberCount;

	private int roomCount;

	private boolean isSign;
	
	public boolean isSign() {
		return isSign;
	}

	public void setSign(boolean isSign) {
		this.isSign = isSign;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public int getRoomCount() {
		return roomCount;
	}

	public void setRoomCount(int roomCount) {
		this.roomCount = roomCount;
	}

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

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public UserVo getManager() {
		return manager;
	}

	public void setManager(UserVo manager) {
		this.manager = manager;
	}


	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public int getHot() {
		return hot;
	}

	public void setHot(int hot) {
		this.hot = hot;
	}

	public String getAvatarSignature() {
		return avatarSignature;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

	public String getBgSignature() {
		return bgSignature;
	}

	public void setBgSignature(String bgSignature) {
		this.bgSignature = bgSignature;
	}

	public CircleVo() {
		super();
	}

	public CircleVo(Long id, String name, String city, String place, String notice, UserVo manager, int memberCount,
			double longitude, double latitude, int hot, String avatarSignature, String bgSignature) {
		super();
		this.id = id;
		this.name = name;
		this.city = city;
		this.place = place;
		this.notice = notice;
		this.manager = manager;
		this.longitude = longitude;
		this.latitude = latitude;
		this.hot = hot;
		this.avatarSignature = avatarSignature;
		this.bgSignature = bgSignature;
		this.memberCount=memberCount;
	}

}
