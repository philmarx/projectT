package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "three_part_info")
public class ThreePartInfoDmo implements Serializable {

	private static final long serialVersionUID = -8517197782114770610L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String photoUrl;

	private String type;
	
	private String nickname;
	
	private long userId;
	
	public ThreePartInfoDmo() {
		super();
	}
	public ThreePartInfoDmo(Long id, String photoUrl, String type, String nickname, long userId) {
		super();
		this.id = id;
		this.photoUrl = photoUrl;
		this.type = type;
		this.nickname = nickname;
		this.userId = userId;
	}
	public Long getId() {
		return id;
	}
	public String getNickname() {
		return nickname;
	}
	public String getPhotoUrl() {
		return photoUrl;
	}
	public String getType() {
		return type;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	
}
