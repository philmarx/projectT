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

@Entity
@Table(name = "friend_Invitation")
public class FriendInvitationDmo implements Serializable {

	private static final long serialVersionUID = -731866137149186668L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name="owner")
	@ManyToOne
	private UserDmo owner;

	@JoinColumn(name="friend")
	@ManyToOne
	private UserDmo friend;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	
	private int state;
	
	private String origin;

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public FriendInvitationDmo() {
		super();
	}

	public FriendInvitationDmo(Long id, UserDmo owner, UserDmo friend, Date createTime, int state) {
		super();
		this.id = id;
		this.owner = owner;
		this.friend = friend;
		this.createTime = createTime;
		this.state = state;
	}



	public Date getCreateTime() {
		return createTime;
	}

	public UserDmo getFriend() {
		return friend;
	}

	public Long getId() {
		return id;
	}

	public UserDmo getOwner() {
		return owner;
	}

	public int getState() {
		return state;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setFriend(UserDmo friend) {
		this.friend = friend;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOwner(UserDmo owner) {
		this.owner = owner;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	
}
