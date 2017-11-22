package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="sept_activity_movie")
@Entity
public class SeptActivityMovieDmo implements Serializable{

	private static final long serialVersionUID = -7786627497494324118L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private int vote;
	
	private String movieName;
	
	private boolean isEffective;

	public SeptActivityMovieDmo() {
		super();
	}

	public SeptActivityMovieDmo(Integer id, int vote, String movieName, boolean isEffective) {
		super();
		this.id = id;
		this.vote = vote;
		this.movieName = movieName;
		this.isEffective = isEffective;
	}

	public Integer getId() {
		return id;
	}

	public String getMovieName() {
		return movieName;
	}

	public int getVote() {
		return vote;
	}

	public boolean isEffective() {
		return isEffective;
	}

	public void setEffective(boolean isEffective) {
		this.isEffective = isEffective;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public void setVote(int vote) {
		this.vote = vote;
	}
	
	
}
