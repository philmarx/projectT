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

/**
 * 小道具
 * 
 * @author jphil
 *
 */
@Entity
@Table(name = "prop")
public class PropDmo implements Serializable {

	private static final long serialVersionUID = -7350948423925385444L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	/**
	 * 小纸条数量
	 */
	private int noteCount;

	/**
	 * 标签清除卡数量
	 */
	private int labelClearCount;

	/**
	 * 改名卡数量
	 */
	private int changeNicknameCount;

	/**
	 * 入场券
	 */
	private int roomTicket;
	
	/**
	 * 剩余的观影券
	 */
	private int remainMovieTicket;

	/**
	 * 同伴卡
	 */
	private int friendCard;

	/**
	 * 会员失效时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date vipExpireDate;
	/**
	 * 补签卡数量
	 */
	private int signCount;
	
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDmo user;

	public PropDmo() {
		super();
	}

	public PropDmo(Long id, int noteCount, int labelClearCount, int changeNicknameCount,int signCount, Date vipExpireDate,
			UserDmo user) {
		super();
		this.id = id;
		this.signCount=signCount;
		this.noteCount = noteCount;
		this.labelClearCount = labelClearCount;
		this.changeNicknameCount = changeNicknameCount;
		this.vipExpireDate = vipExpireDate;
		this.user = user;
	}

	public int getChangeNicknameCount() {
		return changeNicknameCount;
	}

	public int getFriendCard() {
		return friendCard;
	}
	
	public Long getId() {
		return id;
	}
	
	public int getLabelClearCount() {
		return labelClearCount;
	}

	public int getNoteCount() {
		return noteCount;
	}

	public int getRemainMovieTicket() {
		return remainMovieTicket;
	}

	public int getRoomTicket() {
		return roomTicket;
	}

	public int getSignCount() {
		return signCount;
	}

	public UserDmo getUser() {
		return user;
	}

	public Date getVipExpireDate() {
		return vipExpireDate;
	}

	public void setChangeNicknameCount(int changeNicknameCount) {
		this.changeNicknameCount = changeNicknameCount;
	}

	public void setFriendCard(int friendCard) {
		this.friendCard = friendCard;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLabelClearCount(int labelClearCount) {
		this.labelClearCount = labelClearCount;
	}

	public void setNoteCount(int noteCount) {
		this.noteCount = noteCount;
	}

	public void setRemainMovieTicket(int remainMovieTicket) {
		this.remainMovieTicket = remainMovieTicket;
	}

	public void setRoomTicket(int roomTicket) {
		this.roomTicket = roomTicket;
	}

	public void setSignCount(int signCount) {
		this.signCount = signCount;
	}

	public void setUser(UserDmo user) {
		this.user = user;
	}

	public void setVipExpireDate(Date vipExpireDate) {
		this.vipExpireDate = vipExpireDate;
	}

}
