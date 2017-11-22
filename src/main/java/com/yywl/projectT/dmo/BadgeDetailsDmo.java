package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
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
 * 徽章使用记录表
 * @author jphil
 *
 */
@Entity
@Table(name="badge_details")
public class BadgeDetailsDmo implements Serializable{

	private static final long serialVersionUID = -2303913100136485098L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserDmo user;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	private String description;
	
	@Column(name="out_trade_no",nullable=true)
	private String outTradeNo;
	
	@Column(name="trade_no",nullable=true)
	private String tradeNo;
	/**
	 * 购买叶子话费的金额，单位为分
	 */
	private int spendMoney;
	
	
	public int getSpendMoney() {
		return spendMoney;
	}

	public void setSpendMoney(int spendMoney) {
		this.spendMoney = spendMoney;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	/**
	 * 徽章增加或减少的数量
	 */
	private int badge;

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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getBadge() {
		return badge;
	}

	public void setBadge(int badge) {
		this.badge = badge;
	}

	public BadgeDetailsDmo() {
		super();
	}

	public BadgeDetailsDmo(Long id, UserDmo user, Date createTime, String description, int badge) {
		super();
		this.id = id;
		this.user = user;
		this.createTime = createTime;
		this.description = description;
		this.badge = badge;
	}
	
	
}
