package com.yywl.projectT.bo;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.dao.ComplaintDao;
import com.yywl.projectT.dao.LocationDao;
import com.yywl.projectT.dao.PayOrderDao;
import com.yywl.projectT.dao.PropDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomEvaluationDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dao.WithdrawalsDao;
import com.yywl.projectT.dmo.ComplaintDmo;
import com.yywl.projectT.dmo.PayOrderDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.RoomEvaluationDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.TransactionDetailsDmo;
import com.yywl.projectT.dmo.UserDmo;

import io.rong.messages.TxtMessage;

@Service
@Transactional(rollbackOn = Throwable.class)
public class MoneyTransactionBo {
	private static final Log log = LogFactory.getLog(MoneyTransactionBo.class);
	@Autowired
	private UserDao userDao;
	@Autowired
	private TransactionDetailsDao transactionDetailsDao;
	@Autowired
	private RoomMemberDao roomMemberDao;
	@Autowired
	private RongCloudBean rongCloud;

	@Autowired
	private PayOrderDao payOrderDao;

	@Autowired
	private ComplaintDao complaintDao;

	@Autowired
	private RoomDao roomDao;

	@Autowired
	private RoomEvaluationDao roomEvaluationDao;

	@Autowired
	LocationDao locationDao;

	@Autowired
	JdbcTemplate jdbc;

