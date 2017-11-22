package com.yywl.projectT.bo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.bean.JpushBean;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.FriendInvitationState;
import com.yywl.projectT.dao.AugActivityPopularGirlDao;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.FriendInvitationDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomEvaluationDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.FriendDmo;
import com.yywl.projectT.dmo.FriendInvitationDmo;
import com.yywl.projectT.dmo.RoomEvaluationDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.EvaluationVo;

import io.rong.messages.CmdMsgMessage;
import io.rong.messages.TxtMessage;

@Service
@Transactional(rollbackOn = Throwable.class)
public class FriendBoImpl implements FriendBo {

	@Autowired
	FriendDao friendDao;

	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;

	@Override
	public void becameBlueFriends(long ownerId, long friendId, String origin) throws Exception {
		Date now = Calendar.getInstance().getTime();
		if (ownerId == friendId) {
			log.error("不能和自己成为好友");
			throw new Exception("不能和自己成为好友");
		}
		FriendDmo conn1 = this.friendDao.findByOwner_IdAndFriend_Id(ownerId, friendId);
		FriendDmo conn2 = this.friendDao.findByOwner_IdAndFriend_Id(friendId, ownerId);
		boolean flag = false;
		if (null == conn1) {
			conn1 = new FriendDmo();
			conn1.setOwner(new UserDmo(ownerId));
			conn1.setFriend(new UserDmo(friendId));
			conn1.setPoint(7.0);
			conn1.setOrigin(origin);
			conn1.setCreateTime(now);
			conn1.setEvaluatedPoint(7);
			conn1.setEvaluatePoint(7);
			this.friendDao.save(conn1);
			flag = true;
		}
		if (null == conn2) {
			conn2 = new FriendDmo();
			conn2.setOwner(new UserDmo(friendId));
			conn2.setFriend(new UserDmo(ownerId));
			conn2.setPoint(7.0);
			conn2.setEvaluatedPoint(7);
			conn2.setEvaluatePoint(7);
			conn2.setOrigin(origin);
			conn2.setCreateTime(now);
			this.friendDao.save(conn2);
			flag = true;
		}
		if (conn1.getPoint().doubleValue() < 7) {
			conn1.setPoint(7.0);
			conn1.setOrigin(origin);
			conn1.setCreateTime(now);
			conn1.setEvaluatedPoint(7);
			conn1.setEvaluatePoint(7);
			this.friendDao.save(conn1);
			flag = true;
		}
		if (conn2.getPoint().doubleValue() < 7) {
			conn2.setPoint(7.0);
			conn2.setEvaluatedPoint(7);
			conn2.setEvaluatePoint(7);
			conn2.setOrigin(origin);
			conn2.setCreateTime(now);
			this.friendDao.save(conn2);
			flag = true;
		}
		final long id1 = conn1.getOwner().getId();
		final long id2 = conn1.getFriend().getId();
		if (flag) {
			new Thread(() -> {
				try {
					rongCloud.sendMessageToFriend(id1, id2, new TxtMessage("我们已经是好友啦，快来聊天吧", ""));
					rongCloud.sendMessageToFriend(id2, id1, new TxtMessage("我们已经是好友啦，快来聊天吧", ""));
					rongCloud.sendSystemMessage(new String[] { id1 + "", id2 + "" },
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_FRIENDS, ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();

		}

	}

	private final static Log log = LogFactory.getLog(FriendBoImpl.class);

	@Autowired
	AugActivityPopularGirlBo popularGirlBo;

	@Autowired
	AugActivityPopularGirlDao popularGirlDao;

	@Override
	public void evalute(Long userId, String token, long roomId, List<EvaluationVo> vos) throws Exception {
		userBo.loginByToken(userId, token);
		RoomMemberDmo roomMemberDmo = this.roomMemberDao.findByRoom_IdAndMember_Id(roomId, userId);
		if (roomMemberDmo.isEvaluated()) {
			throw new Exception("活动已评价");
		}
		for (EvaluationVo vo : vos) {
			RoomMemberDmo friendRoomMemberDmo = this.roomMemberDao.findByRoom_IdAndMember_Id(roomId, vo.getFriendId());
			boolean isSign = friendRoomMemberDmo.isSigned();
			FriendDmo friendConnDmo1 = friendDao.findByOwner_IdAndFriend_Id(userId, vo.getFriendId());
			if (friendConnDmo1 == null) {
				friendConnDmo1 = new FriendDmo();
				friendConnDmo1.setOwner(new UserDmo(userId));
				friendConnDmo1.setFriend(new UserDmo(vo.getFriendId()));
				friendDao.save(friendConnDmo1);
			}
			FriendDmo friendConnDmo2 = friendDao.findByOwner_IdAndFriend_Id(vo.getFriendId(), userId);
			if (friendConnDmo2 == null) {
				friendConnDmo2 = new FriendDmo();
				friendConnDmo2.setOwner(new UserDmo(vo.getFriendId()));
				friendConnDmo2.setFriend(new UserDmo(userId));
				friendDao.save(friendConnDmo2);
			}
			if (friendConnDmo1.getEvaluatePoint() == 0 && friendConnDmo2.getEvaluatedPoint() == 0) {
					friendConnDmo1.setEvaluatePoint(vo.getPoint());
					friendConnDmo2.setEvaluatedPoint(vo.getPoint());
				if (friendConnDmo1.getEvaluatePoint() > 4 && friendConnDmo1.getEvaluatedPoint() <= 4) {
					friendConnDmo1.setPoint(0.0);
					friendConnDmo2.setPoint(friendConnDmo1.getEvaluatedPoint() * 1.0);
				} else if (friendConnDmo1.getEvaluatePoint() <= 4 && friendConnDmo1.getEvaluatedPoint() > 4) {
					friendConnDmo1.setPoint(friendConnDmo1.getEvaluatePoint() * 1.0);
					friendConnDmo2.setPoint(0.0);
				} else {
					double point = Math.min(friendConnDmo1.getEvaluatePoint(), friendConnDmo1.getEvaluatedPoint());
					friendConnDmo1.setPoint(point);
					friendConnDmo2.setPoint(point);
					try {
						final Long ownerId = friendConnDmo1.getOwner().getId();
						final Long friendId = friendConnDmo2.getOwner().getId();
						new Thread(() -> {
							try {
								Thread.sleep(5000);
								rongCloud.sendSystemMessage(new String[] { ownerId + "", friendId + "" },
										new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_FRIENDS, ""));
								Thread.sleep(5000);
								if (point > 5) {
									rongCloud.sendMessageToFriend(ownerId, friendId,
											new TxtMessage("我们已经是好友啦，快来聊天吧", ""));
									rongCloud.sendMessageToFriend(friendId, ownerId,
											new TxtMessage("我们已经是好友啦，快来聊天吧", ""));
								}
							} catch (Exception e) {
								log.error(e.getMessage());
							}
						}).start();
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
			}
			UserDmo friendDmo = userDao.findOne(vo.getFriendId());
			Set<String> labels = friendDmo.getLabels();
			labels.addAll(vo.getLabels());
			labels.remove("");
			labels.remove(null);
			friendDmo.setLabels(labels);
			userDao.save(friendDmo);
			friendDao.save(friendConnDmo1);
			friendDao.save(friendConnDmo2);
			RoomEvaluationDmo roomEvaluationDmo = this.roomEvaluationDao.findByRoomIdAndOwnerIdAndOtherId(roomId,
					userId, vo.getFriendId());
			if (null == roomEvaluationDmo) {
				roomEvaluationDmo = new RoomEvaluationDmo(userId, vo.getFriendId(), vo.getRoomEvaluationPoint(),
						roomId);
				if (isSign) {
					roomEvaluationDmo.setPoint(vo.getRoomEvaluationPoint());
				}else {
					roomEvaluationDmo.setPoint(1);
				}
				this.roomEvaluationDao.save(roomEvaluationDmo);
			} else {
				if (isSign) {
					roomEvaluationDmo.setPoint(vo.getRoomEvaluationPoint());
				} else {
					roomEvaluationDmo.setPoint(1);
				}
				this.roomEvaluationDao.save(roomEvaluationDmo);
			}
		}
		roomMemberDmo.setEvaluated(true);
		this.roomMemberDao.save(roomMemberDmo);
		// 查看房间内的成员是否都有最终的得分,即是否评价完成。
		boolean existsRoomMemberUnEvaluated = this.roomMemberDao.existsByRoom_IdAndIsEvaluated(roomId, false);
		// 如果存在未评价的，则返回
		if (existsRoomMemberUnEvaluated) {
			return;
		}
		this.roomBo.evalute(this.roomDao.findOne(roomId));
	}

	@Autowired
	RoomDao roomDao;
	@Autowired
	RoomBo roomBo;
	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	RoomEvaluationDao roomEvaluationDao;

	@Autowired
	RongCloudBean rongCloud;

	@Autowired
	RoomMemberDao roomMemberDao;

	@Override
	public FriendDmo findFriend(Long ownId, Long friendId) {
		FriendDmo friendDmo = friendDao.findByOwner_IdAndFriend_Id(ownId, friendId);
		return friendDmo;
	}

	@Override
	public List<FriendDmo> findFriends(Long userId) {
		List<FriendDmo> friendConns = this.friendDao.findByOwner_IdAndPointNotOrderByPointDesc(userId, 0.0);
		return friendConns;
	}

	@Override
	public String getColor(double point) {
		// 如果是红色好友
		if (point < 2) {
			return "红色";
			// 如果是灰色好友
		} else if (point < 4) {
			return "灰色";
			// 如果是绿色好友
		} else if (point < 6) {
			return "绿色";
			// 如果是蓝色好友
		} else if (point < 8) {
			return "蓝色";
			// 如果是金色好友
		} else {
			return "金色";
		}
	}

	@Override
	public void invitateFriend(long userId, String token, long friendId, String origin) throws Exception {
		if (userId == friendId) {
			log.error("不能和自己成为好友");
			throw new Exception("不能和自己成为好友");
		}
		UserDmo owner = this.userBo.loginByToken(userId, token);
		boolean exists = this.userDao.exists(friendId);
		if (!exists) {
			log.error("邀请的ID不存在");
			throw new Exception("邀请的ID不存在");
		}
		UserDmo friend = new UserDmo(friendId);
		FriendInvitationDmo dmo = this.friendInvitationDao.findByOwner_IdAndFriend_Id(userId, friendId);
		if (dmo == null) {
			dmo = new FriendInvitationDmo(null, owner, friend, new Date(), FriendInvitationState.未读.ordinal());
			dmo.setOrigin(origin);
		} else {
			dmo.setCreateTime(new Date());
			dmo.setOrigin(origin);
		}
		friendInvitationDao.save(dmo);
		JpushBean.push("您收到了来自" + owner.getNickname() + "的好友请求", friendId + "");
	}

	@Autowired
	FriendInvitationDao friendInvitationDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void receiveInvitation(long userId, String token, long invitationId, int state) throws Exception {
		this.userBo.loginByToken(userId, token);
		FriendInvitationDmo invitation = this.friendInvitationDao.findOne(invitationId);
		if (invitation == null) {
			log.error("邀请的ID不存在");
			throw new Exception("邀请的ID不存在");
		}
		invitation.setState(state);
		this.friendInvitationDao.save(invitation);
		if (state == FriendInvitationState.已同意.ordinal()) {
			this.becameBlueFriends(invitation.getOwner().getId(), invitation.getFriend().getId(),
					invitation.getOrigin());
		}
	}

	@Override
	public void removeInvitation(long userId, String token, long invitationId) throws Exception {
		this.userBo.loginByToken(userId, token);
		FriendInvitationDmo invitation = this.friendInvitationDao.findOne(invitationId);
		if (invitation == null) {
			log.error("邀请的ID不存在");
			throw new Exception("邀请不存在");
		}
		if (invitation.getOwner().getId().longValue() == userId
				|| invitation.getFriend().getId().longValue() == userId) {
			this.friendInvitationDao.delete(invitationId);
		} else {
			log.error("您无权删除邀请");
			throw new Exception("您无权删除邀请");
		}
	}

}
