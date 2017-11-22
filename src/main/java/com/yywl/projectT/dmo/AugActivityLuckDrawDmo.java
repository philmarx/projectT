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
/**
 * 抽奖
 *
 */
@Table(name = "aug_activity_luck_draw")
@Entity
public class AugActivityLuckDrawDmo implements Serializable {

	private static final long serialVersionUID = -2578938644764904347L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDmo user;

	private int money;

	private int frequency;

	@Temporal(TemporalType.TIMESTAMP)
	private Date drawTime;
	
	public Date getDrawTime() {
		return drawTime;
	}

	public void setDrawTime(Date drawTime) {
		this.drawTime = drawTime;
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

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public AugActivityLuckDrawDmo() {
		super();
	}

	public AugActivityLuckDrawDmo(Long id, UserDmo user, int money, int frequency,Date drawTime) {
		super();
		this.drawTime=drawTime;
		this.id = id;
		this.user = user;
		this.money = money;
		this.frequency = frequency;
	}
	
	
}
