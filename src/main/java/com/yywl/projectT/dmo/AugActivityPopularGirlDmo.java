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
 * 人气女神
 * @author jphil
 *
 */
@Entity
@Table(name="aug_activity_popular_girl")
public class AugActivityPopularGirlDmo implements Serializable{

	private static final long serialVersionUID = -4384829244047305874L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserDmo user;
	
	private int point;

	public AugActivityPopularGirlDmo(Long id, UserDmo user, int point) {
		super();
		this.id = id;
		this.user = user;
		this.point = point;
	}

	public AugActivityPopularGirlDmo() {
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

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}
	
}
