package com.yywl.projectT.web.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bo.OctRoomBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.OctActivityAllPrizeMoneyDao;
import com.yywl.projectT.dmo.OctActivityAllPrizeMoneyDmo;

@RestController
@RequestMapping("oct")
public class OctActivityController {
	@Autowired
	JdbcDao jdbcDao;

	@PostMapping("findRewardUsers")
	public Callable<ResultModel> findRewardUsers(int size) {
		return () -> {
			List<Map<String, Object>> list = this.jdbcDao.findOctRewardUsers(size);
			return new ResultModel(true, "", list);
		};
	}

	@Autowired
	UserBo userBo;

	@Autowired
	OctActivityAllPrizeMoneyDao octActivityAllPrizeMoneyDao;

	@PostMapping("findAllPrizeInfo")
	public Callable<ResultModel> findAllPrizeInfo() {
		return () -> {
			OctActivityAllPrizeMoneyDmo dmo = this.octActivityAllPrizeMoneyDao.findOne(1);
			return new ResultModel(true, null, dmo.getAllMoney());
		};
	}

	@PostMapping("findMyJoinedRoom")
	public Callable<ResultModel> findMyJoinedRoom(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<Map<String, Object>> list = this.jdbcDao.findMyJoinedRoom(userId);
			return new ResultModel(true, null, list);
		};
	}

	@Autowired
	OctRoomBo octRoomBo;

	@PostMapping("getBounty")
	public Callable<ResultModel> getBounty(long userId, String token, long id) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			this.octRoomBo.getBounty(id);
			return new ResultModel(true, "领取成功", this.jdbcDao.findMyJoinedRoom(userId));
		};
	}
}
