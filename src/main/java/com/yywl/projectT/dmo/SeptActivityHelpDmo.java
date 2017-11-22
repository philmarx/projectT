package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "sept_activity_help")
public class SeptActivityHelpDmo implements Serializable {

	private static final long serialVersionUID = 3308800701592712065L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "user_id")
	@ManyToOne
	private UserDmo user;

	@JoinColumn(name = "helper_id")
	@ManyToOne
	private UserDmo helper;

	private String ip;

	private String movieName;

	public SeptActivityHelpDmo() {
		super();
	}

	public UserDmo getHelper() {
		return helper;
	}

	public Long getId() {
		return id;
	}

	public String getIp() {
		return ip;
	}

	public String getMovieName() {
		return movieName;
	}

	public UserDmo getUser() {
		return user;
	}

	public void setHelper(UserDmo helper) {
		this.helper = helper;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public void setUser(UserDmo user) {
		this.user = user;
	}

	public SeptActivityHelpDmo(Long id, UserDmo user, UserDmo helper, String ip, String movieName) {
		super();
		this.id = id;
		this.user = user;
		this.helper = helper;
		this.ip = ip;
		this.movieName = movieName;
	}


}
