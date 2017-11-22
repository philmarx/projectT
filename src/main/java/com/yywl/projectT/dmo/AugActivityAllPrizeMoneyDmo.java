package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 金额总数
 * @author jphil
 *
 */
@Table(name="aug_activity_all_prize_money")
@Entity
public class AugActivityAllPrizeMoneyDmo implements Serializable{

	private static final long serialVersionUID = 5054643865874293491L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private int allMoney;
	
	private int joinMember;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getAllMoney() {
		return allMoney;
	}

	public void setAllMoney(int allMoney) {
		this.allMoney = allMoney;
	}

	public int getJoinMember() {
		return joinMember;
	}

	public void setJoinMember(int joinMember) {
		this.joinMember = joinMember;
	}

	public AugActivityAllPrizeMoneyDmo() {
		super();
	}

	public AugActivityAllPrizeMoneyDmo(Long id, int allMoney, int joinMember) {
		super();
		this.id = id;
		this.allMoney = allMoney;
		this.joinMember = joinMember;
	}
	
	
}
