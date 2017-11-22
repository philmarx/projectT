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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "announcement")
public class AnnouncementDmo implements Serializable {
	private static final long serialVersionUID = 8873500216690416689L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String url;

	private String message;

	private boolean isForce;
	
	private boolean isEffective;

	private String photoUrl;
	
	@Temporal(TemporalType.DATE)
	private Date expiryDate;

	public AnnouncementDmo() {
		super();
	}

	public AnnouncementDmo(Integer id, String url, String message, boolean isForce, boolean isEffective,
			String photoUrl, Date expiryDate) {
		super();
		this.id = id;
		this.url = url;
		this.message = message;
		this.isForce = isForce;
		this.isEffective = isEffective;
		this.photoUrl = photoUrl;
		this.expiryDate = expiryDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public Integer getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public String getUrl() {
		return url;
	}
	
	@JsonIgnore
	public boolean isEffective() {
		return isEffective;
	}

	public boolean isForce() {
		return isForce;
	}

	public void setEffective(boolean isEffective) {
		this.isEffective = isEffective;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public void setForce(boolean isForce) {
		this.isForce = isForce;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
