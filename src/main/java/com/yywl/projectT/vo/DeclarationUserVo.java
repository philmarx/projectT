package com.yywl.projectT.vo;

public class DeclarationUserVo {
	private Long id;
	
	private String nickname;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public DeclarationUserVo(Long id, String nickname) {
		super();
		this.id = id;
		this.nickname = nickname;
	}

	public DeclarationUserVo() {
		super();
	}
	
	
}
