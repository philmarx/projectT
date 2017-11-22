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
 * 称号
 *
 */
@Entity
@Table(name="activity_title")
public class ActivityTitleDmo implements Serializable {

	private static final long serialVersionUID = -7798580128256267683L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserDmo user;
	private String titleName;
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
	public String getTitleName() {
		return titleName;
	}
	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}
	public ActivityTitleDmo() {
		super();
	}
	public ActivityTitleDmo(Long id, UserDmo user, String titleName) {
		super();
		this.id = id;
		this.user = user;
		this.titleName = titleName;
	}
	
	
}
