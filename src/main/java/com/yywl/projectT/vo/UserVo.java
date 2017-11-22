package com.yywl.projectT.vo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.yywl.projectT.bean.Formatter;

public class UserVo {
	private Long id;
	
	private String nickname;
	
	private String avatarSignature;
	
	private Set<String> labels=new HashSet<>();

	/**
	 * @see JdbcDao.findFriendsToEvalute
	 * 如果为0，表示还没有被评价。
	 */
	private int point;
	
	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public Set<String> getLabels() {
		return labels;
	}

	@SuppressWarnings("unchecked")
	public void setLabels(String labels) {
		if (!StringUtils.isEmpty(labels)) {
			this.labels = Formatter.gson.fromJson(labels, Set.class);
		}
	}
	public String getAvatarSignature() {
		return avatarSignature;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

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

	public UserVo() {
		super();
	}

	public UserVo(Long id, String nickname) {
		super();
		this.id = id;
		this.nickname = nickname;
	}

	public UserVo(Long id, String nickname, String avatarSignature) {
		super();
		this.id = id;
		this.nickname = nickname;
		this.avatarSignature = avatarSignature;
	}

}
