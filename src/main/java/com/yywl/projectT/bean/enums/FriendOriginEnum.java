package com.yywl.projectT.bean.enums;

public enum FriendOriginEnum {
	活动评价(0, "活动评价"), 新人邀请(1, "新人邀请");
	private Integer id;
	private String name;
	public Integer getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	private FriendOriginEnum(Integer id, String value) {
		this.id = id;
		this.name = value;
	}

	

}
