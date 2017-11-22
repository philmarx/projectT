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
@Table(name="oct_room")
public class OctRoomDmo implements Serializable{

	private static final long serialVersionUID = 861015639908347379L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private long roomId;
	
	private int signCount;

	private Long rewardAdminId;
	
	/**
	 * 0为审核中，1为通过，2为拒绝
	 */
	private int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	private String reason;
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * 奖金
	 */
	private int bounty;
	
	public int getBounty() {
		return bounty;
	}

	public void setBounty(int bounty) {
		this.bounty = bounty;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date rewardTime;
	
	public Date getRewardTime() {
		return rewardTime;
	}

	public void setRewardTime(Date rewardTime) {
		this.rewardTime = rewardTime;
	}
	public Long getRewardAdminId() {
		return rewardAdminId;
	}

	public void setRewardAdminId(Long rewardAdminId) {
		this.rewardAdminId = rewardAdminId;
	}

	public OctRoomDmo() {
		super();
	}

	public OctRoomDmo(Long id, long roomId, int signCount) {
		super();
		this.id = id;
		this.roomId = roomId;
		this.signCount = signCount;
	}

	public Long getId() {
		return id;
	}

	public long getRoomId() {
		return roomId;
	}

	public int getSignCount() {
		return signCount;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public void setSignCount(int signCount) {
		this.signCount = signCount;
	}
	
	

	
	

}
