package com.yywl.projectT.bo;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.OctRoomUserEnum;
import com.yywl.projectT.dao.OctActivityAllPrizeMoneyDao;
import com.yywl.projectT.dao.OctRoomUserDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.OctActivityAllPrizeMoneyDmo;
import com.yywl.projectT.dmo.OctRoomUserDmo;
import com.yywl.projectT.dmo.TransactionDetailsDmo;
import com.yywl.projectT.dmo.UserDmo;

import io.rong.messages.TxtMessage;

@Service
@Transactional(rollbackOn = Throwable.class)
public class OctRoomBo {
	@Autowired
	OctRoomUserDao octRoomUserDao;

	@Autowired
	UserDao userDao;

	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	private static final Log log = LogFactory.getLog(OctRoomBo.class);

	@Autowired
	RongCloudBean rongCloud;

	@Autowired
	OctActivityAllPrizeMoneyDao octActivityAllPrizeMoneyDao;

	@Autowired
	JdbcTemplate jdbc;

	public void getBounty(long id) throws Exception {
		OctRoomUserDmo octRoomUserDmo = this.octRoomUserDao.findOne(id);
		if (octRoomUserDmo == null) {
			log.error(id);
			throw new Exception("数据不存在");
		}
		if (octRoomUserDmo.getState() != OctRoomUserEnum.通过未领取.ordinal()) {
			throw new Exception("领取失败");
		}
		int count = jdbc.queryForObject(
				"select count(1) from oct_room_user where is_foul=1 and user_id=? ", Integer.class,
				octRoomUserDmo.getUserId());
		if (count > 0) {
			throw new Exception("您有非法记录，领取失败。");
		}
		octRoomUserDmo.setState(OctRoomUserEnum.通过已领取.ordinal());
		octRoomUserDmo.setReason("已领取");
		this.octRoomUserDao.save(octRoomUserDmo);
		UserDmo userDmo = this.userDao.findOne(octRoomUserDmo.getUserId());
		userDmo.setAmount(userDmo.getAmount() + octRoomUserDmo.getBounty());
		this.userDao.save(userDmo);
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setCalc(true);
		tran.setCreateTime(new Date());
		tran.setDescription("神秘宝藏活动奖金领取");
		tran.setMoney(octRoomUserDmo.getBounty());
		tran.setUser(userDmo);
		transactionDetailsDao.save(tran);
		OctActivityAllPrizeMoneyDmo allPrize = this.octActivityAllPrizeMoneyDao.findOne(1);
		allPrize.setAllMoney(allPrize.getAllMoney() - octRoomUserDmo.getBounty() * 2);
		this.octActivityAllPrizeMoneyDao.save(allPrize);
		new Thread(() -> {
			double d = octRoomUserDmo.getBounty() / 100.0;
			try {
				rongCloud.sendSystemMessage(userDmo.getId(),
						new TxtMessage("神秘宝藏活动奖金领取" + String.format("%.2f", d) + "元成功", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}
}
