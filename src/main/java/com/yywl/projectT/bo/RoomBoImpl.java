package com.yywl.projectT.bo;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yywl.projectT.bean.DateFactory;
import com.yywl.projectT.bean.DistanceConverter;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.FriendPointUtil;
import com.yywl.projectT.bean.JpushBean;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.MD5Util;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.bean.enums.FriendOriginEnum;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dao.ComplaintDao;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.GameDao;
import com.yywl.projectT.dao.GameScoreDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.JoinRoomLogDao;
import com.yywl.projectT.dao.LocationDao;
import com.yywl.projectT.dao.NotLateReasonDao;
import com.yywl.projectT.dao.PropDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomEvaluationDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.SpreadUserDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserCircleDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.CircleDmo;
import com.yywl.projectT.dmo.FriendDmo;
import com.yywl.projectT.dmo.GameDmo;
import com.yywl.projectT.dmo.GameScoreDmo;
import com.yywl.projectT.dmo.JoinRoomLogDmo;
import com.yywl.projectT.dmo.LocationDmo;
import com.yywl.projectT.dmo.NotLateReasonDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.RoomEvaluationDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.RoomMemberVo;
import com.yywl.projectT.vo.TempEvaluationVo;

import io.rong.messages.CmdMsgMessage;
import io.rong.messages.InfoNtfMessage;
import io.rong.messages.TxtMessage;

@Service
public class RoomBoImpl implements RoomBo {

	@Autowired
	UserBo userBo;

	@Autowired
	RoomDao roomDao;

	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	UserDao userDao;

	@Autowired
	GameDao gameDao;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	RoleBo roleBo;

	@Autowired
	FriendDao friendDao;

	@Autowired
	Keys keys;

	@Autowired
	RongCloudBean rongCloud;

	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Autowired
	LocationDao locationDao;

	/**
	 * 校验同时所在的房间
	 * 
	 * @param user
	 * @throws Exception
	 */
	public void validateJoinAllCounts(long userId) throws Exception {
		long count = this.roomMemberDao.countByMember_IdAndRoom_StateInAndGame_IdNot(userId,
				new Integer[] { ActivityStates.新建.ordinal(), ActivityStates.准备中.ordinal() }, 30);
		if (count >= Keys.Room.JOIN_MAX) {
			log.error("最多创建/加入" + Keys.Room.JOIN_MAX + "个房间");
			throw new Exception("最多创建/加入" + Keys.Room.JOIN_MAX + "个房间");
		}
	}