	/**
	 * 成为房主，扣除保证金
	 * 
	 * @param user
	 * @param room
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void becomeRoomManager(UserDmo user, RoomDmo room) throws Exception {
		int money = room.getMoney();
		RoomMemberDmo roomMemberDmo = new RoomMemberDmo();
		roomMemberDmo.setGame(room.getGame());
		roomMemberDmo.setRoom(room);
		roomMemberDmo.setMember(user);
		roomMemberDmo.setReady(true);
		roomMemberDmo.setResult(0);
		roomMemberDmo.setNickname(user.getNickname());
		roomMemberDmo.setJoinTime(new Date());
		user.setAmount(user.getAmount() - money);
		user.setLockAmount(user.getLockAmount() + money);
		if (user.getLockAmount() < 0 || user.getAmount() < 0) {
			log.error("您的余额和冻结资金不能小于0");
			throw new Exception("您的余额和冻结资金不能小于0");
		}
		String roomName = room.getName();
		if (money > 0) {
			TransactionDetailsDmo td = new TransactionDetailsDmo();
			td.setCreateTime(new Date());
			td.setDescription("【" + roomName + "】创建，冻结保证金");
			td.setUser(user);
			td.setMoney(0 - money);
			transactionDetailsDao.save(td);
			roomMemberDmo.setLockMoney(true);
		}
		userDao.save(user);
		roomMemberDmo.setOnline(true);
		roomMemberDao.save(roomMemberDmo);
	}

	/**
	 * 取消房间准备
	 * 
	 * @param roomMemberDmo
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void cancelRoomReady(RoomMemberDmo roomMemberDmo) throws Exception {
		UserDmo user = roomMemberDmo.getMember();
		RoomDmo room = roomMemberDmo.getRoom();
		room.setPrepareTime(null);
		roomDao.save(room);
		int money = room.getMoney();
		roomMemberDmo.setReady(false);
		roomMemberDmo.setLockMoney(false);
		roomMemberDao.save(roomMemberDmo);
		String roomName = room.getName();
		if (room.getMoney() > 0) {
			TransactionDetailsDmo td = new TransactionDetailsDmo();
			td.setCreateTime(new Date());
			td.setDescription("【" + roomName + "】取消准备，解冻保证金");
			td.setMoney(money);
			td.setUser(user);
			transactionDetailsDao.save(td);
			user.setLockAmount(user.getLockAmount() - money);
			user.setAmount(user.getAmount() + money);
			if (user.getLockAmount() < 0 || user.getAmount() < 0) {
				log.error("您的余额和冻结资金不能小于0");
				throw new Exception("您的余额和冻结资金不能小于0");
			}
			userDao.save(user);
		}

	}

	/**
	 * 删除房间
	 * 
	 * @param roomDmo
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void deleteRoom(RoomDmo roomDmo) {
		String roomName = roomDmo.getName();
		List<ComplaintDmo> complaintDmos = this.complaintDao.findByRoom_Id(roomDmo.getId());
		this.complaintDao.delete(complaintDmos);
		List<RoomEvaluationDmo> roomEvaluationDmos = this.roomEvaluationDao.findAll();
		this.roomEvaluationDao.delete(roomEvaluationDmos);
		boolean isMovieTicketType = false;
		if (roomDmo.getManager().isSuperUser() && roomDmo.getGame().getId().intValue() == 22) {
			isMovieTicketType = true;
		}
		List<RoomMemberDmo> roomMembers = roomMemberDao.findByRoom_Id(roomDmo.getId());
		for (RoomMemberDmo roomMemberDmo : roomMembers) {
			if (isMovieTicketType && roomMemberDmo.isReady()) {
				PropDmo prop = this.propDao.findByUser_Id(roomMemberDmo.getMember().getId());
				prop.setRemainMovieTicket(prop.getRemainMovieTicket() + 1);
				this.propDao.save(prop);
			}
			if (roomMemberDmo.getFriendCards() > 0) {
				PropDmo prop = this.propDao.findByUser_Id(roomMemberDmo.getMember().getId());
				prop.setFriendCard(prop.getFriendCard() + roomMemberDmo.getFriendCards());
				this.propDao.save(prop);
			}
			if (roomDmo.getMoney() != 0) {
				if (roomMemberDmo.isReady()) {
					UserDmo user = roomMemberDmo.getMember();
					user.setAmount(user.getAmount() + roomDmo.getMoney());
					user.setLockAmount(user.getLockAmount() - roomDmo.getMoney());
					if (user.getLockAmount() < 0 || user.getAmount() < 0) {
						log.error(user.getId() + ":您的余额和冻结资金不能小于0");
					}
					userDao.save(user);
					TransactionDetailsDmo td = new TransactionDetailsDmo();
					td.setCreateTime(new Date());
					td.setDescription("【" + roomName + "】取消，解冻保证金");
					td.setUser(user);
					td.setMoney(roomDmo.getMoney());
					transactionDetailsDao.save(td);
				}
			}
			if (roomMemberDmo.getMember().getId().longValue() != roomDmo.getManager().getId().longValue()) {
				new Thread(() -> {
					try {
						rongCloud.sendSystemTextMsgToOne(roomMemberDmo.getMember().getId(),
								new TxtMessage("您加入的房间【" + roomDmo.getName() + "】已解散", ""));
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}).start();
			}
			roomMemberDao.delete(roomMemberDmo);
		}
		new Thread(() -> {
			try {
				Thread.sleep(5000);
				rongCloud.destoryChatRoom(Keys.Room.PREFIX + roomDmo.getId());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		jdbc.update("delete from join_room_log where room_id="+roomDmo.getId());
		roomDao.delete(roomDmo.getId());
	}

	/**
	 * 分得保证金
	 * 
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void getRoomFee(RoomDmo room, UserDmo user, int addMoney) {
		if (addMoney == 0) {
			return;
		}
		user.setAmount(user.getAmount() + addMoney);
		userDao.save(user);
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setMoney(addMoney);
		tran.setCreateTime(new Date());
		tran.setDescription("【" + room.getName() + "】结束，分得保证金");
		tran.setUser(user);
		transactionDetailsDao.save(tran);
		String m = String.format("%.2f", Float.valueOf(addMoney / 100.0 + ""));
		new Thread(() -> {
			try {
				rongCloud.sendSystemTextMsgToOne(user.getId(),
						new TxtMessage("【" + room.getName() + "】活动结束，分得保证金" + m + "元", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	public void luckDraw(UserDmo user, int money) {
		if (money <= 0) {
			return;
		}
		user.setAmount(user.getAmount() + money);
		this.userDao.save(user);
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setCreateTime(new Date());
		tran.setCalc(true);
		tran.setDescription("抽奖获取");
		tran.setMoney(money);
		tran.setUser(user);
		this.transactionDetailsDao.save(tran);
	}

	/**
	 * 管理员取消准备
	 * 
	 * @param roomDmo
	 * @throws Exception
	 */
	public void managerCancelRoom(RoomDmo roomDmo) throws Exception {
		List<RoomMemberDmo> roomMemberList = this.roomMemberDao.findByRoom_IdAndMember_IdNot(roomDmo.getId(),
				roomDmo.getManager().getId());
		String roomName = roomDmo.getName();
		for (RoomMemberDmo roomMemberDmo : roomMemberList) {
			if (!roomMemberDmo.isReady()) {
				continue;
			}
			roomMemberDmo.setReady(false);
			if (roomDmo.getMoney() > 0) {
				int money = roomDmo.getMoney();
				UserDmo user = roomMemberDmo.getMember();
				user.setAmount(user.getAmount() + money);
				user.setLockAmount(user.getLockAmount() - money);
				if (user.getLockAmount() < 0 || user.getAmount() < 0) {
					log.error("您的余额和冻结资金不能小于0");
					throw new Exception("您的余额和冻结资金不能小于0");
				}
				userDao.save(user);
				TransactionDetailsDmo td = new TransactionDetailsDmo();
				td.setUser(user);
				td.setDescription("【" + roomName + "】取消，解冻保证金");
				td.setMoney(money);
				td.setCreateTime(new Date());
				transactionDetailsDao.save(td);
				roomMemberDmo.setLockMoney(false);
				this.roomMemberDao.save(roomMemberDmo);
			}
		}
		roomDmo.setState(ActivityStates.新建.ordinal());
		roomDmo.setPrepareTime(null);
		this.roomDao.save(roomDmo);

	}

