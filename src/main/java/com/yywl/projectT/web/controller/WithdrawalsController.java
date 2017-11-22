package com.yywl.projectT.web.controller;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.DateFactory;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.bo.WithdrawalsBo;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dao.WithdrawalsDao;
import com.yywl.projectT.dmo.UserDmo;

@RestController
@RequestMapping("withdrawals")
public class WithdrawalsController {
	@Autowired
	UserBo userBo;
	@Autowired
	WithdrawalsBo withdrawalsBo;
	@Autowired
	WithdrawalsDao withdrawalsDao;
	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	JdbcDao jdbcDao;
	
	@Autowired
	UserDao userDao;

	private final static Log log = LogFactory.getLog(WithdrawalsController.class);

	@PostMapping("byAlipayV2")
	public Callable<ResultModel> byAlipayV2(long userId, String token, String alipayAccount, int money, String imei) {
		return () -> {
			if (StringUtils.isEmpty(imei)) {
				throw new Exception("请打开获取imei权限");
			}
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			if (StringUtils.isEmpty(user.getIdCard())) {
				throw new Exception("请实名认证");
			}
			long count=userDao.countByIdCard(user.getIdCard());
			if (count>1) {
				log.error("身份证号码："+user.getIdCard());
				throw new Exception("身份证重复");
			}
			count = this.roomMemberDao.countByMember_IdAndRoom_StateIn(userId, new Integer[] {
					 ActivityStates.待评价.ordinal(), ActivityStates.已结束.ordinal() });
			if (count < 1) {
				return new ResultModel(false, "至少参加过一次活动", null);
			}
			count = this.jdbcDao.countWithdrawalsByUser_IdAndImeiAndCreateTimeBetween(userId, imei,
					DateFactory.getTodayStartTime(), DateFactory.getTodayEndTime());
			if (count > 0) {
				log.error("活动期间，一天只能提现一次");
				return new ResultModel(false, "活动期间，一天只能提现一次", null);
			}
			this.withdrawalsBo.saveV2(user, alipayAccount, money,imei);
			return new ResultModel();
		};
	}
}
