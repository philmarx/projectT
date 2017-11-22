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
@Table(name = "note")
public class NoteDmo implements Serializable {

	private static final long serialVersionUID = -7742806280988565817L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "sender_id")
	private UserDmo sender;

	@ManyToOne
	@JoinColumn(name = "receiver_id")
	private UserDmo receiver;

	private int state;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime=new Date();

	private String content;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserDmo getSender() {
		return sender;
	}

	public void setSender(UserDmo sender) {
		this.sender = sender;
	}

	public UserDmo getReceiver() {
		return receiver;
	}

	public void setReceiver(UserDmo receiver) {
		this.receiver = receiver;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public NoteDmo(Long id, UserDmo sender, UserDmo receiver, int state, Date createTime, String content) {
		super();
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.state = state;
		this.createTime = createTime;
		this.content = content;
	}

	public NoteDmo() {
		super();
	}

	public NoteDmo(Long id) {
		super();
		this.id = id;
	}

}
