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

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="transaction_details")
public class TransactionDetailsDmo implements Serializable{

	private static final long serialVersionUID = -7186433873604037609L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name="user")
	@ManyToOne
	private UserDmo user;
	
	private int money;
	
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date createTime;
	
	private boolean isCalc=true;
	
	public TransactionDetailsDmo() {
		super();
	}

	public Date getCreateTime() {
		return createTime;
	}

	public String getDescription() {
		return description;
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

	public boolean isCalc() {
		return isCalc;
	}

	public void setCalc(boolean isCalc) {
		this.isCalc = isCalc;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setDescription(String description) {
		this.description = description;
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
