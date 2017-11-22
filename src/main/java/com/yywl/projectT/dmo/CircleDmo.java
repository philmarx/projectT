package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "circle")
public class CircleDmo implements Serializable {

	private static final long serialVersionUID = 6373922882216986095L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;

	private String name;

	private String city;

	private String place;

	private String notice;

	@ManyToOne
	@JoinColumn(name = "manager")
	private UserDmo manager;

	@Transient
	private int roomCount;
	
	private int memberCount;
	
	/**
	 * 经度
	 */
	private double longitude;

	/**
	 * 纬度
	 */
	private double latitude;

	private int hot;

	private String avatarSignature;

	private String bgSignature;

	public CircleDmo() {
		super();
	}

	public CircleDmo(Long id) {
		super();
		this.id = id;
	}

	public String getAvatarSignature() {
		return avatarSignature;
	}
	public String getBgSignature() {
		return bgSignature;
	}

	public String getCity() {
		return city;
	}

	public int getHot() {
		return hot;
	}

	public Long getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public UserDmo getManager() {
		return manager;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public String getName() {
		return name;
	}

	public String getNotice() {
		return notice;
	}

	public String getPlace() {
		return place;
	}

	public int getRoomCount() {
		return roomCount;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

	public void setBgSignature(String bgSignature) {
		this.bgSignature = bgSignature;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setHot(int hot) {
		this.hot = hot;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setManager(UserDmo manager) {
		this.manager = manager;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public void setRoomCount(int roomCount) {
		this.roomCount = roomCount;
	}

}
