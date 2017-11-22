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
@Table(name = "withdrawals")
public class WithdrawalsDmo implements Serializable {

	private static final long serialVersionUID = -6534599170255015743L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "user")
	private UserDmo user;

	private String alipayAccount;

	private String imei;

	private int money;

	private long dealAdminId;

	public long getDealAdminId() {
		return dealAdminId;
	}

	public void setDealAdminId(long dealAdminId) {
		this.dealAdminId = dealAdminId;
	}

	private int state;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dealTime;

	private int dealMoney;

	public WithdrawalsDmo() {
		super();
	}

	public String getAlipayAccount() {
		return alipayAccount;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public int getDealMoney() {
		return dealMoney;
	}

	public Date getDealTime() {
		return dealTime;
	}

	public Long getId() {
		return id;
	}

	public int getMoney() {
		return money;
	}

	public int getState() {
		return state;
	}

	public UserDmo getUser() {
		return user;
	}

	public void setAlipayAccount(String alipayAccount) {
		this.alipayAccount = alipayAccount;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setDealMoney(int dealMoney) {
		this.dealMoney = dealMoney;
	}

	public void setDealTime(Date dealTime) {
		this.dealTime = dealTime;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setUser(UserDmo user) {
		this.user = user;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public WithdrawalsDmo(Long id, UserDmo user, String alipayAccount, int money, int state, Date createTime,
			Date dealTime, int dealMoney) {
		super();
		this.id = id;
		this.user = user;
		this.alipayAccount = alipayAccount;
		this.money = money;
		this.state = state;
		this.createTime = createTime;
		this.dealTime = dealTime;
		this.dealMoney = dealMoney;
	}

}
