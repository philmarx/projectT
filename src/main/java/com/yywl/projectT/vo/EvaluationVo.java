package com.yywl.projectT.vo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvaluationVo {
	private long friendId;

	/**
	 * 好友好感度评价
	 */
	private int point = 5;

	private Set<String> labels=new HashSet<>();

	/**
	 * 获取活动表现评价
	 */
	private int roomEvaluationPoint=5;
	
	
	public int getRoomEvaluationPoint() {
		return roomEvaluationPoint;
	}

	public void setRoomEvaluationPoint(int roomEvaluationPoint) {
		this.roomEvaluationPoint = roomEvaluationPoint;
	}

	public EvaluationVo() {
		super();
	}

	public EvaluationVo(long friendId, int point, List<String> labels) {
		super();
		this.friendId = friendId;
		this.point = point;
		this.getLabels().addAll(labels);
	}

	public long getFriendId() {
		return friendId;
	}

	public Set<String> getLabels() {
		return labels;
	}

	public int getPoint() {
		return point;
	}


	public void setFriendId(long friendId) {
		this.friendId = friendId;
	}

	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

	public void setPoint(int point) {
		this.point = point;
	}

}
