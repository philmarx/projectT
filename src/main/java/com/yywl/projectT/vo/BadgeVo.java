package com.yywl.projectT.vo;

import java.util.Date;

public class BadgeVo {
	
	public static class UserVo{
		private Long id;
		
		private String nickname;

		public UserVo() {
			super();
		}

		public UserVo(Long id, String nickname) {
			super();
			this.id = id;
			this.nickname = nickname;
		}

		public Long getId() {
			return id;
		}

		public String getNickname() {
			return nickname;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		
		
		
	}
	private Long id;
	private UserVo user;
	private String description;
	private Date createTime;
	/**
	 * 叶子变化量
	 */
	private int variation;
	
	public BadgeVo() {
		super();
	}
	public BadgeVo(Long id, UserVo user, String description, Date createTime,int variation) {
		super();
		this.variation=variation;
		this.id = id;
		this.user = user;
		this.description = description;
		this.createTime = createTime;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public String getDescription() {
		return description;
	}
	public Long getId() {
		return id;
	}
	public UserVo getUser() {
		return user;
	}
	public int getVariation() {
		return variation;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setUser(UserVo user) {
		this.user = user;
	}
	public void setVariation(int variation) {
		this.variation = variation;
	}
	
}
