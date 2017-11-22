package com.yywl.projectT.bo;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.dao.BadgeDetailsDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserCircleDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.BadgeDetailsDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.UserCircleDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
public class BadgeBo {

	@Autowired
	private BadgeDetailsDao badgeDetailsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	UserCircleDao userCircleDao;

	/**
	 * 创建圈子消耗叶子
	 * 
	 * @param user
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void createCircle(UserDmo user) {
		BadgeDetailsDmo badgeDetailsDmo = new BadgeDetailsDmo(null, user, new Date(), "创建时圈子消耗",
				Keys.Circle.badgeSpend);
		badgeDetailsDao.save(badgeDetailsDmo);
		user.setBadge(user.getBadge() - Keys.Circle.badgeSpend);
		this.userDao.save(user);
	}

	/**
	 * 加入圈子消耗叶子
	 * 
	 * @param user
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void joinCircle(UserDmo user) {
		user.setBadge(user.getBadge() - Keys.Circle.badgeSpend);
		userDao.save(user);
		BadgeDetailsDmo badgeDetailsDmo = new BadgeDetailsDmo(null, user, new Date(), "加入圈子时消耗",
				0-Keys.Circle.badgeSpend);
		badgeDetailsDao.save(badgeDetailsDmo);
	}

	/**
	 * 使用叶子增加经验值
	 * 
	 * @param userCircleDmo
	 * @param badge
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void useBadgeInCircle(UserCircleDmo userCircleDmo, int badge) {
		UserDmo user = userCircleDmo.getUser();
		userCircleDmo.setExperience(userCircleDmo.getExperience() + badge * 10);
		this.userCircleDao.save(userCircleDmo);
		BadgeDetailsDmo badgeDetailsDmo = new BadgeDetailsDmo(null, user, new Date(),
				"使用" + badge + "个叶子添加经验值" + badge * 10 + "分", badge);
		this.badgeDetailsDao.save(badgeDetailsDmo);
		user.setBadge(user.getBadge() - badge);
		userDao.save(user);
	}

	/**
	 * vip会员在每场活动结束后增加一片叶子
	 * 
	 * @param user
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void vipRoomEndAdd(UserDmo user) {
		BadgeDetailsDmo badgeDetails = new BadgeDetailsDmo(null, user, new Date(), "vip会员每场活动获取一个",
				Keys.VIP_BADGE_ADD);
		this.badgeDetailsDao.save(badgeDetails);
		user.setBadge(user.getBadge() + Keys.VIP_BADGE_ADD);
		userDao.save(user);
	}
	
	private static Log log=LogFactory.getLog(BadgeBo.class);
	/**
	 * 喊话消耗叶子
	 * @param user
	 * @throws Exception 
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void declare(UserDmo user) throws Exception {
		if (user.getBadge() <= Keys.DECLARATION_BADGE) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - Keys.DECLARATION_BADGE);
		userDao.save(user);
		BadgeDetailsDmo badgeDetailsDmo = new BadgeDetailsDmo(null, user, new Date(), "喊话消耗",0- Keys.DECLARATION_BADGE);
		this.badgeDetailsDao.save(badgeDetailsDmo);
	}
	/**
	 * 注册赠送叶子
	 * @param user
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void register(UserDmo user) {
		user.setBadge(Keys.INIT_BADGE);
		this.userDao.save(user);
		BadgeDetailsDmo badgeDetailsDmo = new BadgeDetailsDmo(null, user, new Date(), "注册赠送", Keys.INIT_BADGE);
		badgeDetailsDao.save(badgeDetailsDmo);
	}

	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	/**
	 * 房间结束后添加叶子
	 * @param memberId
	 * @param addBadge
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void roomEndAddBadge(Long memberId, Long roomId,int addBadge) {
		RoomMemberDmo rm=this.roomMemberDao.findByMember_IdAndRoom_Id(memberId, roomId);
		if ((!rm.isSigned())||(!rm.isReady())) {
			return;
		}
		UserDmo user=rm.getMember();
		jdbcTemplate.update("update user set badge=? where id=?",user.getBadge()+addBadge,memberId);
		BadgeDetailsDmo badgeDetailsDmo = new BadgeDetailsDmo(null, user, new Date(), "【"+rm.getRoom().getName()+"】结束后赠送", addBadge);
		this.jdbcTemplate.update("update room_member set badge=? where id=?",addBadge,rm.getId());
		badgeDetailsDao.save(badgeDetailsDmo);
	}
	/**
	 * 购买叶子
	 * @param userId
	 * @param badge
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void buy(Long userId, int badge,String outTradeNo,String tradeNo,int spendMoney,String type) {
		boolean exists=this.badgeDetailsDao.existsByOutTradeNo(outTradeNo);
		if (exists) {
			return;
		}
		UserDmo user=this.userDao.findOne(userId);
		user.setBadge(user.getBadge()+badge);
		userDao.save(user);
		BadgeDetailsDmo badgeDetailsDmo = new BadgeDetailsDmo(null, user, new Date(), type+"购买"+badge+"个", badge);
		badgeDetailsDmo.setOutTradeNo(outTradeNo);
		badgeDetailsDmo.setTradeNo(tradeNo);
		badgeDetailsDmo.setSpendMoney(spendMoney);
		badgeDetailsDao.save(badgeDetailsDmo);
	}


}
