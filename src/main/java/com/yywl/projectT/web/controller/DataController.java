package com.yywl.projectT.web.controller;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.OctRoomDao;
import com.yywl.projectT.dao.OctRoomUserDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dmo.OctRoomUserDmo;
import com.yywl.projectT.dmo.RoomDmo;

@RequestMapping("data")
@RestController
public class DataController {

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	OctRoomUserDao octRoomUserDao;

	@Autowired
	FriendDao friendDao;

	@Autowired
	RoomDao roomDao;

	@Autowired
	JdbcDao jdbcDao;
	
	@Autowired
	OctRoomDao octRoomDao;

	@Transactional(rollbackOn = Exception.class)
	public Object update(String secret) {
		if (!Keys.SECRET.equals(secret)) {
			return new ResultModel(false);
		}
		long start=System.currentTimeMillis();
		RoomDmo room=this.roomDao.findOne(1000000000882L);
		List<Long> memberIds = this.jdbcDao.findMember_IdsByRoom_Id(room.getId());
		int bounty=0,managerBounty = 0;
		if (memberIds.size()<4) {
			bounty=500;
		}else if(memberIds.size()==4) {
			bounty=1000;
		}else {
			bounty=1000;
			managerBounty=200*memberIds.size();
		}
		for (Long memberId : memberIds) {
			long friendCount=this.friendDao.countByOwner_IdAndFriend_IdIn(memberId, memberIds);
			OctRoomUserDmo octRoomUserdmo=new OctRoomUserDmo(null, room.getId(), memberId, friendCount==0);
			if (memberId.longValue()==room.getManager().getId().longValue()&&memberIds.size()>4) {
				octRoomUserdmo.setBounty(managerBounty);
			}else {
				octRoomUserdmo.setBounty(bounty);
			}
			octRoomUserdmo.setReason("审核中");
			this.octRoomUserDao.save(octRoomUserdmo);
		}
		return System.currentTimeMillis() - start;
	}
}
