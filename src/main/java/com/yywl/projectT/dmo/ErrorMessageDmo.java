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
@Table(name="error_message")
public class ErrorMessageDmo implements Serializable{

	private static final long serialVersionUID = -3995062606091880857L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Long userId;
	
	private String platform;
	
	private String version;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	
	private String message;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ErrorMessageDmo(Long id, long userId, String platform, String version, Date sendTime, String message) {
		super();
		this.id = id;
		this.userId = userId;
		this.platform = platform;
		this.version = version;
		this.sendTime = sendTime;
		this.message = message;
	}

	public ErrorMessageDmo() {
		super();
	}
	
	
	
}
