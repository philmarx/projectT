package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="location")
public class LocationDmo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name="user_id")
	private UserDmo user;
	
	private double longitude;
	
	/**
	 * 手机标识
	 */
	private String udid;
	
	public String getUdid() {
		return udid;
	}
	public void setUdid(String udid) {
		this.udid = udid;
	}
	/**
	 * 用户发送位置的ip地址
	 */
	private String ip;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	private double latitude;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	
	private String place;
	
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	@ManyToOne
	@JoinColumn(name="room_id",nullable=true)
	private RoomDmo room;
	
	public RoomDmo getRoom() {
		return room;
	}
	public void setRoom(RoomDmo room) {
		this.room = room;
	}
	public LocationDmo() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public UserDmo getUser() {
		return user;
	}
	public void setUser(UserDmo user) {
		this.user = user;
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
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	public LocationDmo(Long id, UserDmo user, double longitude, double latitude, Date sendTime,String ip) {
		super();
		this.ip=ip;
		this.id = id;
		this.user = user;
		this.longitude = longitude;
		this.latitude = latitude;
		this.sendTime = sendTime;
	}
	
}
