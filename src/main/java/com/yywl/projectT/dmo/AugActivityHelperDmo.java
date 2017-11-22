package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
/**
 * 助力
 */
@Entity
@Table(name="aug_activity_helper")
public class AugActivityHelperDmo implements Serializable{

	private static final long serialVersionUID = -8981587554197213768L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	
	/**
	 * 助力的人
	 */
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserDmo user;
	
	/**
	 * 被助力的人
	 */
	@ManyToOne
	@JoinColumn(name="helper_id")
	private UserDmo helper;
	
	/**
	 * 助力者的id
	 */
	private String ip;

	public AugActivityHelperDmo(Long id, UserDmo user, UserDmo helper, String ip) {
		super();
		this.id = id;
		this.user = user;
		this.helper = helper;
		this.ip = ip;
	}

	public AugActivityHelperDmo() {
		super();
	}

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

	public UserDmo getHelper() {
		return helper;
	}

	public void setHelper(UserDmo helper) {
		this.helper = helper;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
}
