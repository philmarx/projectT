package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name="sept_activity_all_prize_money")
public class SeptActivityAllPrizeMoneyDmo implements Serializable{

	private static final long serialVersionUID = 5125708782409378926L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private int money;
	
	private int movieTicket;
	
	private int times;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public int getMovieTicket() {
		return movieTicket;
	}

	public void setMovieTicket(int movieTicket) {
		this.movieTicket = movieTicket;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public SeptActivityAllPrizeMoneyDmo() {
		super();
	}

	public SeptActivityAllPrizeMoneyDmo(Long id, int money, int movieTicket, int times) {
		super();
		this.id = id;
		this.money = money;
		this.movieTicket = movieTicket;
		this.times = times;
	}
	
	

}
