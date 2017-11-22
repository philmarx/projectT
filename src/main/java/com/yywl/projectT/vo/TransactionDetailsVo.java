package com.yywl.projectT.vo;

import java.util.Date;

public class TransactionDetailsVo {
	private String description;
	private int money;
	
	private Date createTime;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public TransactionDetailsVo() {
		super();
	}

	public TransactionDetailsVo(String description, int money, Date createTime) {
		super();
		this.description = description;
		this.money = money;
		this.createTime = createTime;
	}
	
	
}
