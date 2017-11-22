package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 给房间中给成员的表现评价
 * @author jphil
 *
 */
@Entity
@Table(name = "room_evalutation")
public class RoomEvaluationDmo implements Serializable {

	private static final long serialVersionUID = -7799733479454373467L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private long ownerId;

	private long otherId;

	private double point=5;

	private long roomId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public long getOtherId() {
		return otherId;
	}

	public void setOtherId(long otherId) {
		this.otherId = otherId;
	}

	public double getPoint() {
		return point;
	}

	public void setPoint(double point) {
		this.point = point;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public RoomEvaluationDmo() {
		super();
	}

	public RoomEvaluationDmo(long ownerId, long otherId, int point, long roomId) {
		super();
		this.ownerId = ownerId;
		this.otherId = otherId;
		this.point = point;
		this.roomId = roomId;
	}

	

}
