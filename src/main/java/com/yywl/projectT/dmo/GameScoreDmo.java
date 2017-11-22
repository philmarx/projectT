package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "game_score")
public class GameScoreDmo implements Serializable {

	private static final long serialVersionUID = -2436695536342684861L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDmo user;

	@ManyToOne
	@JoinColumn(name = "game_id")
	private GameDmo game;

	private int score;
	/**
	 * 活动次数
	 */
	private int count=1;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public GameScoreDmo() {
		super();
	}

	public UserDmo getUser() {
		return user;
	}

	public void setUser(UserDmo user) {
		this.user = user;
	}

	public GameDmo getGame() {
		return game;
	}

	public void setGame(GameDmo game) {
		this.game = game;
	}

	public GameScoreDmo(Long id, UserDmo user, GameDmo game, int score, int count) {
		super();
		this.id = id;
		this.user = user;
		this.game = game;
		this.score = score;
		this.count = count;
	}


}
