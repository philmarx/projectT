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
@Table(name = "user_circle")
public class UserCircleDmo implements Serializable {

	private static final long serialVersionUID = 489914901215083570L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user")
	private UserDmo user;

	@ManyToOne
	@JoinColumn(name = "circle")
	private CircleDmo circle;
	
	/**
	 * 是否签到过
	 */
	private boolean isSign;
	
	
	public boolean isSign() {
		return isSign;
	}

	public void setSign(boolean isSign) {
		this.isSign = isSign;
	}

	/**
	 * 加入活动的次数
	 */
	private int joinCount=0;
	
	/**
	 * 创建活动的次数
	 */
	private int createCount=0;
	/**
	 * 经验值
	 */
	private int experience=0;
	
	public UserCircleDmo() {
		super();
	}

	public CircleDmo getCircle() {
		return circle;
	}

	public int getCreateCount() {
		return createCount;
	}

	public int getExperience() {
		return experience;
	}

	public Long getId() {
		return id;
	}

	public int getJoinCount() {
		return joinCount;
	}

	public UserDmo getUser() {
		return user;
	}

	public void setCircle(CircleDmo circle) {
		this.circle = circle;
	}

	public void setCreateCount(int createCount) {
		this.createCount = createCount;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setJoinCount(int joinCount) {
		this.joinCount = joinCount;
	}

	public void setUser(UserDmo user) {
		this.user = user;
	}


}
