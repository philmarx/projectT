package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
@Entity
@Table(name="join_room_log")
public class JoinRoomLogDmo implements Serializable{

	private static final long serialVersionUID = 2150822176738164655L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Long userId;
	
	private Long roomId;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date joinTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Date getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}

	public JoinRoomLogDmo(Long id, Long userId, Long roomId, Date joinTime) {
		super();
		this.id = id;
		this.userId = userId;
		this.roomId = roomId;
		this.joinTime = joinTime;
	}

	public JoinRoomLogDmo() {
		super();
	}
	
	
	
}
