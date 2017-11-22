package com.yywl.projectT.vo;

public class OrderCircleVo {
	private Long id;
	private String name;
	private int experience;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getExperience() {
		return experience;
	}
	public void setExperience(int experience) {
		this.experience = experience;
	}
	public OrderCircleVo() {
		super();
	}
	public OrderCircleVo(Long id, String name, int experience) {
		super();
		this.id = id;
		this.name = name;
		this.experience = experience;
	}
	
	
}
