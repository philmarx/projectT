package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "oct_activity_all_prize_money")
public class OctActivityAllPrizeMoneyDmo implements Serializable {

	private static final long serialVersionUID = -1251183081336453407L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private int allMoney;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getAllMoney() {
		return allMoney;
	}

	public void setAllMoney(int allMoney) {
		this.allMoney = allMoney;
	}

	public OctActivityAllPrizeMoneyDmo(Integer id, int allMoney) {
		super();
		this.id = id;
		this.allMoney = allMoney;
	}

	public OctActivityAllPrizeMoneyDmo() {
		super();
	}
	
	
}
