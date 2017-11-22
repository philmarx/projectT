package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "application")
public class ApplicationDmo implements Serializable {

	private static final long serialVersionUID = -1437808877041331214L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String version;

	/**
	 * 是否提醒
	 */
	private boolean isRemind;
	
	public boolean isRemind() {
		return isRemind;
	}

	public void setRemind(boolean isRemind) {
		this.isRemind = isRemind;
	}
	/**
	 * 下载地址
	 */
	private String downUrl;
	
	private String message;
	/**
	 * 是否强制升级
	 */
	private boolean isForce;
	
	@Column(name = "is_current")
	private Boolean isCurrent;

	private String platform;

	public ApplicationDmo() {
		super();
	}

	public String getDownUrl() {
		return downUrl;
	}

	public Integer getId() {
		return id;
	}

	public Boolean getIsCurrent() {
		return isCurrent;
	}

	public String getMessage() {
		return message;
	}

	public String getPlatform() {
		return platform;
	}
	
	public String getVersion() {
		return version;
	}

	public boolean isForce() {
		return isForce;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	public void setForce(boolean force) {
		this.isForce = force;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setIsCurrent(Boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
