package com.yywl.projectT.bean.component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.JpushBean;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.RandomBean;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.bean.enums.OctRoomEnum;
import com.yywl.projectT.bo.AugActivityPopularGirlBo;
import com.yywl.projectT.bo.BadgeBo;
import com.yywl.projectT.bo.GameScoreBo;
import com.yywl.projectT.bo.MoneyTransactionBo;
import com.yywl.projectT.bo.RoomBo;
import com.yywl.projectT.dao.ActivityDao;
import com.yywl.projectT.dao.ApplicationDao;
import com.yywl.projectT.dao.AugActivityPopularGirlDao;
import com.yywl.projectT.dao.BadgeDetailsDao;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.GameScoreDao;
import com.yywl.projectT.dao.GameScoreHistoryDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.OctRoomDao;
import com.yywl.projectT.dao.OctRoomUserDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomEvaluationDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserCircleDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.ActivityDmo;
import com.yywl.projectT.dmo.ApplicationDmo;
import com.yywl.projectT.dmo.CircleDmo;
import com.yywl.projectT.dmo.GameScoreDmo;
import com.yywl.projectT.dmo.OctRoomDmo;
import com.yywl.projectT.dmo.OctRoomUserDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.RoomEvaluationDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.UserCircleDmo;
import com.yywl.projectT.dmo.UserDmo;

import io.rong.messages.CmdMsgMessage;
import io.rong.messages.InfoNtfMessage;
import io.rong.messages.TxtMessage;

@Component
public class ScheduledBean {
	private static final Log log = LogFactory.getLog(ScheduledBean.class);

	private static final boolean isServer1() {
		InetAddress address;
		try {
			address = InetAddress.getLocalHost();
			String hostAddress = address.getHostAddress();
			if (Keys.MAIN_SERVER_IP.equals(hostAddress)) {
				return true;
			}
		} catch (UnknownHostException e) {
			log.error(e.getMessage());
		}
		return false;
	}

	@Autowired
	ApplicationDao applicationDao;
	
	@Scheduled(cron="0 0/10 * ? * *")
	@Async
	public void receiveMail() throws Exception {
		if (!isServer1()) {
			return; 
		}
		ApplicationDmo iosNextDmo=applicationDao.findOne(5);
		if (!iosNextDmo.getIsCurrent()) {
			return;
		}
		ApplicationDmo iosDmo=applicationDao.findOne(2);
		ApplicationDmo iosCheckDmo=applicationDao.findOne(1);
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.auth", "true");
		Session session = Session.getInstance(props);
		session.setDebug(false);
		IMAPStore store = (IMAPStore) session.getStore("imap");
		store.connect("imap.hzease.com", Keys.JavaMail.USERNAME, Keys.JavaMail.PASSWORD);
		IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		int start = folder.getMessageCount() - folder.getUnreadMessageCount() + 1;
		int end = folder.getMessageCount();
		Message[] messages = folder.getMessages(start, end);
		for (Message message : messages) {
			if (!message.getSubject().contains("Ready for Sale")) {
				continue;
			}
			Object objContent = message.getContent();
			if (objContent.getClass() != MimeMultipart.class) {
				continue;
			}
			MimeMultipart mimeMultipart = (MimeMultipart) objContent;
			BodyPart part = mimeMultipart.getBodyPart(0);
			String str = (String) part.getContent();
			str = str.trim();
			if (str.contains("App Version Number: ")) {
				str = str.split("App Version Number: ")[1];
				if (str.contains("\n")) {
					str = str.split("\n")[0].trim();
				}
				if (str.contains(" ")) {
					str = str.split(" ")[0].trim();
				}
			}
			iosDmo.setVersion(str);
			iosCheckDmo.setVersion(iosNextDmo.getVersion());
			iosNextDmo.setIsCurrent(false);
			iosNextDmo.setMessage("程序已修改");
		}
		folder.close(false);
		store.close();
		this.applicationDao.save(iosDmo);
		this.applicationDao.save(iosCheckDmo);
		this.applicationDao.save(iosNextDmo);
	}

