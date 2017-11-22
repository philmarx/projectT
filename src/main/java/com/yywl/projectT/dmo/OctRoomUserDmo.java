package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="oct_room_user")
public class OctRoomUserDmo implements Serializable{

	private static final long serialVersionUID = 861015639908347379L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private long roomId;
	
	private long userId;
	/**
	 * 拒绝理由
	 */
	private String reason;
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	private boolean hasNoFriend;
	/**
	 * 奖金
	 */
	private int bounty;
	/**
	 * 是否违规
	 */
	private boolean isFoul;
	/**
	 * 0 审核中，1 审核通过未领取 ， 2 审核通过已领取 ， 3 审核不通过
	 */
	private int state;
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isFoul() {
		return isFoul;
	}

	public void setFoul(boolean isFoul) {
		this.isFoul = isFoul;
	}

	public int getBounty() {
		return bounty;
	}

	public void setBounty(int bounty) {
		this.bounty = bounty;
	}
	public OctRoomUserDmo() {
		super();
	}

	public OctRoomUserDmo(Long id, long roomId, long userId, boolean hasNoFriend) {
		super();
		this.id = id;
		this.roomId = roomId;
		this.userId = userId;
		this.hasNoFriend = hasNoFriend;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public boolean isHasNoFriend() {
		return hasNoFriend;
	}

	public void setHasNoFriend(boolean hasNoFriend) {
		this.hasNoFriend = hasNoFriend;
	}
	
	
}
