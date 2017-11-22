package com.yywl.projectT.vo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.dao.JdbcDao;

/**
 * 活动结束后评价的用户列表
 * 
 * @author jphil
 *
 */
public class FriendForEvaluatingVo2  {
	
	private boolean isSigned = true;

	private Long friendId;

	private String nickname;

	private String avatarSignature;

	private Set<String> labels = new HashSet<>();

	/**
	 * @see JdbcDao.findFriendsToEvalute 如果为0，表示还没有被评价。
	 */
	private int friendPoint;

	public FriendForEvaluatingVo2(Long friendId, String nickname, String avatarSignature) {
		super();
		this.friendId = friendId;
		this.nickname = nickname;
		this.avatarSignature = avatarSignature;
	}

	public boolean isSigned() {
		return isSigned;
	}

	public void setSigned(boolean isSigned) {
		this.isSigned = isSigned;
	}

	public Long getFriendId() {
		return friendId;
	}

	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatarSignature() {
		return avatarSignature;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

	public Set<String> getLabels() {
		return labels;
	}

	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

	public int getFriendPoint() {
		return friendPoint;
	}

	public void setFriendPoint(int friendPoint) {
		this.friendPoint = friendPoint;
	}
	@SuppressWarnings("unchecked")
	public void setLabels(String labels) {
		if (!StringUtils.isEmpty(labels)) {
			this.labels = Formatter.gson.fromJson(labels, Set.class);
		}
	}
}
