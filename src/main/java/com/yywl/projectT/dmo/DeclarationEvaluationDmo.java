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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "declaration_evaluation")
public class DeclarationEvaluationDmo implements Serializable {

	private static final long serialVersionUID = 6020312336199872727L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "declaration_id")
	@JsonIgnore
	private DeclarationDmo declaration;

	@ManyToOne
	@JoinColumn(name = "sender_id",nullable=true)
	private UserDmo sender;
	@ManyToOne
	@JoinColumn(name = "receiver_id",nullable=true)
	private UserDmo receiver;
	private String content;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DeclarationDmo getDeclaration() {
		return declaration;
	}

	public void setDeclaration(DeclarationDmo declaration) {
		this.declaration = declaration;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public DeclarationEvaluationDmo() {
		super();
	}

}
