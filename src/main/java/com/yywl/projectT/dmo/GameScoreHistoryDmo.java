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
@Table(name="game_score_history")
public class GameScoreHistoryDmo implements Serializable{

	private static final long serialVersionUID = -3234449587147799640L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDmo user;

	@ManyToOne
	@JoinColumn(name = "game_id")
	private GameDmo game;

	private int count;
	
	private int score;

	@Temporal(TemporalType.DATE)
	private Date createDate;

	private int ranking;
	public GameScoreHistoryDmo() {
		super();
	}
	
	public GameScoreHistoryDmo(Long id, UserDmo user, GameDmo game, int score, Date createDate, int ranking,int count) {
		super();
		this.id = id;
		this.user = user;
		this.game = game;
		this.score = score;
		this.createDate = createDate;
		this.ranking = ranking;
		this.count=count;
	}

	public int getCount() {
		return count;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public GameDmo getGame() {
		return game;
	}

	public Long getId() {
		return id;
	}

	public int getRanking() {
		return ranking;
	}

	public int getScore() {
		return score;
	}

	public UserDmo getUser() {
		return user;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setGame(GameDmo game) {
		this.game = game;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setUser(UserDmo user) {
		this.user = user;
	}
	
}
