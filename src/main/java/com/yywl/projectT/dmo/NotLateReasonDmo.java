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

@Table(name = "not_late_reason")
@Entity
public class NotLateReasonDmo implements Serializable {

	private static final long serialVersionUID = -5788601476386024460L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDmo user;

	@ManyToOne
	@JoinColumn(name = "room_id")
	private RoomDmo room;

	@ManyToOne
	@JoinColumn(name="admin_id",nullable=true)
	private AdminDmo admin;
	
	private int dealState;
	
	public AdminDmo getAdmin() {
		return admin;
	}

	public void setAdmin(AdminDmo admin) {
		this.admin = admin;
	}

	public int getDealState() {
		return dealState;
	}

	public void setDealState(int dealState) {
		this.dealState = dealState;
	}

	private String reason;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	private Long certifierId;//证明人id
	
	public Long getCertifierId() {
		return certifierId;
	}

	public void setCertifierId(Long certifierId) {
		this.certifierId = certifierId;
	}

	private String photoUrl;

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

	public RoomDmo getRoom() {
		return room;
	}

	public void setRoom(RoomDmo room) {
		this.room = room;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public NotLateReasonDmo() {
		super();
	}

	public NotLateReasonDmo(Long id, UserDmo user, RoomDmo room, String reason, String photoUrl,long personId,Date createTime) {
		super();
		this.createTime=createTime;
		this.id = id;
		this.user = user;
		this.room = room;
		this.certifierId=personId;
		this.reason = reason;
		this.photoUrl = photoUrl;
	}

}
