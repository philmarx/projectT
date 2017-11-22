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
/**
 * 我们组织的活动，由微信展示
 *
 */
@Entity
@Table(name="activity")
public class ActivityDmo implements Serializable {

	private static final long serialVersionUID = 110474373897657336L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String url;

	private String photoUrl;

	@Temporal(TemporalType.TIMESTAMP)
	private Date beginTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

	private boolean enable;
	
	private String message;
	
	private String shareUrl;
	
	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public ActivityDmo() {
		super();
	}

	public ActivityDmo(Long id, String name, String url, String photoUrl, Date beginTime, Date endTime, boolean enable,
			String message,String shareUrl) {
		super();
		this.shareUrl=shareUrl;
		this.id = id;
		this.name = name;
		this.url = url;
		this.photoUrl = photoUrl;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.enable = enable;
		this.message = message;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getName() {
		return name;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public String getUrl() {
		return url;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
