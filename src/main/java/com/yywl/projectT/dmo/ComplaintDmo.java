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
@Table(name = "complaint")
public class ComplaintDmo implements Serializable {

	private static final long serialVersionUID = -8337777003387814421L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "owner_id")
	private UserDmo owner;

	private String photoUrl;
	
	
	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	@ManyToOne
	@JoinColumn(name = "person_id")
	private UserDmo person;

	@ManyToOne
	@JoinColumn(name = "room_id",nullable=true)
	private RoomDmo room;

	private String content;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date createTime=new Date();

	public ComplaintDmo() {
		super();
	}

	public String getContent() {
		return content;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public Long getId() {
		return id;
	}

	public UserDmo getOwner() {
		return owner;
	}

	public UserDmo getPerson() {
		return person;
	}

	public RoomDmo getRoom() {
		return room;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOwner(UserDmo owner) {
		this.owner = owner;
	}

	public void setPerson(UserDmo person) {
		this.person = person;
	}

	public void setRoom(RoomDmo room) {
		this.room = room;
	}

}