	/**
	 * 支付宝退款
	 * 
	 * @param id
	 * @param outTradeNo
	 * @param tradeNo
	 * @param refundAmount
	 * @param outTradeNo2
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void refundAlipay(long id, String outTradeNo, String tradeNo, int refundAmount, String outTradeNo2)
			throws Exception {
		PayOrderDmo order = this.payOrderDao.findOne(id);
		order.setRefundAmount(order.getRefundAmount() + refundAmount);
		if (order.getTotalAmount() < order.getRefundAmount()) {
			log.error("余额不足");
			throw new Exception("余额不足");
		}
		Long userId = Long.valueOf(order.getOutTradeNo().split("a")[0]);
		UserDmo user = userDao.findOne(userId);
		if (user.getAmount().intValue() < refundAmount) {
			log.error("余额不足");
			throw new Exception("余额不足");
		}
		user.setAmount(user.getAmount() - refundAmount);
		userDao.save(user);
		TransactionDetailsDmo dmo = new TransactionDetailsDmo();
		dmo.setCreateTime(new Date());
		dmo.setDescription("支付宝退款");
		dmo.setMoney(0 - refundAmount);
		dmo.setUser(user);
		transactionDetailsDao.save(dmo);
		payOrderDao.save(order);
		PayOrderDmo order2 = new PayOrderDmo();
		order2.setTotalAmount(0);
		order2.setNotifyTime(new Date());
		order2.setOutTradeNo(outTradeNo2);
		order2.setRefundAmount(refundAmount);
		order2.setTradeNo(tradeNo);
		order2.setType(Keys.ALIPAY_TYPE);
		this.payOrderDao.save(order2);

	}

	/**
	 * 微信退款
	 * 
	 * @param id
	 * @param user
	 * @param refundFee
	 * @param transactionId
	 * @param outTradeNo
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void refundWeixin(long id, UserDmo user, int refundFee, String transactionId, String outTradeNo)
			throws Exception {
		if (user.getAmount().intValue() < refundFee) {
			log.error("余额不足");
			throw new Exception("余额不足");
		}
		user.setAmount(user.getAmount() - refundFee);
		userDao.save(user);
		PayOrderDmo order = this.payOrderDao.findOne(id);
		if (order.getTotalAmount() < refundFee) {
			log.error("余额不足");
			throw new Exception("余额不足");
		}
		int refundAmount = Integer.valueOf(order.getRefundAmount()) + refundFee;
		order.setRefundAmount(refundAmount);
		this.payOrderDao.save(order);
		PayOrderDmo order2 = new PayOrderDmo();
		order2.setNotifyTime(new Date());
		order2.setTradeNo(transactionId);
		order2.setOutTradeNo(outTradeNo);
		order2.setTotalAmount(0);
		order2.setType(Keys.WEIXIN_TYPE);
		order2.setRefundAmount(refundFee);
		this.payOrderDao.save(order2);
		TransactionDetailsDmo dmo = new TransactionDetailsDmo();
		dmo.setCreateTime(new Date());
		dmo.setDescription("微信退款");
		dmo.setMoney(0 - refundFee);
		dmo.setUser(user);
		transactionDetailsDao.save(dmo);

	}

	/**
	 * 活动结束，缺席扣款
	 * 
	 * @param rm
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void roomLateFine(RoomMemberDmo rm) {
		UserDmo userDmo = rm.getMember();
		RoomDmo room = rm.getRoom();
		String m = String.format("%.2f", Float.valueOf(room.getMoney() / 100.0 + ""));
		userDmo.setLockAmount(userDmo.getLockAmount() - room.getMoney());
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setMoney(0 - room.getMoney());
		tran.setCreateTime(new Date());
		tran.setDescription("【" + room.getName() + "】迟到，扣除冻结保证金");
		tran.setUser(userDmo);
		tran.setCalc(false);
		this.userDao.save(userDmo);
		rm.setLockMoney(false);
		this.roomMemberDao.save(rm);
		this.transactionDetailsDao.save(tran);
		new Thread(() -> {
			Map<String, Object> map = new HashMap<>();
			map.put("type", "evaluation");
			map.put("id", room.getId() + "");
			try {
				rongCloud.sendSystemTextMsgToOne(userDmo.getId(),
						new TxtMessage("【" + room.getName() + "】迟到，扣除冻结保证金" + m + "元", Formatter.gson.toJson(map)));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	/**
	 * 房主退出房间
	 * 
	 * @param roomMemberDmo
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void roomManagerQuit(RoomMemberDmo roomMemberDmo) throws Exception {
		UserDmo user = roomMemberDmo.getMember();
		int roomMoney = roomMemberDmo.getRoom().getMoney();
		user.setAmount(user.getAmount() + roomMoney);
		user.setLockAmount(user.getLockAmount() - roomMoney);
		if (user.getAmount() < 0 || user.getLockAmount() < 0) {
			log.error("用户的保证金或者冻结资金不能小于0");
			throw new Exception("用户的保证金或者冻结资金不能小于0");
		}
		if (roomMemberDmo.isReady() == false) {
			log.error("房主必须为准备状态");
			throw new Exception("房主必须为准备状态");
		}
		String roomName = roomMemberDmo.getRoom().getName();
		roomMemberDao.delete(roomMemberDmo.getId());
		if (roomMoney > 0) {
			TransactionDetailsDmo td = new TransactionDetailsDmo();
			td.setCreateTime(new Date());
			td.setUser(user);
			td.setMoney(roomMoney);
			td.setDescription("【" + roomName + "】取消，解冻保证金");
			transactionDetailsDao.save(td);
		}
		userDao.save(user);
	}

	/**
	 * 房间成员退出
	 * 
	 * @param roomMemberDmo
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class, value = TxType.REQUIRED)
	public void roomMemberQuit(RoomMemberDmo roomMemberDmo) throws Exception {
		UserDmo user = roomMemberDmo.getMember();
		int roomMoney = roomMemberDmo.getRoom().getMoney();
		String roomName = roomMemberDmo.getRoom().getName();
		if (roomMemberDmo.isReady()) {
			user.setAmount(user.getAmount() + roomMoney);
			user.setLockAmount(user.getLockAmount() - roomMoney);
			if (user.getAmount() < 0 || user.getLockAmount() < 0) {
				log.error("用户的金额和冻结金额不能小于0");
				throw new Exception("用户的金额和冻结金额不能小于0");
			}
			userDao.save(user);
			if (roomMoney > 0) {
				TransactionDetailsDmo td = new TransactionDetailsDmo();
				td.setCreateTime(new Date());
				td.setMoney(roomMoney);
				td.setUser(user);
				td.setDescription("【" + roomName + "】取消，解冻保证金");
				transactionDetailsDao.save(td);
			}
		}
		this.roomMemberDao.delete(roomMemberDmo);
	}

	/**
	 * 成员准备活动
	 * 
	 * @param roomMember
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void roomReady(RoomMemberDmo roomMember) {
		UserDmo user = roomMember.getMember();
		RoomDmo currentRoom = roomMember.getRoom();
		roomMember.setReady(true);
		String roomName = roomMember.getRoom().getName();
		int money = currentRoom.getMoney();
		if (money > 0) {
			user.setAmount(user.getAmount() - money);
			user.setLockAmount(user.getLockAmount() + money);
			TransactionDetailsDmo td = new TransactionDetailsDmo();
			td.setUser(user);
			td.setDescription("【" + roomName + "】准备，冻结保证金。");
			td.setMoney(0 - money);
			td.setCreateTime(new Date());
			transactionDetailsDao.save(td);
			roomMember.setLockMoney(true);
		}
		roomMemberDao.save(roomMember);
		userDao.save(user);
	}

	/**
	 * 活动结束，解冻保证金
	 * 
	 * @param rm
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void roomThaw(RoomMemberDmo rm) {
		UserDmo userDmo = rm.getMember();
		RoomDmo room = rm.getRoom();
		String m = String.format("%.2f", Float.valueOf(room.getMoney() / 100.0 + ""));
		userDmo.setAmount(userDmo.getAmount() + room.getMoney());
		userDmo.setLockAmount(userDmo.getLockAmount() - room.getMoney());
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setMoney(room.getMoney());
		tran.setCreateTime(new Date());
		tran.setDescription("【" + room.getName() + "】结束，解冻保证金");
		tran.setUser(userDmo);
		this.userDao.save(userDmo);
		rm.setLockMoney(false);
		this.roomMemberDao.save(rm);
		this.transactionDetailsDao.save(tran);
		new Thread(() -> {
			Map<String, Object> map = new HashMap<>();
			map.put("type", "evaluation");
			map.put("id", room.getId() + "");
			try {
				rongCloud.sendSystemTextMsgToOne(userDmo.getId(),
						new TxtMessage("【" + room.getName() + "】结束，解冻保证金" + m + "元", Formatter.gson.toJson(map)));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	/**
	 * 支付宝充值
	 * 
	 * @param userId
	 * @param tradeNo
	 * @param outTradeNo
	 * @param totalAmount
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void saveAlipayAmount(Long userId, String tradeNo, String outTradeNo, int totalAmount, Date notifyTime)
			throws Exception {
		int fee = (int) Math.round(totalAmount * 0.01);
		PayOrderDmo order = new PayOrderDmo();
		order.setNotifyTime(notifyTime);
		order.setTradeNo(tradeNo);
		order.setOutTradeNo(outTradeNo);
		order.setTotalAmount(totalAmount);
		order.setType(Keys.ALIPAY_TYPE);
		if (fee == 0) {
			order.setRefundAmount(0);
		} else {
			order.setRefundAmount(fee);
			PayOrderDmo order2 = new PayOrderDmo();
			order2.setNotifyTime(order.getNotifyTime());
			order2.setOutTradeNo(userId + "c" + System.currentTimeMillis());
			order2.setTotalAmount(0);
			order2.setRefundAmount(fee);
			order2.setTradeNo(tradeNo);
			order2.setType(Keys.ALIPAY_TYPE);
			this.payOrderDao.save(order2);
		}
		UserDmo user = userDao.findOne(userId);
		if (user == null) {
			log.error("用户不存在");
			throw new Exception("用户不存在");
		}
		user.setAmount(user.getAmount() + totalAmount - fee);
		userDao.save(user);
		this.payOrderDao.save(order);
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setDescription("支付宝充值");
		tran.setCreateTime(new Date());
		tran.setMoney(totalAmount);
		tran.setUser(user);
		transactionDetailsDao.save(tran);
		if (fee != 0) {
			TransactionDetailsDmo tran2 = new TransactionDetailsDmo();
			tran2.setDescription("支付宝充值服务费");
			tran2.setCreateTime(new Date());
			tran2.setMoney(0 - fee);
			tran2.setUser(user);
			transactionDetailsDao.save(tran2);
		}
	}

	/**
	 * 系统账户分得资金
	 * 
	 * @param money
	 * @param systemUser
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void saveSystemFee(int money, UserDmo systemUser) {
		systemUser.setAmount(systemUser.getAmount() + money);
		this.userDao.save(systemUser);
	}

	/**
	 * 微信充值
	 * 
	 * @param userId
	 * @param outTradeNo
	 * @param totalFee
	 * @param transactionId
	 * @param createTime
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void saveWeixinAmount(long userId, String outTradeNo, int totalFee, String transactionId, Date createTime) {
		UserDmo user = this.userDao.findOne(userId);
		user.setAmount(user.getAmount() + totalFee);
		this.userDao.save(user);
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setCreateTime(createTime);
		tran.setMoney(totalFee);
		tran.setUser(user);
		tran.setDescription("微信充值");
		this.transactionDetailsDao.save(tran);
		PayOrderDmo order = new PayOrderDmo();
		order.setNotifyTime(createTime);
		order.setOutTradeNo(outTradeNo);
		order.setRefundAmount(0);
		order.setTotalAmount(totalFee);
		order.setTradeNo(transactionId);
		order.setType(Keys.WEIXIN_TYPE);
		this.payOrderDao.save(order);

	}

	/**
	 * 踢人
	 * 
	 * @param roomMember
	 *            被踢的人和房间连接
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void tiRen(RoomMemberDmo roomMember) {
		UserDmo member = roomMember.getMember();
		RoomDmo room = roomMember.getRoom();
		String roomName = room.getName();
		if (roomMember.isReady()) {
			int lockAmount = room.getMoney();
			member.setLockAmount(member.getLockAmount() - lockAmount);
			member.setAmount(member.getAmount() + lockAmount);
			if (lockAmount > 0) {
				TransactionDetailsDmo td = new TransactionDetailsDmo();
				td.setUser(member);
				td.setCreateTime(new Date());
				td.setDescription("【" + roomName + "】取消，解冻保证金");
				td.setMoney(lockAmount);
				transactionDetailsDao.save(td);
			}
		}
		if (member.getGender()) {
			room.setJoinManMember(room.getJoinManMember() - 1);
		} else {
			room.setJoinWomanMember(room.getJoinWomanMember() - 1);
		}
		room.setJoinMember(room.getJoinMember() - 1);
		roomDao.save(room);
		roomMemberDao.delete(roomMember);
		this.userDao.save(member);
	}

	@Autowired
	PropBo propBo;

	@Autowired
	PropDao propDao;

	/**
	 * 跟新房间
	 * 
	 * @param managerId
	 *            管理员id
	 * @param room
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void updateRoom(long managerId, RoomDmo room) {
		List<RoomMemberDmo> roomMemberList = this.roomMemberDao.findByRoom_IdAndMember_IdNot(room.getId(), managerId);
		if (room.getManager().isSuperUser() && room.getGame().getId().intValue() == 22) {
			for (RoomMemberDmo roomMemberDmo : roomMemberList) {
				if (!roomMemberDmo.isReady()) {
					continue;
				}
				PropDmo prop = this.propBo.findByUser_Id(roomMemberDmo.getMember().getId());
				prop.setRemainMovieTicket(prop.getRemainMovieTicket() + 1);
				this.propDao.save(prop);
			}
		}
		String roomName = room.getName();
		if (room.getMoney() != 0) {
			for (RoomMemberDmo roomMemberDmo : roomMemberList) {
				if (!roomMemberDmo.isReady()) {
					continue;
				}
				UserDmo userDmo = roomMemberDmo.getMember();
				userDmo.setAmount(userDmo.getAmount() + room.getMoney());
				userDmo.setLockAmount(userDmo.getLockAmount() - room.getMoney());
				TransactionDetailsDmo tran = new TransactionDetailsDmo();
				tran.setCreateTime(new Date());
				tran.setDescription("【" + roomName + "】取消，解冻保证金");
				tran.setMoney(room.getMoney());
				tran.setUser(userDmo);
				this.transactionDetailsDao.save(tran);
			}
		}
		for (RoomMemberDmo roomMemberDmo : roomMemberList) {
			roomMemberDmo.setReady(false);
		}
		this.roomMemberDao.save(roomMemberList);
	}

	public void validateMoney(UserDmo user) throws Exception {
		int transactionSumAmount = this.jdbc.queryForObject(
				"select sum(money) from transaction_details where user=? and is_calc=1", new Object[] { user.getId() },
				Integer.class);
		if (transactionSumAmount != user.getAmount()) {
			throw new Exception("您的余额与交易记录不匹配");
		}
	}

	/**
	 * 提现
	 * 
	 * @param user
	 * @param money
	 */
	public void withdrawals(UserDmo user, int money) {
		user.setAmount(user.getAmount() - money);
		user.setLockAmount(user.getLockAmount() + money);
		TransactionDetailsDmo tran = new TransactionDetailsDmo();
		tran.setCreateTime(new Date());
		tran.setDescription("支付宝提现处理中");
		tran.setMoney(0 - money);
		tran.setUser(user);
		this.transactionDetailsDao.save(tran);
	}

