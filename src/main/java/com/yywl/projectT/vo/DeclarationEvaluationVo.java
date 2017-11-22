package com.yywl.projectT.vo;

public class DeclarationEvaluationVo {
	private Long id;
	private DeclarationUserVo sender;
	private DeclarationUserVo receiver;
	private String content;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public DeclarationUserVo getSender() {
		return sender;
	}
	public void setSender(DeclarationUserVo sender) {
		this.sender = sender;
	}
	public DeclarationUserVo getReceiver() {
		return receiver;
	}
	public void setReceiver(DeclarationUserVo receiver) {
		this.receiver = receiver;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public DeclarationEvaluationVo() {
		super();
	}
	public DeclarationEvaluationVo(Long id, DeclarationUserVo sender, DeclarationUserVo receiver, String content) {
		super();
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.content = content;
	}
	
	
}
