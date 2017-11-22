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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "friend_connection")
public class FriendDmo implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	private String origin;
	
	/**
	 * 备注
	 */
	private String remarks="";
	
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@ManyToOne
	@JoinColumn(name = "owner")
	@JsonIgnore
	private UserDmo owner;

	@ManyToOne
	@JoinColumn(name = "friend")
	private UserDmo friend;

	private int evaluatePoint = 0;

	private int evaluatedPoint = 0;

	private Double point = 0.0;
	public FriendDmo() {
		super();
		this.createTime=new Date();
	}

	public Date getCreateTime() {
		return createTime;
	}

	public int getEvaluatedPoint() {
		return evaluatedPoint;
	}

	public int getEvaluatePoint() {
		return evaluatePoint;
	}

	public UserDmo getFriend() {
		return friend;
	}

	public Long getId() {
		return id;
	}

	public String getOrigin() {
		return origin;
	}

	public UserDmo getOwner() {
		return owner;
	}

	public Double getPoint() {
		return point;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setEvaluatedPoint(int evaluatedPoint) {
		this.evaluatedPoint = evaluatedPoint;
	}

	public void setEvaluatePoint(int evaluatePoint) {
		this.evaluatePoint = evaluatePoint;
	}

	public void setFriend(UserDmo friend) {
		this.friend = friend;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public void setOwner(UserDmo owner) {
		this.owner = owner;
	}

	public void setPoint(Double point) {
		this.point = point;
	}

}