	@Autowired
	WithdrawalsDao withdrawalsDao;

	@Transactional(rollbackOn = Throwable.class)
	public Map<String, String> withdrawalsV2(UserDmo user, String alipayAccount, int money) throws Exception {
		String withdrawalsMoney = String.format("%.2f", money / 100.0);
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", Keys.Alipay.APP_ID,
				Keys.Alipay.APP_PRIVATE_KEY, "json", "UTF-8", Keys.Alipay.ALIPAY_PUBLIC_KEY, "RSA2");
		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
		String outBizNo = user.getId() + "d" + System.currentTimeMillis();
		request.setBizContent("{" + "\"out_biz_no\":\"" + outBizNo + "\"," + "\"payee_type\":\"ALIPAY_LOGONID\","
				+ "\"payee_account\":\"" + alipayAccount + "\"," + "\"amount\":\"" + withdrawalsMoney + "\","
				+ "\"payer_show_name\":\"后会有期\"," + "\"payee_real_name\":\"" + user.getRealName() + "\","
				+ "\"remark\":\"后会有期app提现\"" + "  }");
		AlipayFundTransToaccountTransferResponse response = alipayClient.execute(request);
		if (response.isSuccess()) {
			Map<String, String> result = new HashMap<>();
			result.put("outBizNo", response.getOutBizNo());
			result.put("orderId", response.getOrderId());
			return result;
		} else {
			log.error("提现ERROR " + response.getSubCode());
			new Thread(() -> {
				try {
					this.rongCloud.sendSystemTextMsgToOne(user.getId().longValue(),
							new TxtMessage(response.getSubCode() + "，或截图至意见反馈。", null));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
			throw new Exception(response.getSubMsg());
		}

	}

	public boolean isMoneyError(UserDmo user) {
		int userAmount = user.getAmount();
		int sumTran = 0;
		String sql = "select count(t.money) c from transaction_details t where t.is_calc=1 and t.user=?";
		List<Integer> list = this.jdbc.query(sql, (ResultSet rs, int num) -> {
			return rs.getInt("c");
		}, user.getId());
		int count = list.isEmpty() ? 0 : list.get(0);
		if (count != 0) {
			sql = "select sum(t.money) s from transaction_details t where t.is_calc=1 and t.user=?";
			list = jdbc.query(sql, (ResultSet rs, int num) -> {
				return rs.getInt("s");
			}, user.getId());
			sumTran = list.isEmpty() ? 0 : list.get(0);
		}
		return userAmount != sumTran;
	}

	@Transactional(rollbackOn = Throwable.class)
	public void cancelRoomReadyV2(RoomMemberDmo roomMember) throws Exception {
		RoomDmo room = roomMember.getRoom();
		UserDmo user = roomMember.getMember();
		int count = jdbc.update("update room_member set ready=0 and is_lock_money=0 where id=?", roomMember.getId());
		if (count == 0) {
			log.error("userId:" + user.getId() + ",roomId:" + room.getId());
			throw new Exception("取消失败，请申诉。");
		}
		count = jdbc.update("update room set prepare_time = null where id=?", room.getId());
		if (count == 0) {
			log.error("userId:" + user.getId() + ",roomId:" + room.getId());
			throw new Exception("取消失败，请申诉。");
		}
		int money = roomMember.getRoom().getMoney();
		if (money > 0) {
			TransactionDetailsDmo td = new TransactionDetailsDmo();
			td.setCreateTime(new Date());
			td.setDescription("【" + room.getName() + "】取消准备，解冻保证金");
			td.setMoney(money);
			td.setUser(user);
			td.setCalc(true);
			count = this.jdbc.update(
					"INSERT INTO transaction_details(description, money, create_time, user, is_calc) VALUES (?, ?, ?,?, ?)",
					td.getDescription(), td.getMoney(), td.getCreateTime(), td.getUser().getId(), td.isCalc());
			if (count == 0) {
				log.error("userId:" + user.getId() + ",roomId:" + room.getId());
				throw new Exception("取消失败，请申诉。");
			}
			user.setLockAmount(user.getLockAmount() - money);
			user.setAmount(user.getAmount() + money);
			if (user.getLockAmount() < 0 || user.getAmount() < 0) {
				log.error("userId:" + user.getId() + ",roomId:" + room.getId());
				throw new Exception("您的余额和冻结资金不能小于0");
			}
			count = jdbc.update("update user set amount=? and lock_amount=? where id=?", user.getAmount(),
					user.getLockAmount(), user.getId());
			if (count == 0) {
				log.error("userId:" + user.getId() + ",roomId:" + room.getId());
				throw new Exception("取消失败，请申诉。");
			}
		}
	}
}