	/**
	 * 每隔半小时
	 */
	@Scheduled(fixedRate = 25 * 60 * 1000)
	@Async
	public void joinChatRoom() {
		if (!isServer1()) {
			return;
		}
		List<Long> roomIds = this.jdbcDao.findRoomIdByStateLessThan(ActivityStates.待评价.ordinal());
		for (Long chatroomId : roomIds) {
			Thread t = new Thread(() -> {
				try {
					rongCloud.rongCloud.chatroom.join(new String[] { Keys.RONGCLOUD_SYSTEM_ID }, chatroomId + "");
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			});
			t.start();
		}
	}

	@Autowired
	RoomDao roomDao;

	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	Keys keys;

	@Autowired
	UserDao userDao;

	@Autowired
	RoomBo roomBo;

	@Autowired
	RongCloudBean rongCloud;

	@Autowired
	CircleDao circleDao;

	@Autowired
	UserCircleDao userCircleDao;

	@Autowired
	GameScoreDao gameScoreDao;

	@Autowired
	MoneyTransactionBo moneyTransactionBo;

	@Autowired
	GameScoreHistoryDao gameScoreHistoryDao;

	@Autowired
	BadgeDetailsDao badgeDetailsDao;

	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	RoomEvaluationDao roomEvaluationDao;
	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Autowired
	FriendDao friendDao;
	@Autowired
	GameScoreBo gameScoreBo;

	@Autowired
	private BadgeBo badgeBo;

	/**
	 * 每天零点清除已经结束的房间，设置为待评价的状态
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	@Async
	public void clearRoom() {
		if (!isServer1()) {
			return;
		}
		jdbcDao.cleanRoom();
	}

	/**
	 * 签到清零
	 */
	@Scheduled(cron = "0 0 0 ? * *")
	@Async
	public void clearSignCircle() {
		if (!isServer1()) {
			return;
		}
		this.jdbcDao.clearSignCircle();
	}

	/**
	 * 如果活动结束后6小时内没有评价，则自动评价。
	 * 
	 */
	@Scheduled(cron = "0 0/10 * ? * *")
	@Async
	public void evaluate() {
		if (!isServer1()) {
			return;
		}
		long time = System.currentTimeMillis();
		Date delayTime = new Date(time + 60 * 1000);
		List<RoomDmo> rooms = roomDao.findByEvaluateTimeBeforeAndStateAndGame_IdNot(delayTime, ActivityStates.待评价.ordinal(),30);
		for (RoomDmo room : rooms) {
			room.setState(ActivityStates.已结束.ordinal());
			roomDao.save(room);
			roomBo.evalute(room);
		}
	}

	@Async
	@Scheduled(cron="0 0/10 * ? * *")
	public void abandonChatRoom() {
		if (!isServer1()) {
			return;
		}
		List<RoomDmo> rooms=this.roomDao.findByEndTimeBeforeAndStateNotAndGame_Id(new Date(),ActivityStates.已废弃.ordinal(),30);
		for (RoomDmo roomDmo : rooms) {
			roomDmo.setState(ActivityStates.已废弃.ordinal());
			this.roomDao.save(roomDmo);
		}
	}
	
	/**
	 * 每隔10分钟检查是否有即将开始的活动，如果有就推送给玩家
	 */
	@Scheduled(cron = "0 0/10 * ? * *")
	@Async
	public void push() {
		if (!isServer1()) {
			return;
		}
		long time = System.currentTimeMillis();
		Date start = new Date(time + 10 * 60 * 1000 - 60 * 1000);
		Date end = new Date(time + 10 * 60 * 1000 + 60 * 1000);
		Sort sort = new Sort(new Sort.Order(Direction.ASC, "room_BeginTime"));
		List<RoomMemberDmo> roomMembers = this.roomMemberDao.findByRoom_BeginTimeBetweenAndRoom_StateAndIsSigned(start,
				end, ActivityStates.准备中.ordinal(), false, sort);
		Set<Long> roomIdSet = new HashSet<>();
		for (RoomMemberDmo dmo : roomMembers) {
			RoomDmo room = dmo.getRoom();
			roomIdSet.add(room.getId());
			Thread thread = new Thread(() -> {
				Map<String, Object> map = new HashMap<>();
				map.put("type", "room");
				map.put("id", room.getId() + "");
				try {
					rongCloud.sendSystemMessage(dmo.getMember().getId(), new TxtMessage(
							"离【" + room.getName() + "】活动还有10分钟，请尽快签到吧，迟到可是会扣保证金的哦~", Formatter.gson.toJson(map)));
					rongCloud.sendSystemMessage(dmo.getMember().getId(),
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_SENDLOCATION, "" + room.getId()));
					String alert = "您加入的活动【" + room.getName() + "】还有10分钟就要开始啦，请尽快签到吧，迟到可是会扣保证金的哦~";
					String message = "地点为：" + room.getPlace() + ",开始时间为："
							+ Formatter.dateTimeFormatter.format(room.getBeginTime());
					JpushBean.push(alert, message, dmo.getMember().getId() + "");

				} catch (Exception e) {
					log.error(e.getMessage());
				}
			});
			thread.start();
		}
		for (Long roomId : roomIdSet) {
			Thread thread = new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new InfoNtfMessage("离活动开始还有10分钟，请尽快签到吧，迟到可是会扣保证金的哦~", ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			});
			thread.start();
		}
	}

	/**
	 * 每隔10分钟检查房间有没有准备，如果准备就进入开始状态，如果没有准备就解散房间。
	 * 
	 */
	@Scheduled(cron = "0 0/10 * * * ?")
	@Async
	public void roomBegin() {
		if (!isServer1()) {
			return;
		}
		Date delayTime = new Date(System.currentTimeMillis() + 60 * 1000);
		List<RoomDmo> newRooms = roomDao.findByBeginTimeBeforeAndStateLessThanAndGame_IdNot(delayTime, ActivityStates.准备中.ordinal(),30),
				prepareRooms = roomDao.findByBeginTimeBeforeAndStateAndGame_IdNot(delayTime, ActivityStates.准备中.ordinal(),30);
		for (RoomDmo room : prepareRooms) {
			room.setState(ActivityStates.进行中.ordinal());
			roomDao.save(room);
		}
		for (RoomDmo room : newRooms) {
			Thread thread = new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, room.getId(),
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_ROOM_DISSOLVE, ""));
					rongCloud.sendSystemMessage(new String[] { room.getManager().getId() + "" },
							new TxtMessage("您的房间【" + room.getName() + "】因到达开始时间未就绪，自动解散", ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			});
			thread.start();
			roomBo.delete(room);
		}
		for (RoomDmo room : prepareRooms) {
			CircleDmo circle = room.getBelongCircle();
			if (circle != null) {
				circle.setHot(circle.getHot() + 1);
				circleDao.save(circle);
			}
			Thread thread1 = new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.SYSTEM_ID + "", room.getId(),
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, "到达活动开始时间"));
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, room.getId(),
							new InfoNtfMessage("活动开始啦", ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			});
			thread1.start();
			List<Long> memberIds = this.jdbcDao.findMember_IdsByRoom_Id(room.getId());
			new Thread(()->{
				for (Long memberId : memberIds) {
					JpushBean.push("【" + room.getName() + "】活动开始啦", memberId + "");
				}
			}).start();
			new Thread(() -> {
				try {
					Map<String, Object> map = new HashMap<>();
					map.put("type", "room");
					map.put("id", room.getId() + "");
					rongCloud.sendSystemMessage(memberIds,
							new TxtMessage("【" + room.getName() + "】活动开始啦", Formatter.gson.toJson(map)));
					rongCloud.sendSystemMessage(memberIds,
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_SENDLOCATION, "" + room.getId()));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				//十月活动
				Date now=new Date();
				ActivityDmo activityDmo=this.activityDao.findOne(7L);
				boolean isInRange=now.after(activityDmo.getBeginTime())&&now.before(activityDmo.getEndTime());
				if (isInRange) {
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
				}
			}).start();

		}
	}

	@Autowired
	OctRoomUserDao octRoomUserDao;
	
	@Autowired
	ActivityDao activityDao;
	
	@Autowired
	OctRoomDao octRoomDao;
	@Autowired
	AugActivityPopularGirlBo popularGirlBo;
	@Autowired
	AugActivityPopularGirlDao popularGirlDao;

	/**
	 * 每隔10分钟运行，等待评价。
	 * 
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	@Async
	public void roomEnd() {
		if (!isServer1()) {
			return;
		}
		long time = System.currentTimeMillis();
		Date delayTime = new Date(time + 60 * 1000);
		List<RoomDmo> rooms = roomDao.findByEndTimeBeforeAndStateAndGame_IdNot(delayTime, ActivityStates.进行中.ordinal(),30);
		for (RoomDmo room : rooms) {
			room.setState(ActivityStates.待评价.ordinal());
			roomDao.save(room);
		}
		for (RoomDmo room : rooms) {
			CircleDmo belongCircle = room.getBelongCircle();
			// 获取参加活动的成员
			List<Long> memberIds = jdbcDao.findMember_IdsByRoom_Id(room.getId());
			// 其它成员的数量
			int otherCount = memberIds.size() - 1;
			for (Long memberId : memberIds) {
				Thread t = new Thread() {
					public void run() {
						JpushBean.push(room.getName() + "活动结束，快给小伙伴点个赞吧", "" + memberId);
					};
				};
				t.start();
				// 如果是VIP会员，添加一个徽章
				int addBadge = 1;
				if (memberId.longValue() == room.getManager().getId().longValue()) {
					addBadge++;
				}
				if (room.getMemberCount() < 20) {
					// 查询未评价过的
					long friendCount = this.friendDao.countByOwner_IdAndFriend_IdIn(memberId, memberIds);
					// 其它未成为好友的人数
					long unFriendCount = new Long(otherCount) - friendCount;
					// 加上未评价过的人数
					addBadge += unFriendCount;
				}
				if (belongCircle != null && userCircleDao.existsByUser_IdAndCircle_Id(memberId, belongCircle.getId())) {
					int level = this.jdbcDao.findUserCircleLevel(memberId, belongCircle.getId());
					int levelBadge = RandomBean.random.nextInt(2) * (level - 1);
					addBadge += levelBadge;
				}
				// 叶子最多加15个
				addBadge = addBadge > 15 ? 15 : addBadge;
				this.badgeBo.roomEndAddBadge(memberId, room.getId(), addBadge);
			}
			// 如果保证金不为0
			if (room.getMoney() > 0) {
				int remainingMoney = 0;
				List<UserDmo> attendMembers = new LinkedList<>();
				for (Long id : memberIds) {
					RoomMemberDmo rm = roomMemberDao.findByMember_IdAndRoom_Id(id, room.getId());
					// 如果没有按时签到,不解冻保证金
					if ((!rm.isSigned()) || (!rm.isReady())) {
						if (rm.isRequestNotLate()) {
							continue;
						}
						remainingMoney += room.getMoney();
						this.moneyTransactionBo.roomLateFine(rm);
					} else {
						UserDmo userDmo = rm.getMember();
						attendMembers.add(userDmo);
						this.moneyTransactionBo.roomThaw(rm);
					}
				}
				// 如果出席的人小于总人数，即有人未出席
				if (!attendMembers.isEmpty() && attendMembers.size() < memberIds.size()) {
					// 计算每个人余额的增加量（向下取整）
					int addMoney = remainingMoney / attendMembers.size();
					// 出席的人分到钱
					if (addMoney > 0) {
						for (UserDmo userDmo : attendMembers) {
							this.moneyTransactionBo.getRoomFee(room, userDmo, addMoney);
						}
					}
					// 保存房间剩余的金额,存入系统账户
					int money = remainingMoney - addMoney * attendMembers.size();
					if (money > 0) {
						UserDmo systemUser = this.userDao.findOne(Keys.SYSTEM_ID);
						this.moneyTransactionBo.saveSystemFee(money, systemUser);
						room.setRemainingMoney(money);
						this.roomDao.save(room);
					}
				}
				if (attendMembers.isEmpty() && attendMembers.size() < memberIds.size()) {
					int money = memberIds.size() * room.getMoney();
					UserDmo user = this.userDao.findOne(Keys.SYSTEM_ID);
					user.setAmount(user.getAmount() + money);
					this.userDao.save(user);
				}
			} else {
				new Thread(() -> {
					Map<String, Object> map = new HashMap<>();
					map.put("type", "evaluation");
					map.put("id", room.getId() + "");
					try {
						rongCloud.sendSystemMessage(memberIds,
								new TxtMessage("【" + room.getName() + "】结束，快去评价吧(点击跳转)", Formatter.gson.toJson(map)));
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}).start();
			}
			// 获取圈子成员表，计算经验值
			for (Long memberId : memberIds) {
				// 活动结束后添加默认的评价
				List<Long> memberIds2 = new LinkedList<>();
				memberIds2.addAll(memberIds);
				memberIds2.remove(memberId);
				for (Long memberId2 : memberIds2) {
					RoomEvaluationDmo roomEvaluationDmo = this.roomEvaluationDao
							.findByRoomIdAndOwnerIdAndOtherId(room.getId(), memberId, memberId2);
					if (null == roomEvaluationDmo) {
						roomEvaluationDmo = new RoomEvaluationDmo(memberId, memberId2, 5, room.getId());
						this.roomEvaluationDao.save(roomEvaluationDmo);
					}
				}
				if (belongCircle != null) {
					UserCircleDmo userCircleDmo = this.userCircleDao.findByUser_IdAndCircle_Id(memberId,
							belongCircle.getId());
					if (userCircleDmo == null) {
						userCircleDmo = new UserCircleDmo();
						userCircleDmo.setUser(userDao.findOne(memberId));
						userCircleDmo.setCircle(belongCircle);
					}
					// 获取活动的管理员
					// 获取圈子成员表，计算经验值
					if (room.getManager().getId() == memberId) {
						userCircleDmo.setCreateCount(userCircleDmo.getCreateCount() + 1);
						userCircleDmo.setExperience(userCircleDmo.getExperience() + Keys.Circle.CREATE_EXPERIENCE_ADD);
					} else {
						userCircleDmo.setExperience(userCircleDmo.getExperience() + Keys.Circle.JOIN_EXPERIENCE_ADD);
						userCircleDmo.setJoinCount(userCircleDmo.getJoinCount() + 1);
					}
					userCircleDao.save(userCircleDmo);
				}
				// 添加好友
				roomBo.addFriends(memberId, room.getId());
				if (room.getGame().isScoring()) {
					GameScoreDmo gameScoreDmo = gameScoreDao.findByUser_IdAndGame_Id(memberId, room.getGame().getId());
					if (gameScoreDmo == null) {
						gameScoreDmo = new GameScoreDmo(null, new UserDmo(memberId), room.getGame(),
								Keys.Room.INIT_POINT, 1);
					} else {
						gameScoreDmo.setCount(gameScoreDmo.getCount() + 1);
					}
					gameScoreDao.save(gameScoreDmo);
				}
			}
			new Thread(() -> {
				try {
					rongCloud.destoryChatRoom(Keys.Room.PREFIX + room.getId());
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
			//十月活动
			new Thread(()->{
				Date now=new Date();
				ActivityDmo activityDmo=this.activityDao.findOne(7L);
				boolean isInRange=now.after(activityDmo.getBeginTime())&&now.before(activityDmo.getEndTime());
				if (isInRange) {
					int signCount=this.roomMemberDao.countByRoom_IdAndIsSigned(room.getId(),true);
					OctRoomDmo dmo=this.octRoomDao.findByRoomId(room.getId());
					if (dmo==null) {
						dmo=new OctRoomDmo(null, room.getId(), signCount);
					}else {
						dmo.setSignCount(signCount);
					}
					int bounty=0;
					if (memberIds.size()<4) {
						bounty=500;
					}else {
						bounty=1000;
					}
					dmo.setBounty(bounty);
					dmo.setState(OctRoomEnum.审核中.ordinal());
					dmo.setReason("审核中");
					this.octRoomDao.save(dmo);
				}
			}).start();
		}
	}

	/**
	 * 每周一导入排行榜历史数据
	 */
	@Scheduled(cron = "0 0 4 ? * MON")
	@Async
	public void saveScoreHistory() {
		if (!isServer1()) {
			return;
		}
		this.gameScoreBo.clear();
		this.gameScoreBo.imports();
	}

	/**
	 * 切换管理员
	 */
	@Scheduled(cron = "0 0 3 ? * MON")
	@Async
	public void setCircleManager() {
		if (!isServer1()) {
			return;
		}
		List<CircleDmo> circleDmos = this.circleDao.findAll();
		for (CircleDmo circle : circleDmos) {
			UserDmo oldManager = circle.getManager();
			String oldManagerName = oldManager.getNickname();
			Long userId = jdbcDao.findCircleMaxExperienceUserId(circle.getId());
			if (userId != null) {
				if (userId.longValue() == oldManager.getId().longValue()) {
					continue;
				}
				UserDmo newManager = this.userDao.findOne(userId);
				String newManagerName = newManager.getNickname();
				circle.setManager(newManager);
				Thread t = new Thread(() -> {
					try {
						rongCloud.sendSystemTextMsgToOne(oldManager.getId(),
								new TxtMessage("您在【" + circle.getName() + "】圈子的经验值被超过，取消了圈主权限", ""));
						rongCloud.sendSystemTextMsgToOne(newManager.getId(),
								new TxtMessage("您在【" + circle.getName() + "】圈子的经验值被超过原来的圈主，成为了新的圈主", ""));
						rongCloud.sendGroup(newManager.getId() + "", circle.getId() + "",
								new InfoNtfMessage("圈主由【" + oldManagerName + "】切换为【" + newManagerName + "】", ""));
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				});
				t.start();
			}
		}
		circleDao.save(circleDmos);
	}

	@Scheduled(cron = "0 0 5 * * ?")
	@Async
	public void clearPropOrder() {
		if (!isServer1()) {
			return;
		}
		this.jdbcDao.deletePropOrderByCreateTimeLessAndFinishedTime();
	}
}