	public void validateJoinTodayCounts(long userId, Date beginTime) throws Exception {
		boolean isVip = jdbcDao.isVip(userId);
		long count = this.roomMemberDao.countByRoom_BeginTimeBetweenAndMember_IdAndAndRoom_StateInAndGame_IdNot(
				DateFactory.getStartTime(beginTime), DateFactory.getEndTime(beginTime), userId,
				new Integer[] { ActivityStates.新建.ordinal(), ActivityStates.准备中.ordinal() }, 30);
		if (isVip) {
			if (count >= Keys.Room.VIP_JOIN_TODAY_MAX) {
				throw new Exception("会员每天最多只能创建/加入" + Keys.Room.VIP_JOIN_TODAY_MAX + "场活动");
			}
		} else {
			if (count >= Keys.Room.JOIN_TODAY_MAX) {
				throw new Exception("每天最多只能创建/加入" + Keys.Room.JOIN_TODAY_MAX + "场活动");
			}
		}
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void managerCancelRoom(long userId, String token, long roomId) throws Exception {
		this.userBo.loginByToken(userId, token);
		RoomDmo roomDmo = this.roomDao.findOne(roomId);
		if (null == roomDmo) {
			log.error("房间不存在");
			throw new Exception("房间不存在");
		}
		if (roomDmo.getManager().getId().longValue() != userId) {
			log.error("您没有权限");
			throw new Exception("您没有权限");
		}
		if (roomDmo.getState() > ActivityStates.准备中.ordinal()) {
			log.error("无法取消");
			throw new Exception("无法取消");
		}
		if (roomDmo.getState() == ActivityStates.准备中.ordinal()) {
			log.error("房间已准备，无法取消");
			throw new Exception("房间已准备，无法取消");
		}
		this.moneyTransactionBo.managerCancelRoom(roomDmo);
		new Thread(() -> {
			try {
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new InfoNtfMessage("房主已取消活动开始状态", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	private static final Log log = LogFactory.getLog(RoomBoImpl.class);

	@Override
	@Async
	public void begin(UserDmo user, RoomDmo room, long now) throws Exception {
		long roomId = room.getId();
		long beginTime = room.getBeginTime().getTime();
		room.setPrepareTime(new Date(now + 2 * 60 * 1000 < beginTime ? now + 2 * 60 * 1000 : beginTime));
		roomDao.save(room);
		room = this.roomDao.findOne(room.getId());
		long t = room.getPrepareTime().getTime();
		new Thread(() -> {
			try {
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new InfoNtfMessage("活动准备开始，2分钟内可以反悔，取消准备，一旦超出两分钟，将无法退出", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		long time = Math.min(2 * 61 * 1000, room.getBeginTime().getTime() - System.currentTimeMillis() - 1000);
		Thread.sleep(time < 0 ? 0 : time);
		room = this.roomDao.findOne(room.getId());
		if (room.getPrepareTime() != null && room.getPrepareTime().getTime() == t) {
			room.setState(ActivityStates.准备中.ordinal());
			this.roomDao.save(room);
			new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new InfoNtfMessage("活动已准备完成，请大家准时参加。", ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}

			}).start();
		}
	}

	@Autowired
	private MoneyTransactionBo moneyTransactionBo;

	@Autowired
	UserCircleDao userCircleDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public RoomDmo createRoom(long userId, String token, String name, String password, String beginTime, String endTime,
			String place, int money, int memberCount, int manCount, int womanCount, String description,
			double longitude, double latitude, int gameId, String city, long belongCircle, boolean open, int gameMode)
			throws Exception {
		if (token == null) {
			log.error("请登录");
			throw new Exception("请登录");
		}
		if (StringUtils.isEmpty(city)) {
			log.error("请选择城市");
			throw new Exception("请选择城市");
		}
		if (StringUtils.isEmpty(place)) {
			log.error("活动地点不能为空");
			throw new Exception("活动地点不能为空");
		}
		if (memberCount != 0 && memberCount < 2) {
			log.error("房间人数不能小于2");
			throw new Exception("房间人数不能小于2");
		}
		if (manCount != 0 && womanCount != 0 && memberCount != (manCount + womanCount)) {
			log.error("男女人数限制不等于总和");
			throw new Exception("男女人数限制不等于总和");
		}
		if (StringUtils.isEmpty(description)) {
			description = "暂无介绍";
		}
		UserDmo user = this.userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			log.error("您还没有初始化");
			throw new Exception("您还没有初始化");
		}
		if (user.getGender() == null) {
			log.error("请先设置性别");
			throw new Exception("请先设置性别");
		}
		if (manCount != 0 || womanCount != 0) {
			if (user.getGender()) {
				if (manCount < 1) {
					log.error("男性人数不能小于1");
					throw new Exception("男性人数不能小于1");
				}
			} else {
				if (womanCount < 1) {
					log.error("女性人数不能小于1");
					throw new Exception("女性人数不能小于1");
				}
			}
			if (womanCount + manCount != memberCount) {
				log.error("人数不正确");
				throw new Exception("人数不正确");
			}
		}
		if (money < 0) {
			log.error("房间的保证金不能为负数");
			throw new Exception("房间的保证金不能为负数");
		}
		if (user.getAmount() < money) {
			log.error("保证金不足");
			throw new Exception("保证金不足");
		}
		if (!user.isSuperUser()) {
			this.validateJoinAllCounts(userId);
		}
		RoomDmo room = new RoomDmo();
		room.setGameMode(gameMode);
		room.setOpen(open);
		room.setCity(city);
		if (StringUtils.isEmpty(name)) {
			log.error("房间名不能为空");
			throw new Exception("房间名不能为空");
		}
		room.setName(name);
		if (!StringUtils.isEmpty(password)) {
			room.setPassword(MD5Util.getSecurityCode(password));
			room.setLocked(true);
		} else {
			room.setLocked(false);
		}
		if (StringUtils.isEmpty(beginTime) || StringUtils.isEmpty(endTime)) {
			log.error("请设置时间");
			throw new Exception("请设置时间");
		}
		Date begin = Formatter.dateTimeFormatter.parse(beginTime);
		if (!user.isSuperUser()) {
			this.validateJoinTodayCounts(userId, begin);
		}
		if (begin.before(new Date())) {
			log.error("开始时间不能小于当前时间");
			throw new Exception("开始时间不能小于当前时间");
		}
		if (!begin.after(new Date(System.currentTimeMillis() + 1000 * 60 * 10))) {
			log.error("开始时间必须在当前时间后10分钟");
			throw new Exception("开始时间必须在当前时间后10分钟");
		}
		room.setBeginTime(begin);
		Date end = Formatter.dateTimeFormatter.parse(endTime);
		if (!end.after(begin)) {
			log.error("结束时间必须在开始时间之后");
			throw new Exception("结束时间必须在开始时间之后");
		}
		room.setEndTime(end);
		long time = room.getEndTime().getTime() + 16 * 60 * 60 * 1000L;
		room.setEvaluateTime(new Date(time));
		boolean isVip = this.jdbcDao.isVip(userId);
		if (!isVip && !user.isSuperUser()) {
			String str = beginTime.split(" ")[0];
			Date date1 = Formatter.dateFormatter.parse(str);
			Date date2 = new Date(date1.getTime() + 24 * 60 * 60 * 1000);
			long count = roomDao.countByManager_IdAndBeginTimeBetweenAndGame_IdNot(userId, date1, date2, 30);
			if (count >= Keys.Room.MAX_ROOM_COUNT) {
				log.error("当天只能举行" + Keys.Room.MAX_ROOM_COUNT + "场活动");
				throw new Exception("当天只能举行" + Keys.Room.MAX_ROOM_COUNT + "场活动");
			}
		}
		if (!user.isSuperUser()) {
			List<RoomMemberDmo> roomMemberDmos = roomMemberDao.findByMember_IdAndReadyAndRoom_Game_IdNot(userId, true,
					30);
			for (RoomMemberDmo dmo : roomMemberDmos) {
				RoomDmo r = dmo.getRoom();
				if (r.getBeginTime().before(room.getEndTime()) && r.getEndTime().after(room.getBeginTime())) {
					log.error("您在此时间已经参加了[" + r.getName() + "],请不要贪心噢!");
					throw new Exception("您在此时间已经参加了[" + r.getName() + "],请不要贪心噢!");
				}
			}
		}
		if (belongCircle != 0) {
			CircleDmo belongCircleDmo = this.circleDao.findOne(belongCircle);
			if (null != belongCircleDmo) {
				room.setBelongCircle(belongCircleDmo);
			}
		}
		room.setPlace(place);
		room.setMoney(money);
		room.setMemberCount(memberCount);
		room.setManager(user);
		room.setManCount(manCount);
		room.setWomanCount(womanCount);
		room.setDescription(description);
		room.setGame(gameDao.findOne(gameId));
		if (null == room.getGame()) {
			log.error("请选择活动分类");
			throw new Exception("请选择活动分类");
		}
		room.setState(ActivityStates.新建.ordinal());
		room.setCreateTime(new Date());
		room.setLatitude(latitude);
		room.setLongitude(longitude);
		room.setJoinMember(1);
		if (user.getGender()) {
			room.setJoinManMember(1);
		} else {
			room.setJoinWomanMember(1);
		}
		this.roomDao.save(room);
		this.moneyTransactionBo.becomeRoomManager(user, room);
		this.joinRoomLogDao.save(new JoinRoomLogDmo(null, user.getId(), room.getId(), new Date()));
		new Thread(() -> {
			try {
				rongCloud.createChatRoom(room.getId() + "", room.getName());
				rongCloud.createChatRoom("cmd" + room.getId() + "", room.getName());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		return room;
	}

	@Override
	public List<RoomDmo> findMyCreateRooms(Long userId, String token, int page, int size) throws Exception {
		UserDmo user = this.userBo.loginByToken(userId, token);
		Page<RoomDmo> roomPageList = this.roomDao.findByManager_IdAndStateNotAndGame_IdNot(user.getId(),
				ActivityStates.已废弃.ordinal(), 30,
				new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC, "createTime"));
		List<RoomDmo> rooms = roomPageList.getContent();
		if (null != rooms && !rooms.isEmpty()) {
			String sql = "select rm.member,rm.nickname,rm.ready,u.avatar_signature,u.gender from room_member rm "
					+ "left join user u on u.id=rm.member where room=? ";
			for (RoomDmo roomDmo : rooms) {
				List<RoomMemberVo> members = jdbc.query(sql, new Object[] { roomDmo.getId() }, (rs, num) -> {
					RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getBoolean("ready"), rs.getString("avatar_signature"));
					vo.setGender(rs.getBoolean("gender"));
					return vo;
				});
				roomDmo.setJoinMembers(members);
			}
		}
		return rooms;
	}

	@Override
	public List<RoomDmo> findMyJoinRooms(UserDmo userDmo, int page, int size) throws Exception {
		StringBuilder builder = new StringBuilder(
				"SELECT r.is_anonymous,r.id,r.state,r.name,r.belong_circle,r.manager,r.place,r.begin_time,r.evaluate_time,");
		builder.append(" r.end_time,r.create_time,r.password,r.game,r.money,r.member_count,r.man_count,r.woman_count,");
		builder.append(" r.description,r.join_member,r.join_man_member,r.join_woman_member,r.longitude,r.latitude,");
		builder.append(
				" r.locked,r.city,r.prepare_time,r.open,r.game_mode,r.remaining_money,ABS(UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(r.begin_time)) timediff, ");
		builder.append(" g.name gname ");
		builder.append(" FROM room r inner join game g on g.id=r.game inner join room_member rm on rm.room=r.id ");
		builder.append(" where r.state<>5 and r.game<>30 and rm.member=" + userDmo.getId());
		builder.append(" order by r.state ,timediff");
		builder.append(" limit " + page * size + "," + size);
		List<RoomDmo> list = this.jdbc.query(builder.toString(), (ResultSet rs, int num) -> {
			RoomDmo dmo = new RoomDmo();
			dmo.setAnonymous(rs.getBoolean("is_anonymous"));
			dmo.setId(rs.getLong("id"));
			dmo.setState(rs.getInt("state"));
			dmo.setName(rs.getString("name"));
			dmo.setBelongCircle(new CircleDmo(rs.getLong("belong_circle")));
			dmo.setManager(new UserDmo(rs.getLong("manager")));
			dmo.setPlace(rs.getString("place"));
			dmo.setBeginTime(rs.getTimestamp("begin_time"));
			dmo.setEvaluateTime(rs.getTimestamp("evaluate_time"));
			dmo.setEndTime(rs.getTimestamp("end_time"));
			dmo.setCreateTime(rs.getDate("create_time"));
			dmo.setPassword(rs.getString("password"));
			dmo.setGame(new GameDmo(rs.getInt("game"), rs.getString("gname")));
			dmo.setMoney(rs.getInt("money"));
			dmo.setMemberCount(rs.getInt("member_count"));
			dmo.setManCount(rs.getInt("man_count"));
			dmo.setWomanCount(rs.getInt("woman_count"));
			dmo.setDescription(rs.getString("description"));
			dmo.setJoinMember(rs.getInt("join_member"));
			dmo.setJoinManMember(rs.getInt("join_man_member"));
			dmo.setJoinWomanMember(rs.getInt("join_woman_member"));
			dmo.setLongitude(rs.getDouble("longitude"));
			dmo.setLatitude(rs.getDouble("latitude"));
			dmo.setLocked(rs.getBoolean("locked"));
			dmo.setCity(rs.getString("city"));
			dmo.setPrepareTime(rs.getDate("prepare_time"));
			dmo.setOpen(rs.getBoolean("open"));
			dmo.setGameMode(rs.getInt("game_mode"));
			dmo.setRemainingMoney(rs.getInt("remaining_money"));
			return dmo;
		});
		for (RoomDmo room : list) {
			String sql = "select rm.member,rm.nickname,rm.ready,u.avatar_signature,u.gender from room_member rm "
					+ "left join user u on u.id=rm.member where room=" + room.getId();
			List<RoomMemberVo> members = jdbc.query(sql, (rs, num) -> {
				RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
						rs.getBoolean("ready"), rs.getString("avatar_signature"));
				vo.setGender(rs.getBoolean("gender"));
				return vo;
			});
			room.setJoinMembers(members);
		}
		return list;
	}

	@Override
	public RoomDmo findOne(Long id) throws Exception {
		RoomDmo room = this.roomDao.findOne(id);
		if (room == null) {
			log.error("房间不存在");
			throw new Exception("房间不存在");
		}
		String sql = "select u.gender,rm.member,rm.is_online,rm.nickname,rm.ready,u.avatar_signature,rm.is_attend,rm.is_signed from room_member rm "
				+ "left join user u on u.id=rm.member where room=? ";
		List<RoomMemberVo> members = jdbc.query(sql, new Object[] { room.getId() }, (rs, num) -> {
			RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"), rs.getBoolean("ready"),
					rs.getString("avatar_signature"));
			vo.setAttend(rs.getBoolean("is_attend"));
			vo.setSigned(rs.getBoolean("is_signed"));
			vo.setVip(this.jdbcDao.isVip(vo.getId()));
			vo.setOnline(rs.getBoolean("is_online"));
			vo.setGender(rs.getBoolean("gender"));
			return vo;
		});
		room.setJoinMembers(members);
		return room;
	}

	@Override
	public List<RoomDmo> findMyRunningRooms(long userId, Integer gameId) {
		List<RoomDmo> list;
		StringBuilder sql = new StringBuilder(
				" select r.is_anonymous,r.prepare_time,r.city, p.vip_expire_date>now() is_vip,").append(
						" r.id,r.name,r.place,r.manager,r.begin_time,r.end_time,r.state,r.create_time,r.belong_circle,r.open")
						.append(" ,r.game,r.money,r.member_count,r.man_count,r.woman_count,r.description")
						.append(" ,r.join_member,r.join_man_member,r.join_woman_member,r.latitude,r.longitude,r.locked,g.name game_name ")
						.append("  FROM room r left join game g on r.game=g.id ")
						.append(" left join user u on u.id=r.manager ").append(" left join prop p on p.user_id=u.id ")
						.append(" right join room_member rm on rm.room=r.id ")
						.append(" where rm.member=? and r.state<>5 and g.id<>30 ")
						.append(" and ((r.state in (1,2)) or (r.state=0 and (r.prepare_time is not null or (r.belong_circle is not null and r.open=0))))  ");
		if (null != gameId && gameId != 0) {
			sql.append(" and g.id=" + gameId);
		}
		sql.append(" order by r.state desc,r.prepare_time desc,r.begin_time asc ");
		list = jdbc.query(sql.toString(), new Object[] { userId }, (ResultSet resultSet, int rowNum) -> {
			RoomDmo room = new RoomDmo();
			room.setAnonymous(resultSet.getBoolean("is_anonymous"));
			room.setId(resultSet.getLong("id"));
			room.setName(resultSet.getString("name"));
			room.setPlace(resultSet.getString("place"));
			room.setManager(new UserDmo(resultSet.getLong("manager")));
			room.setBeginTime(resultSet.getTimestamp("begin_time"));
			room.setEndTime(resultSet.getTimestamp("end_time"));
			room.setState(resultSet.getInt("state"));
			room.setCreateTime(resultSet.getTimestamp("create_time"));
			GameDmo game = new GameDmo(resultSet.getInt("game"), resultSet.getString("game_name"));
			room.setGame(game);
			room.setMoney(resultSet.getInt("money"));
			room.setMemberCount(resultSet.getInt("member_count"));
			room.setManCount(resultSet.getInt("man_count"));
			room.setWomanCount(resultSet.getInt("woman_count"));
			room.setDescription(resultSet.getString("description"));
			room.setJoinMember(resultSet.getInt("join_member"));
			room.setJoinManMember(resultSet.getInt("join_man_member"));
			room.setJoinWomanMember(resultSet.getInt("join_woman_member"));
			room.setLatitude(resultSet.getDouble("latitude"));
			room.setLongitude(resultSet.getDouble("longitude"));
			room.setLocked(resultSet.getBoolean("locked"));
			long belongCircleId = resultSet.getLong("belong_circle");
			room.setBelongCircle(circleDao.findOne(belongCircleId));
			room.setOpen(resultSet.getBoolean("open"));
			room.setCity(resultSet.getString("city"));
			room.setPrepareTime(resultSet.getTimestamp("prepare_time"));
			return room;
		});
		sql = new StringBuilder(
				" select rm.member,rm.nickname,rm.ready,u.avatar_signature,u.gender from room_member rm ")
						.append(" left join user u on u.id=rm.member where room=? ");
		if (null != list && !list.isEmpty()) {
			for (RoomDmo roomDmo : list) {
				List<RoomMemberVo> members = jdbc.query(sql.toString(), new Object[] { roomDmo.getId() }, (rs, num) -> {
					RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getBoolean("ready"), rs.getString("avatar_signature"));
					vo.setGender(rs.getBoolean("gender"));
					return vo;
				});
				roomDmo.setJoinMembers(members);
			}
		}
		return list;
	}

	
	
	@Override
	public List<RoomDmo> findMyRunningRoomsV2(long userId, Integer gameId) {
		List<RoomDmo> list;
		StringBuilder sql = new StringBuilder(
				" select r.is_anonymous,r.prepare_time,r.city, p.vip_expire_date>now() is_vip,").append(
						" r.id,r.name,r.place,r.manager,r.begin_time,r.end_time,r.state,r.create_time,r.belong_circle,r.open")
						.append(" ,r.game,r.money,r.member_count,r.man_count,r.woman_count,r.description")
						.append(" ,r.join_member,r.join_man_member,r.join_woman_member,r.latitude,r.longitude,r.locked,g.name game_name ")
						.append("  FROM room r left join game g on r.game=g.id ")
						.append(" left join user u on u.id=r.manager ").append(" left join prop p on p.user_id=u.id ")
						.append(" right join room_member rm on rm.room=r.id ")
						.append(" where rm.member=? and r.state<>5 ")
						.append(" and ((r.state in (1,2)) or r.game=30 or (r.state=0 and (r.prepare_time is not null or (r.belong_circle is not null and r.open=0))))  ");
		if (null != gameId && gameId != 0) {
			sql.append(" and g.id=" + gameId);
		}
		sql.append(" order by r.join_member desc,r.state desc,r.prepare_time desc,r.begin_time asc ");
		list = jdbc.query(sql.toString(), new Object[] { userId }, (ResultSet resultSet, int rowNum) -> {
			RoomDmo room = new RoomDmo();
			room.setAnonymous(resultSet.getBoolean("is_anonymous"));
			room.setId(resultSet.getLong("id"));
			room.setName(resultSet.getString("name"));
			room.setPlace(resultSet.getString("place"));
			room.setManager(new UserDmo(resultSet.getLong("manager")));
			room.setBeginTime(resultSet.getTimestamp("begin_time"));
			room.setEndTime(resultSet.getTimestamp("end_time"));
			room.setState(resultSet.getInt("state"));
			room.setCreateTime(resultSet.getTimestamp("create_time"));
			GameDmo game = new GameDmo(resultSet.getInt("game"), resultSet.getString("game_name"));
			room.setGame(game);
			room.setMoney(resultSet.getInt("money"));
			room.setMemberCount(resultSet.getInt("member_count"));
			room.setManCount(resultSet.getInt("man_count"));
			room.setWomanCount(resultSet.getInt("woman_count"));
			room.setDescription(resultSet.getString("description"));
			room.setJoinMember(resultSet.getInt("join_member"));
			room.setJoinManMember(resultSet.getInt("join_man_member"));
			room.setJoinWomanMember(resultSet.getInt("join_woman_member"));
			room.setLatitude(resultSet.getDouble("latitude"));
			room.setLongitude(resultSet.getDouble("longitude"));
			room.setLocked(resultSet.getBoolean("locked"));
			long belongCircleId = resultSet.getLong("belong_circle");
			room.setBelongCircle(circleDao.findOne(belongCircleId));
			room.setOpen(resultSet.getBoolean("open"));
			room.setCity(resultSet.getString("city"));
			room.setPrepareTime(resultSet.getTimestamp("prepare_time"));
			return room;
		});
		sql = new StringBuilder(
				" select rm.member,rm.nickname,rm.ready,u.avatar_signature,u.gender from room_member rm ")
						.append(" left join user u on u.id=rm.member where room=? ");
		if (null != list && !list.isEmpty()) {
			for (RoomDmo roomDmo : list) {
				List<RoomMemberVo> members = jdbc.query(sql.toString(), new Object[] { roomDmo.getId() }, (rs, num) -> {
					RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getBoolean("ready"), rs.getString("avatar_signature"));
					vo.setGender(rs.getBoolean("gender"));
					return vo;
				});
				roomDmo.setJoinMembers(members);
			}
		}
		return list;
	}

	@Override
	public List<RoomDmo> findRoomsByGameOrder(int gameId, String games, Integer state, Double longitude,
			Double latitude, int page, int size, String city) throws Exception {
		List<RoomDmo> list;
		StringBuilder sql = new StringBuilder(" select r.is_anonymous,r.city, p.vip_expire_date>now() is_vip,").append(
				" r.id,r.name,r.place,r.manager,r.begin_time,r.end_time,r.state,r.create_time,r.belong_circle,r.open")
				.append(" ,r.game,r.money,r.member_count,r.man_count,r.woman_count,r.description")
				.append(" ,r.join_member,r.join_man_member,r.join_woman_member,r.latitude,r.longitude,r.locked,g.name game_name ")
				.append(" , (TIMESTAMPDIFF(HOUR,now(),r.begin_time)+distance_calc(r.longitude,r.latitude," + longitude
						+ "," + latitude + ")+r.member_count/r.join_member  ) as order_score")
				.append("  FROM room r left join game g on r.game=g.id ").append(" left join user u on u.id=r.manager ")
				.append(" left join prop p on p.user_id=u.id ").append(" WHERE g.id<>30 and r.state<>5 and r.prepare_time is null ");
		if (!StringUtils.isEmpty(city)) {
			sql.append(" and (r.city like '" + city + "%' ) ");
		}
		if (gameId != 0) {
			sql.append(" and r.game=" + gameId + " ");
		}
		if (!StringUtils.isEmpty(games) && gameId == 0) {
			sql.append(" and r.game in (" + games + ")");
		}
		if (state != null) {
			sql.append(" and r.state=" + state + " ");
		}
		sql.append(" and open=1 ");
		sql.append(" ORDER BY order_score asc LIMIT " + page * size + "," + size + " ");
		list = jdbc.query(sql.toString(), (ResultSet resultSet, int rowNum) -> {
			RoomDmo room = new RoomDmo();
			room.setAnonymous(resultSet.getBoolean("is_anonymous"));
			room.setId(resultSet.getLong("id"));
			room.setName(resultSet.getString("name"));
			room.setPlace(resultSet.getString("place"));
			room.setManager(new UserDmo(resultSet.getLong("manager")));
			room.setBeginTime(resultSet.getTimestamp("begin_time"));
			room.setEndTime(resultSet.getTimestamp("end_time"));
			room.setState(resultSet.getInt("state"));
			room.setCreateTime(resultSet.getTimestamp("create_time"));
			GameDmo game = new GameDmo(resultSet.getInt("game"), resultSet.getString("game_name"));
			room.setGame(game);
			room.setMoney(resultSet.getInt("money"));
			room.setMemberCount(resultSet.getInt("member_count"));
			room.setManCount(resultSet.getInt("man_count"));
			room.setWomanCount(resultSet.getInt("woman_count"));
			room.setDescription(resultSet.getString("description"));
			room.setJoinMember(resultSet.getInt("join_member"));
			room.setJoinManMember(resultSet.getInt("join_man_member"));
			room.setJoinWomanMember(resultSet.getInt("join_woman_member"));
			room.setLatitude(resultSet.getDouble("latitude"));
			room.setLongitude(resultSet.getDouble("longitude"));
			room.setLocked(resultSet.getBoolean("locked"));
			long belongCircleId = resultSet.getLong("belong_circle");
			room.setBelongCircle(circleDao.findOne(belongCircleId));
			room.setOpen(resultSet.getBoolean("open"));
			room.setCity(resultSet.getString("city"));
			return room;
		});
		sql = new StringBuilder(
				" select u.gender,rm.member,rm.nickname,rm.ready,u.avatar_signature from room_member rm ")
						.append(" left join user u on u.id=rm.member where room=? ");
		if (null != list && !list.isEmpty()) {
			for (RoomDmo roomDmo : list) {
				List<RoomMemberVo> members = jdbc.query(sql.toString(), new Object[] { roomDmo.getId() }, (rs, num) -> {
					RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getBoolean("ready"), rs.getString("avatar_signature"));
					vo.setGender(rs.getBoolean("gender"));
					return vo;
				});
				roomDmo.setJoinMembers(members);
			}
		}
		return list;
	}

	@Autowired
	CircleDao circleDao;

	@Autowired
	GameScoreDao gameScoreDao;

	@Override
	public boolean isJoined(long userId, String token, long roomId) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		long count = roomMemberDao.countByMember_IdAndRoom_Id(user.getId(), roomId);
		return count > 0;
	}

	@Override
	public boolean isJoined(UserDmo user, Long roomId) {
		long count = roomMemberDao.countByMember_IdAndRoom_Id(user.getId(), roomId);
		return count > 0;
	}

	@Autowired
	JoinRoomLogDao joinRoomLogDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void join(UserDmo user, Long roomId, String password) throws Exception {
		long userId = user.getId();
		RoomMemberDmo roomMemberDmo = this.roomMemberDao.findByMember_IdAndRoom_Id(userId, roomId);
		if (roomMemberDmo != null) {
			roomMemberDmo.setOnline(true);
			RoomDmo room = roomMemberDmo.getRoom();
			if (roomMemberDmo.getRoom().getGame().getId().intValue() == 30) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
				room.setEndTime(cal.getTime());
				this.roomDao.save(room);
			}
			this.roomMemberDao.save(roomMemberDmo);
			new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, "" + userId));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();

			return;
		}
		if (!user.isSuperUser()) {
			this.validateJoinAllCounts(user.getId());
		}
		RoomDmo room = this.roomDao.findOne(roomId);
		if (null == room) {
			log.error("该房间ID不存在");
			throw new Exception("该房间ID不存在");
		}
		if (room.getState() != ActivityStates.新建.ordinal()) {
			log.error(roomId + ":房间已开始，不能加入");
			throw new Exception("房间已开始，不能加入");
		}
		if (!user.isSuperUser()&&room.getGame().getId().intValue()!=30) {
			this.validateJoinTodayCounts(user.getId(), room.getBeginTime());
		}
		if (room.getName().contains("新用户")) {
			int joinCount = jdbc.queryForObject("select count(1) from room_member where member=?", Integer.class,
					userId);
			if (joinCount > 0) {
				throw new Exception("不能加入新用户房间");
			}
		}
		if (room.getManager().isSuperUser() && room.getGame().getId().intValue() == 22) {
			PropDmo prop = this.propBo.findByUser_Id(userId);
			if (prop.getRemainMovieTicket() < 1) {
				throw new Exception("您没有观影券");
			}
		}
		if (room.getPrepareTime() != null || (room.getState() != ActivityStates.新建.ordinal())) {
			log.error("房间无法加入");
			throw new Exception("房间无法加入");
		}
		if (!Keys.ROOM_KEY.equals(password)) {
			if (!StringUtils.isEmpty(room.getPassword())
					&& !room.getPassword().equals(MD5Util.getSecurityCode(password))) {
				log.error("房间密码不正确");
				throw new Exception("房间密码不正确");
			}
		}
		if (!user.isSuperUser()) {
			boolean managerIsVip = jdbcDao.isVip(room.getManager().getId());
			if (managerIsVip) {
				FriendDmo friendDmo = this.friendDao.findByOwner_IdAndFriend_Id(room.getManager().getId(), userId);
				if (friendDmo != null) {
					if (friendDmo.getPoint() <= 2 && friendDmo.getPoint() != 0) {
						log.error("红色玩家无法加入");
						throw new Exception("红色玩家无法加入");
					}
				}
			}
		}
		if (user.getGender() == null) {
			log.error("请设置性别");
			throw new Exception("请设置性别");
		}
		if (!user.getIsInit()) {
			log.error("请先初始化信息");
			throw new Exception("请先初始化信息");
		}
		room.setJoinMember(room.getJoinMember() + 1);
		if (user.getGender()) {
			room.setJoinManMember(room.getJoinManMember() + 1);
		} else {
			room.setJoinWomanMember(room.getJoinWomanMember() + 1);
		}
		boolean userIsVip = this.jdbcDao.isVip(userId);
		if (!userIsVip && room.getMemberCount().intValue() != 0) {
			if (room.getMemberCount().intValue() < room.getJoinMember().intValue()) {
				log.error("房间人数已满");
				throw new Exception("房间人数已满");
			}
			if (user.getGender()) {
				if (room.getManCount() != 0 && room.getJoinManMember() > room.getManCount()) {
					log.error("房间人数已满");
					throw new Exception("房间人数已满");
				}
			} else {
				if (room.getWomanCount() != 0 && room.getJoinWomanMember() > room.getWomanCount()) {
					log.error("房间人数已满");
					throw new Exception("房间人数已满");
				}
			}
		}
		RoomMemberDmo memberDmo = new RoomMemberDmo();
		memberDmo.setJoinTime(new Date());
		memberDmo.setRoom(room);
		memberDmo.setGame(room.getGame());
		memberDmo.setMember(user);
		memberDmo.setResult(0);
		memberDmo.setNickname(user.getNickname());
		memberDmo.setOnline(true);
		if (room.getGame().getId().intValue() == 30) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
			room.setEndTime(cal.getTime());
		}
		this.roomDao.save(room);
		this.roomMemberDao.save(memberDmo);
		this.joinRoomLogDao.save(new JoinRoomLogDmo(null, user.getId(), roomId, new Date()));
		new Thread(() -> {
			try {
				Thread.sleep(1000);
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new InfoNtfMessage("【" + user.getNickname() + "】进入房间", null));
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, "" + userId));
			} catch (Exception e) {
				log.error(e.getMessage());
			}

		}).start();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void leave(long userId, String token, long roomId) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		RoomMemberDmo roomMemberDmo = roomMemberDao.findByMember_IdAndRoom_Id(user.getId(), roomId);
		if (roomMemberDmo == null) {
			return;
		}
		roomMemberDmo.setOnline(false);
		this.roomMemberDao.save(roomMemberDmo);
		RoomDmo room = roomMemberDmo.getRoom();
		// 房主离开，不操作
		if (room.getManager().getId().longValue() == user.getId().longValue()) {
			if (!roomMemberDmo.isReady()&&room.getGame().getId().intValue()==30) {
				int friendCard = roomMemberDmo.getFriendCards();
				room.setJoinMember(room.getJoinMember() - 1 - friendCard);
				if (user.getGender()) {
					room.setJoinManMember(room.getJoinManMember() - 1 - friendCard);
				} else {
					room.setJoinWomanMember(room.getJoinWomanMember() - 1 - friendCard);
				}
				this.roomMemberDao.delete(roomMemberDmo);
			}
			new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, "" + userId));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
			return;
		}
		// 如果成员准备，离开时不操作
		if (roomMemberDmo.isReady()) {
			roomMemberDmo.setOnline(false);
			new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, "" + userId));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
			return;
			// 如果成员未准备，离开时处理数据，发消息
		}
		int friendCard = roomMemberDmo.getFriendCards();
		roomMemberDao.delete(roomMemberDmo.getId());
		room.setPrepareTime(null);
		room.setJoinMember(room.getJoinMember() - 1 - friendCard);
		if (user.getGender()) {
			room.setJoinManMember(room.getJoinManMember() - 1 - friendCard);
		} else {
			room.setJoinWomanMember(room.getJoinWomanMember() - 1 - friendCard);
		}
		roomDao.save(room);
		if (friendCard > 0) {
			PropDmo prop = this.propBo.findByUser_Id(userId);
			prop.setFriendCard(prop.getFriendCard() + friendCard);
			this.propDao.save(prop);
		}
		new Thread(() -> {
			try {
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, "" + userId));
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new InfoNtfMessage("【" + user.getNickname() + "】退出房间", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void quit(long userId, String token, long roomId) throws Exception {
		UserDmo user = this.userBo.loginByToken(userId, token);
		if (null == user) {
			log.error("用户登录失败");
			throw new Exception("用户登录失败");
		}
		RoomDmo room = roomDao.findOne(roomId);
		if (room == null) {
			log.error("房间不存在");
			throw new Exception("房间不存在");
		}
		if (room.getState() > ActivityStates.准备中.ordinal()) {
			log.error("退出失败");
			throw new Exception("退出失败");
		}
		RoomMemberDmo roomMemberDmo = roomMemberDao.findByMember_IdAndRoom_Id(user.getId(), room.getId());
		if (roomMemberDmo == null) {
			log.error("该用户没有加入此房间");
			throw new Exception("该用户没有加入此房间");
		}
		if (room.getState() == ActivityStates.准备中.ordinal()) {
			log.error("房间已准备，无法退出");
			throw new Exception("房间已准备，无法退出");
		}
		if (roomMemberDmo.isReady() && room.getManager().isSuperUser() && room.getGame().getId().intValue() == 22) {
			PropDmo prop = this.propBo.findByUser_Id(userId);
			prop.setRemainMovieTicket(prop.getRemainMovieTicket() + 1);
			propDao.save(prop);
		}
		room.setPrepareTime(null);
		// 如果是房主退出，切换房主
		if (user.getId().longValue() == room.getManager().getId().longValue()) {
			// 删除记录，退还保证金
			this.moneyTransactionBo.roomManagerQuit(roomMemberDmo);
			List<RoomMemberDmo> roomMemberDmos = roomMemberDao.findByRoom_IdAndMember_IdNotAndReady(room.getId(),
					user.getId(), true);
			// 如果没有可成为房主的成员
			if (roomMemberDmos.size() == 0) {
				int friendCard = roomMemberDmo.getFriendCards();
				if (friendCard > 0) {
					PropDmo prop = this.propBo.findByUser_Id(userId);
					prop.setFriendCard(prop.getFriendCard() + friendCard);
					this.propDao.save(prop);
				}
				if (room.getGame().getId().longValue()==30) {
					this.roomMemberDao.delete(roomMemberDmo);
					new Thread(()->{
						try {
							rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
									new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
						}catch (Exception e) {
							log.error(e.getMessage());
						}
					}).start();
					return;
				}
				new Thread() {
					@Override
					public void run() {
						try {
							rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
									new CmdMsgMessage(Keys.RongCloud.CMD_MSG_ROOM_DISSOLVE, ""));
							Thread.sleep(5000);
							rongCloud.destoryChatRoom(Keys.Room.PREFIX + room.getId());
						} catch (InterruptedException e) {
							log.error(e.getMessage());
						} catch (Exception e) {
							log.error(e.getMessage());
						}
					}
				}.start();
				rongCloud.sendSystemTextMsgToOne(user.getId(),
						new TxtMessage("您创建的【" + room.getName() + "】房间已被解散", ""));
				// 删除房间
				this.delete(room);
				// 切换房主
			} else {
				// 获取原房主的姓名
				String originalName = room.getManager().getNickname();
				int friendCard = roomMemberDmo.getFriendCards();
				room.setJoinMember(room.getJoinMember() - 1 - friendCard);
				if (user.getGender()) {
					room.setJoinManMember(room.getJoinManMember() - 1 - friendCard);
				} else {
					room.setJoinWomanMember(room.getJoinWomanMember() - 1 - friendCard);
				}
				if (friendCard > 0) {
					PropDmo prop = this.propBo.findByUser_Id(userId);
					prop.setFriendCard(prop.getFriendCard() + friendCard);
					this.propDao.save(prop);
				}
				roomMemberDmo = roomMemberDmos.get(0);
				UserDmo newManager = roomMemberDmo.getMember();
				room.setManager(newManager);
				new Thread(() -> {
					try {
						rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
								new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
						rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId, new InfoNtfMessage(
								"原房主【" + originalName + "】已退出房间，【" + newManager.getNickname() + "】已成为新房主", ""));
						rongCloud.sendSystemTextMsgToOne(newManager.getId(),
								new TxtMessage("原房主已退出，您已成为【" + room.getName() + "】的新房主", ""));
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}).start();
				roomDao.save(room);
			}
			// 如果不是房主退出
		} else {
			int friendCard = roomMemberDmo.getFriendCards();
			room.setJoinMember(room.getJoinMember() - 1 - friendCard);
			if (user.getGender()) {
				room.setJoinManMember(room.getJoinManMember() - 1 - friendCard);
			} else {
				room.setJoinWomanMember(room.getJoinWomanMember() - 1 - friendCard);
			}
			if (friendCard > 0) {
				PropDmo prop = this.propBo.findByUser_Id(userId);
				prop.setFriendCard(prop.getFriendCard() + friendCard);
				this.propDao.save(prop);
			}
			roomDao.save(room);
			// 如果成员已经准备
			this.moneyTransactionBo.roomMemberQuit(roomMemberDmo);
			new Thread(() -> {
				try {
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, "" + userId));
					rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new InfoNtfMessage("【" + user.getNickname() + "】退出房间", ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
		}

	}

	@Autowired
	PropDao propDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void ready(long userId, String token, long roomId) throws Exception {
		RoomDmo roomDmo = roomDao.findOne(roomId);
		if (roomDmo == null) {
			log.error("该房间不存在，可能已经被注销");
			throw new Exception("该房间不存在，可能已经被注销");
		}
		UserDmo user = userBo.loginByToken(userId, token);
		if (roomDmo.getManager().isSuperUser() && roomDmo.getGame().getId().intValue() == 22) {
			PropDmo prop = this.propBo.findByUser_Id(userId);
			if (prop.getRemainMovieTicket() < 1) {
				throw new Exception("您没有观影券");
			}
			prop.setRemainMovieTicket(prop.getRemainMovieTicket() - 1);
			this.propDao.save(prop);
		}
		RoomMemberDmo roomMember = roomMemberDao.findByMember_IdAndRoom_Id(userId, roomId);
		if (roomMember == null) {
			log.error("请先加入房间");
			throw new Exception("请先加入房间");
		}
		if (roomMember.isReady()) {
			log.error("已经准备了");
			throw new Exception("已经准备了");
		}
		RoomDmo currentRoom = roomMember.getRoom();
		if (currentRoom.getMoney() > user.getAmount()) {
			log.error("保证金不足");
			throw new Exception("保证金不足");
		}
		if (!user.isSuperUser()&&currentRoom.getGame().getId().intValue()!=30) {
			List<RoomMemberDmo> roomMemberDmos = roomMemberDao.findByRoom_IdNotAndMember_IdAndReady(roomId, userId,
					true);
			for (RoomMemberDmo dmo : roomMemberDmos) {
				RoomDmo room = dmo.getRoom();
				if (room.getBeginTime().before(currentRoom.getEndTime())
						&& room.getEndTime().after(currentRoom.getBeginTime())) {
					log.error("您在此时间已经参加了【" + room.getName() + "】,请不要贪心噢!");
					throw new Exception("您在此时间已经参加了【" + room.getName() + "】,请不要贪心噢!");
				}
			}
		}
		this.moneyTransactionBo.roomReady(roomMember);
		new Thread(() -> {
			try {
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void addFriends(long userId, long roomId) {
		RoomDmo room = this.roomDao.findOne(roomId);
		boolean isGreaterThan20 = room.getJoinMember().intValue() > 20;
		if (isGreaterThan20) {
			jdbc.update("update room_member set is_evaluated=1 where room=?", room.getId());
			return;
		}
		Date now = Calendar.getInstance().getTime();
		UserDmo user = userDao.findOne(userId);
		boolean exists1, exists2;
		List<RoomMemberDmo> friends = roomMemberDao.findByRoom_IdAndMember_IdNot(roomId, userId);
		for (RoomMemberDmo roomMemberDmo : friends) {
			UserDmo friend = roomMemberDmo.getMember();
			exists1 = friendDao.existsByOwner_IdAndFriend_Id(userId, friend.getId());
			exists2 = friendDao.existsByOwner_IdAndFriend_Id(friend.getId(), userId);
			if (exists1 != exists2) {
				if (exists1 && !exists2) {
					FriendDmo friendDmo2 = new FriendDmo();
					friendDmo2.setOwner(friend);
					friendDmo2.setFriend(user);
					friendDmo2.setPoint(0.0);
					friendDmo2.setCreateTime(now);
					friendDmo2.setOrigin(FriendOriginEnum.活动评价.getName());
					friendDao.save(friendDmo2);
				} else if (exists2 && !exists1) {
					FriendDmo friendDmo2 = new FriendDmo();
					friendDmo2.setOwner(user);
					friendDmo2.setFriend(friend);
					friendDmo2.setPoint(0.0);
					friendDmo2.setCreateTime(now);
					friendDmo2.setOrigin(FriendOriginEnum.活动评价.getName());
					friendDao.save(friendDmo2);
				}

			} else {
				if ((!exists1) && (!exists2)) {
					FriendDmo friendDmo1 = new FriendDmo();
					friendDmo1.setOwner(user);
					friendDmo1.setFriend(friend);
					friendDmo1.setPoint(0.0);
					friendDmo1.setCreateTime(now);
					friendDmo1.setOrigin("活动");
					friendDao.save(friendDmo1);
					FriendDmo friendDmo2 = new FriendDmo();
					friendDmo2.setOwner(friend);
					friendDmo2.setFriend(user);
					friendDmo2.setPoint(0.0);
					friendDmo2.setCreateTime(now);
					friendDmo2.setOrigin("活动");
					friendDao.save(friendDmo2);
				}
				if (exists1 && exists2) {
					FriendDmo ownerDmo = friendDao.findByOwner_IdAndFriend_Id(userId, friend.getId());
					friendPointUtil.addFriendPoint(ownerDmo);
					friendDao.save(ownerDmo);
					FriendDmo friendDmo = friendDao.findByOwner_IdAndFriend_Id(friend.getId(), userId);
					friendPointUtil.addFriendPoint(friendDmo);
					friendDao.save(friendDmo);
					final long id1 = ownerDmo.getOwner().getId();
					final long id2 = friendDmo.getOwner().getId();
					new Thread(() -> {
						try {
							rongCloud.sendSystemCmdMsgToOne(id1, Keys.RongCloud.CMD_MSG_REFRESH_FRIENDS);
							rongCloud.sendSystemCmdMsgToOne(id2, Keys.RongCloud.CMD_MSG_REFRESH_FRIENDS);
						} catch (Exception e) {
							log.error(e.getMessage());
						}
					}).start();
				}
			}

		}
	}

	@Autowired
	FriendPointUtil friendPointUtil;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void tiRen(long userId, String token, long roomId, long memberId, String reason) throws Exception {
		if (userId == memberId) {
			log.error("不能踢自己");
			throw new Exception("不能踢自己");
		}
		UserDmo user = userBo.loginByToken(userId, token);
		RoomMemberDmo roomMember = roomMemberDao.findByMember_IdAndRoom_Id(memberId, roomId);
		RoomDmo room = roomMember.getRoom();
		if (memberId == room.getManager().getId()) {
			log.error("不能踢管理员");
			throw new Exception("不能踢管理员");
		}
		if (user.getId().longValue() != room.getManager().getId().longValue()) {
			log.error("您没有权限");
			throw new Exception("您没有权限");
		}
		if (room.getState() != ActivityStates.新建.ordinal()) {
			log.error("房间已经开始，无法踢人");
			throw new Exception("房间已经开始，无法踢人");
		}
		UserDmo member = roomMember.getMember();
		boolean isVip = this.jdbcDao.isVip(memberId);
		if (isVip) {
			if (StringUtils.isEmpty(reason) || reason.length() < 5) {
				log.error("理由必须在5个字以上");
				throw new Exception("理由必须在5个字以上");
			}
		}
		if (null == member || roomMember == null) {
			log.error("成员不存在");
			throw new Exception("成员不存在");
		}
		this.moneyTransactionBo.tiRen(roomMember);
		new Thread(() -> {
			try {
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_OUTMAN, memberId + ":&:" + reason));
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, memberId + ":&:" + reason));
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new InfoNtfMessage("【" + member.getNickname() + "】被房主踢出房间", ""));
				if (!StringUtils.isEmpty(reason)) {
					rongCloud.sendSystemTextMsgToOne(memberId,
							new TxtMessage("您被踢出了" + room.getName() + "房间，理由：" + reason, ""));
				} else {
					rongCloud.sendSystemTextMsgToOne(memberId, new TxtMessage("您被踢出了" + room.getName() + "房间", ""));
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();

	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void updateRoom(long userId, String token, long roomId, String name, String description, String beginTime,
			String endTime, int memberCount, int manCount, int womanCount) throws Exception {
		if (StringUtils.isEmpty(name)) {
			log.error("请设置房间名");
			throw new Exception("请设置房间名");
		}
		RoomDmo room = roomDao.findOne(roomId);
		UserDmo manager = userBo.loginByToken(userId, token);
		if (room.getState() != ActivityStates.新建.ordinal()) {
			log.error("房间已准备，无法修改");
			throw new Exception("房间已准备，无法修改");
		}
		if (room.getManager().getId().longValue() != manager.getId().longValue()) {
			log.error("您没有权限修改");
			throw new Exception("您没有权限修改");
		}
		Date beginDate = Formatter.dateTimeFormatter.parse(beginTime),
				endDate = Formatter.dateTimeFormatter.parse(endTime);
		if (room.getName().equals(name) && room.getDescription().equals(description)
				&& room.getBeginTime().getTime() == beginDate.getTime()
				&& room.getEndTime().getTime() == endDate.getTime() && room.getMemberCount().intValue() == memberCount
				&& room.getManCount().intValue() == manCount && room.getWomanCount().intValue() == womanCount) {
			return;
		}
		boolean isCancelState = true;
		if (beginDate.getTime() == room.getBeginTime().getTime() && endDate.getTime() == room.getEndTime().getTime()
				&& room.getMemberCount().intValue() == memberCount && room.getWomanCount().intValue() == womanCount
				&& room.getManCount().intValue() == manCount) {
			isCancelState = false;
		}
		room.setBeginTime(beginDate);
		room.setEndTime(endDate);
		if (!room.getEndTime().after(room.getBeginTime())) {
			log.error("结束时间必须在开始时间之后");
			throw new Exception("结束时间必须在开始时间之后");
		}
		if (!room.getBeginTime().after(new Date(System.currentTimeMillis() + 1000 * 60 * 10))) {
			log.error("开始时间必须在当前时间后10分钟");
			throw new Exception("开始时间必须在当前时间后10分钟");
		}
		room.setMemberCount(memberCount);
		room.setWomanCount(womanCount);
		room.setManCount(manCount);
		if (room.getMemberCount().intValue() != 0
				&& room.getJoinMember().intValue() > room.getMemberCount().intValue()) {
			log.error("人数限制不能小于当前人数");
			throw new Exception("人数限制不能小于当前人数");
		}
		if (room.getMemberCount().intValue() != 0
				&& (room.getWomanCount().intValue() != 0 || room.getManCount().intValue() != 0)) {
			if (room.getJoinManMember() > room.getManCount()) {
				log.error("人数限制不能小于当前人数");
				throw new Exception("人数限制不能小于当前人数");
			}
			if (room.getJoinWomanMember() > room.getWomanCount()) {
				log.error("人数限制不能小于当前人数");
				throw new Exception("人数限制不能小于当前人数");
			}
			if ((room.getWomanCount() + room.getManCount()) != room.getMemberCount()) {
				log.error("人数不正确");
				throw new Exception("人数不正确");
			}
		}
		room.setName(name);
		room.setDescription(description);
		if (!room.getManager().isSuperUser()) {
			if (isCancelState) {
				room.setPrepareTime(null);
				room.setState(ActivityStates.新建.ordinal());
				this.moneyTransactionBo.updateRoom(userId, room);
			}
			roomDao.save(room);
		}
		List<Long> memberIds = this.jdbcDao.findMember_IdsByRoom_IdAndMember_IdNot(room.getId(),
				room.getManager().getId());
		new Thread(() -> {
			try {
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
				if (memberIds != null && (!memberIds.isEmpty())) {
					rongCloud.sendSystemMessage(memberIds,
							new TxtMessage("您加入的房间【" + room.getName() + "】已修改房间信息，请查看后确认是否还要参加", ""));
				}
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new InfoNtfMessage("房间信息已修改，请查看后确认是否还要参加", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			for (Long memberId : memberIds) {
				JpushBean.push("您加入的房间【" + room.getName() + "】已修改房间信息，请查看后确认是否还要参加", memberId + "");
			}
		}).start();
	}

	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	ComplaintDao complaintDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void delete(RoomDmo room) {
		this.jdbc.update("update location set room_id=null where room_id=" + room.getId().longValue());
		this.jdbc.update("update complaint set room_id=null where room_id=" + room.getId().longValue());
		this.jdbc.update("delete from not_late_reason where room_id=" + room.getId().longValue());
		this.jdbc.update("delete from join_room_log where room_id=" + room.getId().longValue());
		this.moneyTransactionBo.deleteRoom(room);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void setOpen(long userId, String token, long roomId, boolean isOpen) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		RoomDmo roomDmo = roomDao.findOne(roomId);
		if (null == roomDmo) {
			log.error("活动不存在");
			throw new Exception("活动不存在");
		}
		if (roomDmo.getManager().getId() != user.getId()) {
			log.error("您没有权限修改");
			throw new Exception("您没有权限修改");
		}
		roomDmo.setOpen(isOpen);
		roomDao.save(roomDmo);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void sendLocation(long userId, String token, long roomId, double latitude, double longitude, String place,
			String ip) throws Exception {
		userBo.loginByToken(userId, token);
		LocationDmo location = new LocationDmo(null, new UserDmo(userId), longitude, latitude, new Date(), ip);
		location.setRoom(this.roomDao.findOne(roomId));
		location.setPlace(place);
		location.setIp(ip);
		locationDao.save(location);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void sign(long userId, String token, long roomId, double latitude, double longitude, String ip)
			throws Exception {
		userBo.loginByToken(userId, token);
		RoomMemberDmo roomMemberDmo = this.roomMemberDao.findByMember_IdAndRoom_Id(userId, roomId);
		if (roomMemberDmo == null) {
			log.error("您不在此房间");
			throw new Exception("您不在此房间");
		}
		if (roomMemberDmo.isSigned()) {
			log.error("您已经签到过了");
			throw new Exception("您已经签到过了");
		}
		RoomDmo room = roomMemberDmo.getRoom();
		if (room.getState() >= 2) {
			log.error("活动已开始，无法签到");
			throw new Exception("活动已开始，无法签到");
		}
		if (System.currentTimeMillis() < (room.getBeginTime().getTime() - 1000 * 60 * 60)) {
			log.error("签到只能在活动开始前一小时内");
			throw new Exception("签到只能在活动开始前一小时内");
		}
		roomMemberDmo.setLatitude(latitude);
		roomMemberDmo.setLongitude(longitude);
		double distance = DistanceConverter.getDistance(longitude, latitude, room.getLongitude(), room.getLatitude());
		if (distance <= Keys.Room.DISTANCE) {
			roomMemberDmo.setSigned(true);
		} else {
			log.error("离活动地点大于300米，签到失败！如因室内GPS信号弱，请连接WiFi后再试（安卓用户请打开系统的高精度定位）");
			throw new Exception("离活动地点大于300米，签到失败！如因室内GPS信号弱，请连接WiFi后再试（安卓用户请打开系统的高精度定位）");
		}
		roomMemberDmo.setAttend(true);
		this.roomMemberDao.save(roomMemberDmo);
		LocationDmo location = new LocationDmo(null, new UserDmo(userId), longitude, latitude, new Date(), ip);
		location.setRoom(room);
		locationDao.save(location);
		new Thread(() -> {
			try {
				this.rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, userId + ":sign"));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void signAgain(long userId, String token, long roomId, double latitude, double longitude, String ip)
			throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		RoomMemberDmo roomMemberDmo = this.roomMemberDao.findByMember_IdAndRoom_Id(userId, roomId);
		if (roomMemberDmo == null) {
			log.error("您不在此房间");
			throw new Exception("您不在此房间");
		}
		if (roomMemberDmo.isSigned()) {
			log.error("您已经签到过了");
			throw new Exception("您已经签到过了");
		}
		PropDmo prop = this.propBo.findByUser_Id(userId);
		if (prop.getSignCount() < 1) {
			log.error("补签卡不足");
			throw new Exception("补签卡不足。");
		}
		RoomDmo room = roomMemberDmo.getRoom();
		Date now = new Date();
		if (now.before(room.getBeginTime()) || now.after(new Date(room.getBeginTime().getTime() + 5 * 60 * 1000))) {
			log.error("补签必须在活动开始的后五分钟内");
			throw new Exception("补签必须在活动开始的后五分钟内");
		}
		roomMemberDmo.setLatitude(latitude);
		roomMemberDmo.setLongitude(longitude);
		double distance = DistanceConverter.getDistance(longitude, latitude, room.getLongitude(), room.getLatitude());
		if (distance <= Keys.Room.DISTANCE) {
			roomMemberDmo.setSigned(true);
			new Thread(() -> {
				try {
					this.rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
							new InfoNtfMessage(user.getNickname() + "补签", ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
		} else {
			log.error("不在范围之内");
			throw new Exception("不在范围之内");
		}
		roomMemberDmo.setAttend(true);
		this.roomMemberDao.save(roomMemberDmo);
		LocationDmo location = new LocationDmo(null, new UserDmo(userId), longitude, latitude, new Date(), ip);
		location.setRoom(room);
		locationDao.save(location);
		prop.setSignCount(prop.getSignCount() - 1);
		propBo.save(prop);
		new Thread(() -> {
			try {
				this.rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, userId + ":sign"));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();

	}

	@Autowired
	PropBo propBo;

	@Autowired
	RoomEvaluationDao roomEvaluationDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void evalute(RoomDmo room) {
		List<Long> friendIds = jdbcDao.findMember_IdsByRoom_Id(room.getId());
		List<RoomMemberDmo> roomMembers = roomMemberDao.findByRoom_Id(room.getId());
		for (RoomMemberDmo roomMemberDmo : roomMembers) {
			for (Long friendId : friendIds) {
				if (roomMemberDmo.getMember().getId().longValue() == friendId.longValue()) {
					continue;
				}
				if (!friendDao.existsByOwner_IdAndFriend_Id(roomMemberDmo.getMember().getId(), friendId)) {
					FriendDmo dmo = new FriendDmo();
					dmo.setFriend(new UserDmo(friendId));
					dmo.setOwner(new UserDmo(roomMemberDmo.getMember().getId()));
					this.friendDao.save(dmo);
				}
				if (!friendDao.existsByOwner_IdAndFriend_Id(friendId, roomMemberDmo.getMember().getId())) {
					FriendDmo dmo = new FriendDmo();
					dmo.setOwner(new UserDmo(friendId));
					dmo.setFriend(new UserDmo(roomMemberDmo.getMember().getId()));
					this.friendDao.save(dmo);
				}
			}
		}
		/*
		 * 好友好感度评价
		 */
		for (RoomMemberDmo roomMemberDmo : roomMembers) {
			if (roomMemberDmo.isEvaluated()) {
				continue;
			}
			roomMemberDmo.setEvaluated(true);
			roomMemberDao.save(roomMemberDmo);
			List<FriendDmo> friendDmos = friendDao.findByOwner_IdAndFriend_IdIn(roomMemberDmo.getMember().getId(),
					friendIds);
			for (FriendDmo friendDmo : friendDmos) {
				FriendDmo friendDmo2 = friendDao.findByOwner_IdAndFriend_Id(friendDmo.getFriend().getId(),
						friendDmo.getOwner().getId());
				if (friendDmo.getEvaluatePoint() == 0 && friendDmo2.getEvaluatedPoint() == 0) {
					friendDmo.setEvaluatePoint(5);
					friendDmo2.setEvaluatedPoint(5);
					if (friendDmo.getEvaluatePoint() > 4 && friendDmo.getEvaluatedPoint() <= 4) {
						friendDmo.setPoint(0.0);
						friendDmo2.setPoint(friendDmo.getEvaluatedPoint() * 1.0);
						friendDao.save(friendDmo);
						friendDao.save(friendDmo2);
					} else if (friendDmo.getEvaluatePoint() <= 4 && friendDmo.getEvaluatedPoint() > 4) {
						friendDmo.setPoint(friendDmo.getEvaluatePoint() * 1.0);
						friendDmo2.setPoint(0.0);
						friendDao.save(friendDmo);
						friendDao.save(friendDmo2);
					} else {
						double point = Math.min(friendDmo.getEvaluatePoint(), friendDmo.getEvaluatedPoint());
						friendDmo.setPoint(point);
						friendDmo2.setPoint(point);
						friendDao.save(friendDmo);
						friendDao.save(friendDmo2);
						Thread t = new Thread(() -> {
							try {
								rongCloud.sendSystemMessage(
										new String[] { friendDmo.getOwner().getId() + "",
												friendDmo2.getOwner().getId() + "" },
										new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_FRIENDS, ""));
								if (point > 5) {
									rongCloud.sendMessageToFriend(friendDmo2.getOwner().getId(),
											friendDmo2.getFriend().getId(), new TxtMessage("我们已经是好友啦，快来聊天吧", ""));
									rongCloud.sendMessageToFriend(friendDmo.getOwner().getId(),
											friendDmo.getFriend().getId(), new TxtMessage("我们已经是好友啦，快来聊天吧", ""));
								}
							} catch (Exception e) {
								log.error(e.getMessage());
							}
						});
						t.start();
					}

				}

			}
		}
		// 如果活动不需要计分，返回。
		if (!room.getGame().isScoring()) {
			return;
		}
		// 如果活动只有2人，不计分，返回。
		if (room.getMemberCount() != 0 && room.getMemberCount().intValue() <= 2) {
			return;
		}
		// 无限房间，加入的人数小于2
		if (room.getMemberCount().intValue() == 0 && room.getJoinMember().intValue() <= 2) {
			return;
		}
		// 超过20个人不计分
		if (room.getJoinMember().intValue() >= 20) {
			return;
		}
		/*
		 * 计算分数
		 */
		// 设置默认分数
		for (RoomMemberDmo rm1 : roomMembers) {
			for (RoomMemberDmo rm2 : roomMembers) {
				if (rm1.getMember().getId().longValue() == rm2.getMember().getId().longValue()) {
					continue;
				}
				RoomEvaluationDmo evaluationDmo = this.roomEvaluationDao.findByRoomIdAndOwnerIdAndOtherId(
						rm1.getRoom().getId(), rm1.getMember().getId(), rm2.getMember().getId());
				if (evaluationDmo == null) {
					evaluationDmo = new RoomEvaluationDmo(rm1.getMember().getId(), rm2.getMember().getId(), 5,
							rm1.getRoom().getId());
					this.roomEvaluationDao.save(evaluationDmo);
				}
			}
		}
		// 一、将原始分数替换为计算评价标准之后的分数
		// 获取一个房间的评价集合，按照评价者排序
		List<RoomEvaluationDmo> evaluationList = this.roomEvaluationDao.findByRoomIdOrderByOwnerId(room.getId());
		log.info(room.getId() + ",房间分数评价表的行数为：" + evaluationList.size());
		// 获取房间人数
		int count = friendIds.size();
		// key存储评价者，value存储评价的评价基准变量
		Map<Long, Double> map = new HashMap<>();
		long tempUserId = 0;
		for (RoomEvaluationDmo roomEvaluationDmo : evaluationList) {
			// 如果是新的评价者
			if (tempUserId != roomEvaluationDmo.getOwnerId()) {
				// 计算上一次评价基准
				if (map.containsKey(tempUserId)) {
					double val = map.get(tempUserId);
					val = val / ((count - 1) * Keys.AVERAGE_POINT);
					map.put(tempUserId, val);
				}
				// 切换指针，存储分数
				tempUserId = roomEvaluationDmo.getOwnerId();
				map.put(tempUserId, roomEvaluationDmo.getPoint());
				// 如果不是新的评价者，添加分数。
			} else {
				double sum = map.get(roomEvaluationDmo.getOwnerId());
				sum += roomEvaluationDmo.getPoint();
				map.put(roomEvaluationDmo.getOwnerId(), sum);
			}
		}
		// 修改分数
		for (RoomEvaluationDmo roomEvaluationDmo : evaluationList) {
			double point = roomEvaluationDmo.getPoint();
			double val = map.get(roomEvaluationDmo.getOwnerId());
			point = point / val;
			roomEvaluationDmo.setPoint(point);
		}
		roomEvaluationDao.save(evaluationList);
		// 二、修改房间成员评分表，游戏成员评分表
		// 获取平均分列表
		List<TempEvaluationVo> tempEvaluations = this.jdbcDao.findTempEvaluationsByRoomId(room.getId());
		// 计算最终分数
		int order = 0;
		for (int i = 0; i < tempEvaluations.size(); ++i) {
			TempEvaluationVo tempEvaluation = tempEvaluations.get(i);
			RoomMemberDmo roomMemberDmo = this.roomMemberDao.findByMember_IdAndRoom_Id(tempEvaluation.getUserId(),
					room.getId());
			// 如果没有签到
			if (!roomMemberDmo.isSigned()) {
				int point = Keys.AVERAGE_POINT * (count - (tempEvaluations.size() - 1) * 2);
				roomMemberDmo.setPoint(point);
				this.roomMemberDao.save(roomMemberDmo);
				GameScoreDmo gameScore = this.gameScoreDao.findByUser_IdAndGame_Id(tempEvaluation.getUserId(),
						room.getGame().getId());
				if (gameScore == null) {
					gameScore = new GameScoreDmo(null, new UserDmo(tempEvaluation.getUserId()), room.getGame(),
							Keys.Room.INIT_POINT, 1);
				}
				gameScore.setScore(gameScore.getScore() + point);
				int score = gameScore.getScore() < 1000 ? 1000 : gameScore.getScore();
				gameScore.setScore(score);
				gameScoreDao.save(gameScore);
				continue;
			}
			int point = Keys.AVERAGE_POINT * (count - order++ * 2);
			roomMemberDmo.setPoint(point);
			roomMemberDao.save(roomMemberDmo);
			GameScoreDmo gameScore = this.gameScoreDao.findByUser_IdAndGame_Id(tempEvaluation.getUserId(),
					room.getGame().getId());
			if (gameScore == null) {
				gameScore = new GameScoreDmo(null, new UserDmo(tempEvaluation.getUserId()), room.getGame(),
						Keys.Room.INIT_POINT, 1);
			}
			gameScore.setScore(gameScore.getScore() + point);
			int score = gameScore.getScore() < 1000 ? 1000 : gameScore.getScore();
			gameScore.setScore(score);
			gameScoreDao.save(gameScore);
		}
		for (RoomMemberDmo roomMemberDmo : roomMembers) {
			JpushBean.push("【" + room.getName() + "】评价完成啦，快看看自己得了多少分吧", roomMemberDmo.getMember().getId() + "");
		}
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void notLate(long userId, String token, long roomId, String reason, String photoUrl, long certifierId)
			throws Exception {
		this.userBo.loginByToken(userId, token);
		if (StringUtils.isEmpty(reason) || reason.length() < 5) {
			log.error("理由至少5个字");
			throw new Exception("理由至少5个字");
		}
		RoomMemberDmo roomMemberDmo = roomMemberDao.findByRoom_IdAndMember_Id(roomId, userId);
		RoomDmo room = roomMemberDmo.getRoom();
		if (!roomMemberDao.existsByRoom_IdAndMember_Id(roomId, certifierId)) {
			log.error("证明人不在场");
			throw new Exception("证明人不在场");
		}
		if (room.getState() < ActivityStates.进行中.ordinal()) {
			log.error("活动还没开始");
			throw new Exception("活动还没开始");
		}
		if (room.getState() > ActivityStates.进行中.ordinal()) {
			log.error("活动已结束");
			throw new Exception("活动已结束");
		}
		if (roomMemberDmo.isSigned()) {
			log.error("已经签到过了");
			throw new Exception("已经签到过了");
		}
		if (roomMemberDmo.isRequestNotLate()) {
			log.error("已经申请过了");
			throw new Exception("已经申请过了");
		}
		roomMemberDmo.setRequestNotLate(true);
		roomMemberDao.save(roomMemberDmo);
		NotLateReasonDmo dmo = this.notLateReasonDao.findByUser_IdAndRoom_Id(userId, roomId);
		if (dmo == null) {
			dmo = new NotLateReasonDmo(null, roomMemberDmo.getMember(), roomMemberDmo.getRoom(), reason, photoUrl,
					certifierId, new Date());
			this.notLateReasonDao.save(dmo);
		}
	}

	@Autowired
	NotLateReasonDao notLateReasonDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void attend(long userId, String token, long roomId) throws Exception {
		this.userBo.loginByToken(userId, token);
		RoomMemberDmo roomMemberDmo = this.roomMemberDao.findByRoom_IdAndMember_Id(roomId, userId);
		roomMemberDmo.setAttend(true);
		this.roomMemberDao.save(roomMemberDmo);
		new Thread(() -> {
			try {
				this.rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, userId + ":attend"));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void setOnline(long userId, long roomId, boolean online) throws Exception {
		RoomMemberDmo rm = this.roomMemberDao.findByMember_IdAndRoom_Id(userId, roomId);
		if (rm == null) {
			log.error("用户没有加入该房间");
			throw new Exception("用户没有加入该房间");
		}
		rm.setOnline(online);
		this.roomMemberDao.save(rm);
		new Thread(() -> {
			try {
				this.rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void cancelReady(long userId, String token, long roomId) throws Exception {
		UserDmo user = this.userBo.loginByToken(userId, token);
		RoomMemberDmo roomMember = this.roomMemberDao.findByMember_IdAndRoom_Id(userId, roomId);
		if (roomMember == null) {
			log.error("用户没有加入该房间");
			throw new Exception("用户没有加入该房间");
		}
		if (!roomMember.isReady()) {
			log.error("用户没有准备");
			throw new Exception("用户没有准备");
		}
		RoomDmo room = roomMember.getRoom();
		if (room.getState() >= ActivityStates.准备中.ordinal()) {
			log.error("取消准备失败");
			throw new Exception("取消准备失败");
		}
		if (room.getState() != ActivityStates.新建.ordinal()) {
			log.error("房间已准备，无法退出");
			throw new Exception("房间已准备，无法退出");
		}
		// 如果用户是房主
		if (room.getManager().getId() == user.getId()&&room.getGame().getId().intValue()!=30) {
			log.error("房主不可以取消");
			throw new Exception("房主不可以取消");
		}
		if (room.getManager().isSuperUser() && room.getGame().getId().intValue() == 22) {
			PropDmo prop = this.propBo.findByUser_Id(userId);
			prop.setRemainMovieTicket(prop.getRemainMovieTicket() + 1);
			propDao.save(prop);
		}
		this.moneyTransactionBo.cancelRoomReadyV2(roomMember);
		new Thread(() -> {
			try {
				rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, roomId,
						new CmdMsgMessage(Keys.RongCloud.CMD_MSG_REFRESH_ROOM, ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void sendLocationV2(long userId, String token, Long roomId, double latitude, double longitude, String place,
			String udid, String ip) throws Exception {
		userBo.loginByToken(userId, token);
		LocationDmo location = new LocationDmo(null, new UserDmo(userId), longitude, latitude, new Date(), ip);
		if (null != roomId) {
			location.setRoom(new RoomDmo(roomId));
		}
		location.setPlace(place);
		location.setIp(ip);
		location.setUdid(udid);
		locationDao.save(location);
	}

	@Autowired
	SpreadUserDao spreadUserDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public RoomDmo createChatRoom(UserDmo user, String name, boolean anonymous, String password) throws Exception {
		if (!user.getIsInit()) {
			log.error("您还没有初始化");
			throw new Exception("您还没有初始化");
		}
		if (user.getGender() == null) {
			log.error("请先设置性别");
			throw new Exception("请先设置性别");
		}
		RoomDmo room = new RoomDmo();
		room.setManager(user);
		room.setName(name);
		room.setAnonymous(anonymous);
		room.setGame(new GameDmo(30));
		room.setCreateTime(new Date());
		room.setJoinMember(1);
		room.setCity("");
		if (!StringUtils.isEmpty(password)) {
			room.setPassword(MD5Util.getSecurityCode(password));
		}
		if (user.getGender()) {
			room.setJoinManMember(1);
		} else {
			room.setJoinWomanMember(1);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		room.setEndTime(cal.getTime());
		room.setCreateTime(new Date());
		room.setBeginTime(new Date());
		this.roomDao.save(room);
		this.moneyTransactionBo.becomeRoomManager(user, room);
		JoinRoomLogDmo logDmo = new JoinRoomLogDmo(null, user.getId(), room.getId(), new Date());
		this.joinRoomLogDao.save(logDmo);
		new Thread(() -> {
			try {
				rongCloud.createChatRoom(room.getId() + "", room.getName());
				rongCloud.createChatRoom("cmd" + room.getId() + "", room.getName());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		return room;
	}


	@Override
	public List<RoomDmo> findByGame_Id(int gameId, int page, int size) {
		Page<RoomDmo> roomDmoPages = this.roomDao.findByGame_IdAndStateNot(gameId, ActivityStates.已废弃.ordinal(),
				new PageRequest(page, size));
		List<RoomDmo> list = roomDmoPages.getContent();
		StringBuilder sql = new StringBuilder(
				" select u.gender,rm.member,rm.nickname,rm.ready,u.avatar_signature from room_member rm ")
						.append(" left join user u on u.id=rm.member where room=? ");
		if (null != list && !list.isEmpty()) {
			for (RoomDmo roomDmo : list) {
				List<RoomMemberVo> members = jdbc.query(sql.toString(), new Object[] { roomDmo.getId() }, (rs, num) -> {
					RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getBoolean("ready"), rs.getString("avatar_signature"));
					vo.setGender(rs.getBoolean("gender"));
					return vo;
				});
				roomDmo.setJoinMembers(members);
			}
		}
		return list;
	}

	@Override
	public List<RoomDmo> findRoomsByGameOrderV2(int gameId, String games, Integer state, Double longitude,
			Double latitude, int page, int size, String city) throws Exception {
		List<RoomDmo> list;
		StringBuilder sql = new StringBuilder(" select r.is_anonymous,r.city, p.vip_expire_date>now() is_vip,").append(
				" r.id,r.name,r.place,r.manager,r.begin_time,r.end_time,r.state,r.create_time,r.belong_circle,r.open")
				.append(" ,r.game,r.money,r.member_count,r.man_count,r.woman_count,r.description")
				.append(" ,r.join_member,r.join_man_member,r.join_woman_member,r.latitude,r.longitude,r.locked,g.name game_name ")
				.append(" , (TIMESTAMPDIFF(HOUR,now(),r.begin_time)+distance_calc(r.longitude,r.latitude," + longitude
						+ "," + latitude + ")+r.member_count/r.join_member  ) as order_score")
				.append("  FROM room r left join game g on r.game=g.id ").append(" left join user u on u.id=r.manager ")
				.append(" left join prop p on p.user_id=u.id ").append(" WHERE r.state<>5 and r.prepare_time is null ");
		if (!StringUtils.isEmpty(city)) {
			sql.append(" and (r.city like '" + city + "%' or r.game=30) ");
		}
		if (gameId != 0) {
			sql.append(" and r.game=" + gameId + " ");
		}
		if (!StringUtils.isEmpty(games) && gameId == 0) {
			sql.append(" and (r.game in (" + games + ") or r.game=30)");
		}
		if (state != null) {
			sql.append(" and r.state=" + state + " ");
		}
		sql.append(" and open=1 ");
		sql.append(" ORDER BY r.join_member desc, order_score asc LIMIT " + page * size + "," + size + " ");
		list = jdbc.query(sql.toString(), (ResultSet resultSet, int rowNum) -> {
			RoomDmo room = new RoomDmo();
			room.setAnonymous(resultSet.getBoolean("is_anonymous"));
			room.setId(resultSet.getLong("id"));
			room.setName(resultSet.getString("name"));
			room.setPlace(resultSet.getString("place"));
			room.setManager(new UserDmo(resultSet.getLong("manager")));
			room.setBeginTime(resultSet.getTimestamp("begin_time"));
			room.setEndTime(resultSet.getTimestamp("end_time"));
			room.setState(resultSet.getInt("state"));
			room.setCreateTime(resultSet.getTimestamp("create_time"));
			GameDmo game = new GameDmo(resultSet.getInt("game"), resultSet.getString("game_name"));
			room.setGame(game);
			room.setMoney(resultSet.getInt("money"));
			room.setMemberCount(resultSet.getInt("member_count"));
			room.setManCount(resultSet.getInt("man_count"));
			room.setWomanCount(resultSet.getInt("woman_count"));
			room.setDescription(resultSet.getString("description"));
			room.setJoinMember(resultSet.getInt("join_member"));
			room.setJoinManMember(resultSet.getInt("join_man_member"));
			room.setJoinWomanMember(resultSet.getInt("join_woman_member"));
			room.setLatitude(resultSet.getDouble("latitude"));
			room.setLongitude(resultSet.getDouble("longitude"));
			room.setLocked(resultSet.getBoolean("locked"));
			long belongCircleId = resultSet.getLong("belong_circle");
			room.setBelongCircle(circleDao.findOne(belongCircleId));
			room.setOpen(resultSet.getBoolean("open"));
			room.setCity(resultSet.getString("city"));
			return room;
		});
		sql = new StringBuilder(
				" select u.gender,rm.member,rm.nickname,rm.ready,u.avatar_signature from room_member rm ")
						.append(" left join user u on u.id=rm.member where room=? ");
		if (null != list && !list.isEmpty()) {
			for (RoomDmo roomDmo : list) {
				List<RoomMemberVo> members = jdbc.query(sql.toString(), new Object[] { roomDmo.getId() }, (rs, num) -> {
					RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getBoolean("ready"), rs.getString("avatar_signature"));
					vo.setGender(rs.getBoolean("gender"));
					return vo;
				});
				roomDmo.setJoinMembers(members);
			}
		}
		return list;
	}

	@Override
	public List<RoomDmo> findMyJoinRoomsV2(UserDmo userDmo, int page, int size) {
		StringBuilder builder = new StringBuilder(
				"SELECT r.is_anonymous,r.id,r.state,r.name,r.belong_circle,r.manager,r.place,r.begin_time,r.evaluate_time,");
		builder.append(" r.end_time,r.create_time,r.password,r.game,r.money,r.member_count,r.man_count,r.woman_count,");
		builder.append(" r.description,r.join_member,r.join_man_member,r.join_woman_member,r.longitude,r.latitude,");
		builder.append(
				" r.locked,r.city,r.prepare_time,r.open,r.game_mode,r.remaining_money,ABS(UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(r.begin_time)) timediff, ");
		builder.append(" g.name gname ");
		builder.append(" FROM room r inner join game g on g.id=r.game inner join room_member rm on rm.room=r.id ");
		builder.append(" where r.state<>5 and rm.member=" + userDmo.getId());
		builder.append(" order by r.state ,timediff");
		builder.append(" limit " + page * size + "," + size);
		List<RoomDmo> list = this.jdbc.query(builder.toString(), (ResultSet rs, int num) -> {
			RoomDmo dmo = new RoomDmo();
			dmo.setAnonymous(rs.getBoolean("is_anonymous"));
			dmo.setId(rs.getLong("id"));
			dmo.setState(rs.getInt("state"));
			dmo.setName(rs.getString("name"));
			dmo.setBelongCircle(new CircleDmo(rs.getLong("belong_circle")));
			dmo.setManager(new UserDmo(rs.getLong("manager")));
			dmo.setPlace(rs.getString("place"));
			dmo.setBeginTime(rs.getTimestamp("begin_time"));
			dmo.setEvaluateTime(rs.getTimestamp("evaluate_time"));
			dmo.setEndTime(rs.getTimestamp("end_time"));
			dmo.setCreateTime(rs.getDate("create_time"));
			dmo.setPassword(rs.getString("password"));
			dmo.setGame(new GameDmo(rs.getInt("game"), rs.getString("gname")));
			dmo.setMoney(rs.getInt("money"));
			dmo.setMemberCount(rs.getInt("member_count"));
			dmo.setManCount(rs.getInt("man_count"));
			dmo.setWomanCount(rs.getInt("woman_count"));
			dmo.setDescription(rs.getString("description"));
			dmo.setJoinMember(rs.getInt("join_member"));
			dmo.setJoinManMember(rs.getInt("join_man_member"));
			dmo.setJoinWomanMember(rs.getInt("join_woman_member"));
			dmo.setLongitude(rs.getDouble("longitude"));
			dmo.setLatitude(rs.getDouble("latitude"));
			dmo.setLocked(rs.getBoolean("locked"));
			dmo.setCity(rs.getString("city"));
			dmo.setPrepareTime(rs.getDate("prepare_time"));
			dmo.setOpen(rs.getBoolean("open"));
			dmo.setGameMode(rs.getInt("game_mode"));
			dmo.setRemainingMoney(rs.getInt("remaining_money"));
			return dmo;
		});
		for (RoomDmo room : list) {
			String sql = "select rm.member,rm.nickname,rm.ready,u.avatar_signature,u.gender from room_member rm "
					+ "left join user u on u.id=rm.member where room=" + room.getId();
			List<RoomMemberVo> members = jdbc.query(sql, (rs, num) -> {
				RoomMemberVo vo = new RoomMemberVo(rs.getLong("member"), rs.getString("nickname"),
						rs.getBoolean("ready"), rs.getString("avatar_signature"));
				vo.setGender(rs.getBoolean("gender"));
				return vo;
			});
			room.setJoinMembers(members);
		}
		return list;
	}

}
