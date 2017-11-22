package com.yywl.projectT.vo;

import java.util.Date;
import java.util.List;

public class MyRoomVo extends RoomVo{
	protected boolean evaluated;
	
	public boolean isEvaluated() {
		return evaluated;
	}

	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}
	
	
	
	public MyRoomVo() {
		super();
	}

	public MyRoomVo(Long id, String name, String place, UserVo manager, boolean open, 
			Date beginTime, Date endTime, Date createTime, Integer state, boolean locked, GameVo game, int money,
			Integer joinMember, Integer joinManMember, Integer joinWomanMember, Integer memberCount,
			Integer manCount, Integer womanCount, String description, Double longitude, Double latitude,
			Date prepareTime, List<RoomMemberVo> joinMembers, String city,boolean evaluated) {
		super();
		this.id = id;
		this.name = name;
		this.place = place;
		this.manager = manager;
		this.open = open;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.createTime = createTime;
		this.state = state;
		this.locked = locked;
		this.game = game;
		this.money = money;
		this.joinMember = joinMember;
		this.joinManMember = joinManMember;
		this.joinWomanMember = joinWomanMember;
		this.memberCount = memberCount;
		this.manCount = manCount;
		this.womanCount = womanCount;
		this.description = description;
		this.longitude = longitude;
		this.latitude = latitude;
		this.prepareTime = prepareTime;
		this.joinMembers = joinMembers;
		this.city = city;
		this.evaluated=evaluated;
	}
}
