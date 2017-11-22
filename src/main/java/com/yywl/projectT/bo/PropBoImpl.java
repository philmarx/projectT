package com.yywl.projectT.bo;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.bean.enums.PropType;
import com.yywl.projectT.dao.BadgeDetailsDao;
import com.yywl.projectT.dao.PropDao;
import com.yywl.projectT.dao.PropOrderDao;
import com.yywl.projectT.dao.PropTypeDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.BadgeDetailsDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.PropOrderDmo;
import com.yywl.projectT.dmo.PropTypeDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
@Transactional(rollbackOn = Exception.class)
public class PropBoImpl implements PropBo {
	private static final Log log = LogFactory.getLog(PropBoImpl.class);

	@Autowired
	PropDao propDao;

	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;

	@Autowired
	private BadgeDetailsDao badgeDetailsDao;

	@Override
	public void buyChangeNameCard(UserDmo user, int count) throws Exception {
		PropTypeDmo typeDmo = this.propTypeDao.findByUniqueId(PropType.changeNameCard.ordinal());
		if (typeDmo.getBadge()<0) {
			throw new Exception("无法用叶子购买");
		}
		int badge = count * typeDmo.getBadge();
		if (user.getBadge() < badge) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - badge);
		this.userDao.save(user);
		PropDmo prop = this.findByUser_Id(user.getId());
		prop.setChangeNicknameCount(prop.getChangeNicknameCount() + count);
		BadgeDetailsDmo detail = new BadgeDetailsDmo(null, user, new Date(), "购买" + count + "个改名卡", 0 - badge);
		this.badgeDetailsDao.save(detail);
		this.propDao.save(prop);
	}

	@Override
	public void buyLabelClearCard(UserDmo user, int count) throws Exception {
		PropTypeDmo typeDmo = this.propTypeDao.findByUniqueId(PropType.clearLabelCard.ordinal());
		if (typeDmo.getBadge()<0) {
			throw new Exception("无法用叶子购买");
		}
		int badge = count * typeDmo.getBadge();
		if (user.getBadge() < badge) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - badge);
		this.userDao.save(user);
		PropDmo prop = this.findByUser_Id(user.getId());
		prop.setLabelClearCount(prop.getLabelClearCount() + count);
		BadgeDetailsDmo detail = new BadgeDetailsDmo(null, user, new Date(), "购买" + count + "个标签消除卡", 0 - badge);
		this.badgeDetailsDao.save(detail);
		this.propDao.save(prop);
	}

	@Autowired
	PropTypeDao propTypeDao;

	@Override
	public void buyNote(UserDmo user, int count) throws Exception {
		PropTypeDmo typeDmo = this.propTypeDao.findByUniqueId(PropType.note.ordinal());
		if (typeDmo.getBadge()<0) {
			throw new Exception("无法用叶子购买");
		}
		int badge = count * typeDmo.getBadge();
		if (user.getBadge() < badge) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - badge);
		this.userDao.save(user);
		PropDmo prop = this.findByUser_Id(user.getId());
		prop.setNoteCount(prop.getNoteCount() + count);
		BadgeDetailsDmo detail = new BadgeDetailsDmo(null, user, new Date(), "购买" + count + "个小纸条", 0 - badge);
		this.badgeDetailsDao.save(detail);
		this.propDao.save(prop);
	}

	@Override
	public void buySignAgainCard(UserDmo user, int count) throws Exception {
		PropTypeDmo typeDmo = this.propTypeDao.findByUniqueId(PropType.signAgainCard.ordinal());
		if (typeDmo.getBadge()<0) {
			throw new Exception("无法用叶子购买");
		}
		int badge = count * typeDmo.getBadge();
		if (user.getBadge() < badge) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - badge);
		this.userDao.save(user);
		PropDmo prop = this.findByUser_Id(user.getId());
		prop.setSignCount(prop.getSignCount() + count);
		BadgeDetailsDmo detail = new BadgeDetailsDmo(null, user, new Date(), "购买" + count + "个补签卡", 0 - badge);
		this.badgeDetailsDao.save(detail);
		this.propDao.save(prop);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void buyVipOneMonth(UserDmo user, int count) throws Exception {
		PropTypeDmo typeDmo = this.propTypeDao.findByUniqueId(PropType.vip1.ordinal());
		if (typeDmo.getBadge()<0) {
			throw new Exception("无法用叶子购买");
		}
		int badge = count * typeDmo.getBadge();
		if (user.getBadge() < badge) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - badge);
		this.userDao.save(user);
		PropDmo prop = this.findByUser_Id(user.getId());
		if (prop.getVipExpireDate().before(new Date())) {
			Date date = new Date();
			date.setMonth(date.getMonth() + count);
			prop.setVipExpireDate(date);
		} else {
			Date date = prop.getVipExpireDate();
			date.setMonth(date.getMonth() + count);
			prop.setVipExpireDate(date);
		}
		this.propDao.save(prop);
		BadgeDetailsDmo detail = new BadgeDetailsDmo(null, user, new Date(), "购买" + count + "个1个月会员", 0 - badge);
		this.badgeDetailsDao.save(detail);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void buyVipOneYear(UserDmo user, int count) throws Exception {
		PropTypeDmo typeDmo = this.propTypeDao.findByUniqueId(PropType.vip12.ordinal());
		if (typeDmo.getBadge()<0) {
			throw new Exception("无法用叶子购买");
		}
		int badge = count * typeDmo.getBadge();
		if (user.getBadge() < badge) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - badge);
		this.userDao.save(user);
		PropDmo prop = this.findByUser_Id(user.getId());
		if (prop.getVipExpireDate().before(new Date())) {
			Date date = new Date();
			date.setYear(date.getYear() + 1 * count);
			prop.setVipExpireDate(date);
		} else {
			Date date = prop.getVipExpireDate();
			date.setYear(date.getYear() + 1 * count);
			prop.setVipExpireDate(date);
		}
		this.propDao.save(prop);
		BadgeDetailsDmo detail = new BadgeDetailsDmo(null, user, new Date(), "购买" + count + "个1年会员", 0 - badge);
		this.badgeDetailsDao.save(detail);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void buyVipThreeMonth(UserDmo user, int count) throws Exception {
		PropTypeDmo typeDmo = this.propTypeDao.findByUniqueId(PropType.vip3.ordinal());
		if (typeDmo.getBadge()<0) {
			throw new Exception("无法用叶子购买");
		}
		int badge = count * typeDmo.getBadge();
		if (user.getBadge() < badge) {
			log.error("叶子不够");
			throw new Exception("叶子不够");
		}
		user.setBadge(user.getBadge() - badge);
		this.userDao.save(user);
		PropDmo prop = this.findByUser_Id(user.getId());
		if (prop.getVipExpireDate().before(new Date())) {
			Date date = new Date();
			date.setMonth(date.getMonth() + 3 * count);
			prop.setVipExpireDate(date);
		} else {
			Date date = prop.getVipExpireDate();
			date.setMonth(date.getMonth() + 3 * count);
			prop.setVipExpireDate(date);
		}
		this.propDao.save(prop);
		BadgeDetailsDmo detail = new BadgeDetailsDmo(null, user, new Date(), "购买" + count + "个3个月会员", 0 - badge);
		this.badgeDetailsDao.save(detail);
	}

	@Override
	public PropDmo findByUser_Id(long userId) {
		PropDmo propDmo = this.propDao.findByUser_Id(userId);
		if (propDmo == null) {
			propDmo = new PropDmo(null, 1, 1, 1, 1, new Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000),
					new UserDmo(userId));
			this.propDao.save(propDmo);
		}
		return propDmo;
	}

	@Override
	public void save(PropDmo prop) {
		this.propDao.save(prop);
	}

	@Autowired
	PropOrderDao propOrderDao;

	@SuppressWarnings("deprecation")
	@Override
	public void buyProp(String outTradeNo) throws Exception {
		PropOrderDmo propOrderDmo = this.propOrderDao.findByOutTradeNo(outTradeNo);
		if (propOrderDmo.getFinishTime() != null) {
			return;
		}
		long userId = propOrderDmo.getUserId();
		int type = propOrderDmo.getPropType();
		int count = propOrderDmo.getCount();
		PropDmo prop = this.findByUser_Id(userId);
		if (type == PropType.note.ordinal()) {
			prop.setNoteCount(prop.getNoteCount() + count);
		} else if (type == PropType.clearLabelCard.ordinal()) {
			prop.setLabelClearCount(prop.getLabelClearCount() + count);
		} else if (type == PropType.signAgainCard.ordinal()) {
			prop.setSignCount(prop.getSignCount() + count);
		} else if (type == PropType.changeNameCard.ordinal()) {
			prop.setChangeNicknameCount(prop.getChangeNicknameCount() + count);
		} else if (type == PropType.vip1.ordinal()) {
			if (prop.getVipExpireDate().before(new Date())) {
				Date date = new Date();
				date.setMonth(date.getMonth() + count);
				prop.setVipExpireDate(date);
			} else {
				Date date = prop.getVipExpireDate();
				date.setMonth(date.getMonth() + count);
				prop.setVipExpireDate(date);
			}
		} else if (type == PropType.vip3.ordinal()) {
			if (prop.getVipExpireDate().before(new Date())) {
				Date date = new Date();
				date.setMonth(date.getMonth() + 3 * count);
				prop.setVipExpireDate(date);
			} else {
				Date date = prop.getVipExpireDate();
				date.setMonth(date.getMonth() + 3 * count);
				prop.setVipExpireDate(date);
			}
		} else if (type == PropType.vip12.ordinal()) {
			if (prop.getVipExpireDate().before(new Date())) {
				Date date = new Date();
				date.setYear(date.getYear() + 1 * count);
				prop.setVipExpireDate(date);
			} else {
				Date date = prop.getVipExpireDate();
				date.setYear(date.getYear() + 1 * count);
				prop.setVipExpireDate(date);
			}
		} else {
			log.error("道具类型不正确");
			throw new Exception("道具类型不正确");
		}
		this.propDao.save(prop);
		propOrderDmo.setFinishTime(new Date());
		this.propOrderDao.save(propOrderDmo);
	}

	@Autowired
	RoomMemberDao roomMemberDao;

	@Override
	@Transactional(rollbackOn=Exception.class)
	public void useFriendCard(UserDmo user, long roomId, int count) throws Exception {
		PropDmo prop=this.findByUser_Id(user.getId());
		if (prop.getFriendCard()<count) {
			log.error(user.getId()+":同伴卡数量不足");
			throw new Exception("同伴卡数量不足");
		}
		RoomMemberDmo roomMember=this.roomMemberDao.findByMember_IdAndRoom_Id(user.getId(), roomId);
		if (roomMember==null) {
			log.error(user.getId()+","+roomId);
			throw new Exception("用户不在房间内");
		}
		RoomDmo room=roomMember.getRoom();
		if (room.getState()!=ActivityStates.新建.ordinal()||room.getPrepareTime()!=null) {
			throw new Exception("房间已准备，无法使用同伴卡");
		}
		if (room.getMemberCount()!=0&&room.getMemberCount().intValue()==room.getJoinMember().intValue()) {
			throw new Exception("房间人数已满");
		}
		room.setJoinMember(room.getJoinMember()+count);
		if (user.getGender()) {
			room.setJoinManMember(room.getJoinManMember()+count);
		}else {
			room.setJoinWomanMember(room.getJoinWomanMember()+count);
		}
		this.roomDao.save(room);
		prop.setFriendCard(prop.getFriendCard()-count);
		this.propDao.save(prop);
		roomMember.setFriendCards(roomMember.getFriendCards()+count);
		this.roomMemberDao.save(roomMember);
	}

	@Autowired
	RoomDao roomDao;

}
