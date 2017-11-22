package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="sept_activity_luck_draw")
public class SeptActivityLuckDrawDmo implements Serializable{

	private static final long serialVersionUID = 3053176389959047069L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserDmo user;
	
	private int money;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date drawTime;

	public SeptActivityLuckDrawDmo() {
		super();
	}

	public SeptActivityLuckDrawDmo(Long id, UserDmo user, int money, Date drawTime) {
		super();
		this.id = id;
		this.user = user;
		this.money = money;
		this.drawTime = drawTime;
	}

	public Date getDrawTime() {
		return drawTime;
	}

	public Long getId() {
		return id;
	}

	public int getMoney() {
		return money;
	}

	public UserDmo getUser() {
		return user;
	}

	public void setDrawTime(Date drawTime) {
		this.drawTime = drawTime;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void setUser(UserDmo user) {
		this.user = user;
	}
	
	
}
