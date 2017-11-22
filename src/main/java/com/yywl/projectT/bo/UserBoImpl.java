package com.yywl.projectT.bo;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.MD5Util;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.bean.enums.FriendOriginEnum;
import com.yywl.projectT.dao.AugActivityLuckDrawDao;
import com.yywl.projectT.dao.BadgeDetailsDao;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.LocationDao;
import com.yywl.projectT.dao.PayOrderDao;
import com.yywl.projectT.dao.PropDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.SmsCodeDao;
import com.yywl.projectT.dao.SpreadUserDao;
import com.yywl.projectT.dao.SuggestionDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.FriendDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.SmsCodeDmo;
import com.yywl.projectT.dmo.SuggestionDmo;
import com.yywl.projectT.dmo.UserDmo;

import io.rong.messages.CmdMsgMessage;
import io.rong.messages.TxtMessage;

@Service
@Transactional(rollbackOn = Throwable.class)
public class UserBoImpl implements UserBo {

	@Autowired
	UserDao userDao;

	@Autowired
	SuggestionDao suggestionDao;

	@Autowired
	Keys keys;

	@Autowired
	SmsCodeDao smsCodeDao;

	private static final Log log = LogFactory.getLog(UserBoImpl.class);

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void unbind3Part(UserDmo user, String type) throws Exception {
		boolean isPhoneEmpty = StringUtils.isEmpty(user.getPhone());
		switch (type) {
		case Keys.LoginType.WECHAT:
			if (isPhoneEmpty && StringUtils.isEmpty(user.getQqUid()) && StringUtils.isEmpty(user.getXlwbUid())) {
				throw new Exception("至少绑定一个账号");
			}
			jdbc.update("update user set wx_uid=null where id=" + user.getId());
			break;
		case Keys.LoginType.QQ:
			if (isPhoneEmpty && StringUtils.isEmpty(user.getWxUid()) && StringUtils.isEmpty(user.getXlwbUid())) {
				throw new Exception("至少绑定一个账号");
			}
			jdbc.update("update user set qq_uid=null where id=" + user.getId());

			break;
		case Keys.LoginType.WEIBO:
			if (isPhoneEmpty && StringUtils.isEmpty(user.getWxUid()) && StringUtils.isEmpty(user.getQqUid())) {
				throw new Exception("至少绑定一个账号");
			}
			jdbc.update("update user set xlwb_uid=null where id=" + user.getId());
			break;
		default:
			throw new Exception("类型不正确");
		}
		jdbc.update("delete from three_part_info where user_id=? and type=?", user.getId(), type);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void bindPhone(long userId, String token, String phone, String smsCode) throws Exception {
		UserDmo user = this.loginByToken(userId, token);
		SmsCodeDmo smsCodeDmo = smsCodeDao.findByPhone(phone);
		if (smsCodeDmo.getExpiration().before(new Date())) {
			log.error("验证码已过期");
			throw new Exception("验证码已过期");
		}
		if (!smsCodeDmo.getSmscode().equals(smsCode)) {
			log.error("验证码不正确");
			throw new Exception("验证码不正确");
		}
		user.setPhone(phone);
		userDao.save(user);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void authorized(long userId, String token, String realName, String idCard) throws Exception {
		UserDmo user = loginByToken(userId, token);
		if (!user.getIsInit()) {
			log.error("请先实名认证");
			throw new Exception("请先实名认证");
		}
		user.setRealName(realName);
		user.setIdCard(idCard);
		user.setAuthorized(true);
		userDao.save(user);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public UserDmo loginByToken(Long userId, String token) throws Exception {
		if (null == userId) {
			log.error("用户ID不能为空");
			throw new Exception("用户ID不能为空");
		}
		if (StringUtils.isEmpty(token)) {
			log.error("请先登陆");
			throw new Exception("请先登陆");
		}
		UserDmo user = userDao.findOne(userId);
		if (null == user) {
			throw new Exception("用户ID不存在");
		}
		if (StringUtils.isEmpty(user.getToken())) {
			user.setToken(rongCloud.getToken(user.getId() + "", user.getNickname()));
			this.userDao.save(user);
		}
		if (!user.getToken().equals(token)) {
			log.error("登录失效");
			throw new Exception("登录失效");
		}
		user.setIsVip(jdbcDao.isVip(userId));
		return user;
	}

	@Autowired
	JdbcDao jdbcDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public UserDmo loginByPassword(String username, String password, String type) throws Exception {
		if (StringUtils.isEmpty(username)) {
			log.error("用户名不能为空");
			throw new Exception("用户名不能为空");
		}
		if (StringUtils.isEmpty(password)) {
			log.error("密码不能为空");
			throw new Exception("密码不能为空");
		}
		UserDmo user = this.findByUsernameAndType(username, type);
		if (user == null || !user.getPassword().equals(MD5Util.getSecurityCode(password))) {
			log.error("用户名不存在或密码不正确");
			throw new Exception("用户名不存在或密码不正确");
		}
		if (StringUtils.isEmpty(user.getToken())) {
			user.setToken(rongCloud.getToken(user.getId() + "", user.getNickname()));
			this.userDao.save(user);
		}
		return user;
	}

	@Autowired
	RongCloudBean rongCloud;

	public String newNickname() throws Exception {
		String username = null;
		username = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
		while (userDao.existsByNickname(username)) {
			username = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
		}
		return username;
	}

	/**
	 * 所有渠道注册
	 */
	@Override
	@Transactional(rollbackOn = Throwable.class)
	public UserDmo register(String username, String type, Long recommenderId) throws Exception {
		username = username.trim();
		if (StringUtils.isEmpty(username)) {
			log.error("用户名不能为空");
			throw new Exception("用户名不能为空");
		}
		UserDmo user = new UserDmo();
		if (Keys.LoginType.PHONE.equals(type)) {
			if (userDao.existsByPhone(username)) {
				log.error("该手机号已注册过");
				throw new Exception("该手机号已注册过");
			}
			user.setPhone(username);
			user.setNickname("用户" + (username.length() >= 10 ? username.substring(0, 10) : username));
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname());
			}
		} else if (Keys.LoginType.WECHAT.equals(type)) {
			user.setWxUid(username);
			user.setNickname("用户" + (username.length() >= 15 ? username.substring(5, 15) : username));
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname());
			}
		} else if (Keys.LoginType.QQ.equals(type)) {
			user.setQqUid(username);
			user.setNickname("用户" + (username.length() >= 15 ? username.substring(5, 15) : username));
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname());
			}
		} else if (Keys.LoginType.WEIBO.equals(type)) {
			user.setXlwbUid(username);
			user.setNickname("用户" + (username.length() >= 15 ? username.substring(5, 15) : username));
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname());
			}
		} else {
			log.error("登录渠道type不正确");
			throw new Exception("登录渠道type不正确");
		}
		user.setAmount(0);
		user.setLockAmount(0);
		String password = System.currentTimeMillis() + "";
		password = MD5Util.getSecurityCode(password);
		user.setPassword(password);
		user.setRegisterTime(new Date());
		Calendar calendar = Calendar.getInstance();
		calendar.set(1990, 0, 1);
		user.setBirthday(calendar.getTime());
		user.setIsInit(false);
		user.setRecommenderId(recommenderId == null ? -1L : recommenderId);
		this.userDao.save(user);
		this.requestNewToken(user);
		return user;
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public UserDmo register(String username, String type, String nickname, Boolean gender, String avatarSignature,
			Long recommenderId) throws Exception {
		if (StringUtils.isEmpty(username)) {
			log.error("用户名不能为空");
			throw new Exception("用户名不能为空");
		}
		UserDmo user = new UserDmo();
		nickname = StringUtils.isEmpty(nickname) ? "用户" : nickname;
		if (Keys.LoginType.PHONE.equals(type)) {
			if (userDao.existsByPhone(username)) {
				log.error("该手机号已注册过");
				throw new Exception("该手机号已注册过");
			}
			user.setPhone(username);
			user.setNickname("用户" + (username.length() >= 10 ? username.substring(0, 10) : username));
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else if (Keys.LoginType.WECHAT.equals(type)) {
			if (userDao.existsByWxUid(username)) {
				log.error("该微信已注册过");
				throw new Exception("该微信已注册过");
			}
			user.setWxUid(username);
			user.setNickname(nickname);
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else if (Keys.LoginType.QQ.equals(type)) {
			if (userDao.existsByQqUid(username)) {
				log.error("该QQ已注册过");
				throw new Exception("该QQ已注册过");
			}
			user.setQqUid(username);
			user.setNickname(nickname);
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else if (Keys.LoginType.WEIBO.equals(type)) {
			if (userDao.existsByXlwbUid(username)) {
				log.error("该新浪微博已注册过");
				throw new Exception("该新浪微博已注册过");
			}
			user.setXlwbUid(username);
			user.setNickname(nickname);
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else {
			log.error("登录渠道type不正确");
			throw new Exception("登录渠道type不正确");
		}
		user.setAvatarSignature(avatarSignature);
		user.setAmount(0);
		user.setGender(gender);
		user.setLockAmount(0);
		String password = System.currentTimeMillis() + "";
		password = MD5Util.getSecurityCode(password);
		user.setPassword(password);
		this.userDao.save(user);
		this.requestNewToken(user);
		Calendar calendar = Calendar.getInstance();
		calendar.set(1990, 0, 1);
		user.setBirthday(calendar.getTime());
		user.setIsInit(false);
		user.setRecommenderId(recommenderId == null ? -1L : recommenderId);
		user.setRegisterTime(new Date());
		this.userDao.save(user);
		this.badgeBo.register(user);
		return user;
	}

	public String newNickname(String nickname) {
		String username = nickname + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 5);
		while (userDao.existsByNickname(username)) {
			username = nickname + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 5);
		}
		return username;
	}

	@Autowired
	BadgeBo badgeBo;
	@Autowired
	BadgeDetailsDao badgeDetailsDao;

	/**
	 * 根据类型查找
	 * 
	 * @throws Exception
	 */
	@Override
	@Transactional(rollbackOn = Throwable.class)
	public UserDmo findByUsernameAndType(String username, String type) throws Exception {
		if (StringUtils.isEmpty(type)) {
			log.error("参数类型不能为空");
			throw new Exception("参数类型不能为空");
		}
		UserDmo user = null;
		if (Keys.LoginType.WECHAT.equals(type)) {
			user = this.userDao.findByWxUid(username);
			if (user==null) {
				user=userDao.findByWxUid("deprecated."+username);
				if (user!=null) {
					String uid=user.getWxUid();
					uid=uid.replaceFirst("deprecated.", "");
					user.setWxUid(uid);
					this.userDao.save(user);
				}
			}
		} else if (Keys.LoginType.QQ.equals(type)) {
			user = this.userDao.findByQqUid(username);
			if (user==null) {
				user=userDao.findByQqUid("deprecated."+username);
				if (user!=null) {
					String uid=user.getQqUid();
					uid=uid.replaceFirst("deprecated.", "");
					user.setQqUid(uid);
					this.userDao.save(user);
				}
			}
		} else if (Keys.LoginType.WEIBO.equals(type)) {
			user = this.userDao.findByXlwbUid(username);
			if (user==null) {
				user=userDao.findByXlwbUid("deprecated."+username);
				if (user!=null) {
					String uid=user.getXlwbUid();
					uid=uid.replaceFirst("deprecated.", "");
					user.setXlwbUid(uid);
					this.userDao.save(user);
				}
			}
		} else if (Keys.LoginType.PHONE.equals(type)) {
			user = this.userDao.findByPhone(username);
		} else {
			log.error("参数类型不正确");
			throw new Exception("参数类型不正确");
		}
		if (null != user) {
			if (StringUtils.isEmpty(user.getToken())) {
				user.setToken(rongCloud.getToken(user.getId() + "", user.getNickname()));
				userDao.save(user);
			}
		}
		return user;
	}

	@Autowired
	PropBo propBo;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void updateNickname(long userId, String token, String nickname) throws Exception {
		UserDmo dmo = loginByToken(userId, token);
		if (dmo.getIsInit()) {
			PropDmo prop = this.propBo.findByUser_Id(userId);
			if (prop.getChangeNicknameCount() < 1) {
				log.error("请购买改名卡");
				throw new Exception("请购买改名卡");
			} else {
				prop.setChangeNicknameCount(prop.getChangeNicknameCount() - 1);
				propBo.save(prop);
			}
		}
		boolean existsNickname = this.userDao.existsByNickname(nickname);
		if (existsNickname) {
			log.error("昵称已被使用");
			throw new Exception("昵称已被使用");
		}
		dmo.setNickname(nickname);
		jdbcDao.updateMemberNickname(userId, nickname);
		userDao.save(dmo);
		new Thread(() -> {
			try {
				rongCloud.refresh(userId, nickname);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void removeLabel(long userId, String token, String removedLable) throws Exception {
		UserDmo user = this.loginByToken(userId, token);
		PropDmo prop = this.propBo.findByUser_Id(userId);
		if (prop.getLabelClearCount() < 1) {
			log.error("标签消除卡不足");
			throw new Exception("标签消除卡不足");
		}
		prop.setLabelClearCount(prop.getLabelClearCount() - 1);
		propBo.save(prop);
		Set<String> labels = user.getLabels();
		labels.remove(removedLable);
		user.setLabels(labels);
		userDao.save(user);
	}

	@Autowired
	SpreadUserDao spreadUserDao;

	/**
	 * 初始化信息
	 */
	@Override
	@Transactional(rollbackOn = Throwable.class)
	public UserDmo initInfo(long id, String token, String nickname, String password, boolean gender, Date birthday,
			String recommenderAccount) throws Exception {
		if (StringUtils.isEmpty(nickname)) {
			log.error("昵称不能为空");
			throw new Exception("昵称不能为空");
		}
		if (nickname.length() > 12) {
			log.error("昵称长度不能超过12");
			throw new Exception("昵称长度不能超过12");
		}
		if (StringUtils.isEmpty(password)) {
			log.error("密码不能为空");
			throw new Exception("密码不能为空");
		}
		UserDmo user = this.loginByToken(id, token);
		if (user == null) {
			log.error("请重新登录");
			throw new Exception("请重新登录");
		}
		if (user.getIsInit()) {
			log.error("已经做过初始化");
			throw new Exception("已经做过初始化");
		}
		long countNickname = userDao.countByIdNotAndNickname(id, nickname);
		if (countNickname != 0) {
			log.error("昵称已经被使用了");
			throw new Exception("昵称已经被使用了");
		}
		user.setNickname(nickname);
		new Thread(() -> {
			try {
				rongCloud.refresh(user.getId(), user.getNickname());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		user.setPassword(MD5Util.getSecurityCode(password));
		user.setGender(gender);
		user.setBirthday(birthday);
		user.setIsInit(true);
		this.badgeBo.register(user);
		this.propBo.findByUser_Id(user.getId());
		long recommenderId = user.getRecommenderId();
		if (recommenderId != -1L) {
			if (!spreadUserDao.existsByUserId(recommenderId)) {
				UserDmo recommender = this.userDao.findOne(recommenderId);
				if (recommender != null) {
					recommender.setBadge(recommender.getBadge() + Keys.RECOMMENDER__BADGE);
					userDao.save(recommender);
					long ownerId = user.getId();
					long friendId = recommenderId;
					String origin = FriendOriginEnum.新人邀请.getName();
					this.becameBlueFriends(ownerId, friendId, origin);
				}
			}
		} else {
			if (!StringUtils.isEmpty(recommenderAccount)) {
				if (userDao.existsByAccount(recommenderAccount)) {
					UserDmo recommender = this.userDao.findByAccount(recommenderAccount).get(0);
					user.setRecommenderId(recommender.getId());
					if (!spreadUserDao.existsByUserId(recommender.getId())) {
						recommender.setBadge(recommender.getBadge() + Keys.RECOMMENDER__BADGE);
						userDao.save(recommender);
						long ownerId = user.getId();
						// long friendId = recommenderId;
						// recommenderId == -1 才进入这个判断的
						long friendId = recommender.getId();
						user.setRecommenderId(friendId);
						String origin = FriendOriginEnum.新人邀请.getName();
						this.becameBlueFriends(ownerId, friendId, origin);
					}
				} else {
					throw new Exception("推荐人不存在");
				}

			}
		}
		user.setInitTime(new Date());
		this.userDao.save(user);
		return user;
	}

	@Transactional(rollbackOn = Throwable.class)
	public void becameBlueFriends(long ownerId, long friendId, String origin) {
		Date now = Calendar.getInstance().getTime();
		FriendDmo conn1 = this.friendDao.findByOwner_IdAndFriend_Id(ownerId, friendId);
		FriendDmo conn2 = this.friendDao.findByOwner_IdAndFriend_Id(friendId, ownerId);
		boolean flag = false;
		if (null == conn1) {
			conn1 = new FriendDmo();
			conn1.setOwner(new UserDmo(ownerId));
			conn1.setFriend(new UserDmo(friendId));
			conn1.setPoint(7.0);
			conn1.setEvaluatedPoint(7);
			conn1.setEvaluatePoint(7);
			conn1.setOrigin(origin);
			conn1.setCreateTime(now);
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
			conn1.setEvaluatedPoint(7);
			conn1.setEvaluatePoint(7);
			conn1.setOrigin(origin);
			conn1.setCreateTime(now);
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

	@Autowired
	FriendDao friendDao;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void suggest(Long userId, String token, String content, String photoUrl) throws Exception {
		if (StringUtils.isEmpty(content)) {
			log.error("内容不能为空");
			throw new Exception("内容不能为空");
		}
		UserDmo user = loginByToken(userId, token);
		SuggestionDmo suggestion = new SuggestionDmo();
		suggestion.setContent(content);
		suggestion.setCreateTime(new Date());
		suggestion.setUser(user);
		suggestion.setPhotoUrl(photoUrl);
		suggestionDao.save(suggestion);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public String requestNewToken(UserDmo user) throws Exception {
		if (null == user.getId() || null == user) {
			log.error("用户登录失败，无法刷新token");
			throw new Exception("用户登录失败，无法刷新token");
		}
		String token = rongCloud.getToken(user.getId() + "", user.getNickname());
		user.setToken(token);
		this.userDao.save(user);
		return token;
	}

	@Autowired
	LocationDao locationDao;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	RoomDao roomDao;
	
	@Autowired
	CircleDao circleDao;
	
	@Autowired
	PayOrderDao payOrderDao;
	
	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void merge(UserDmo fromUser, UserDmo toUser) throws Exception {
		if (roomDao.existsByManager_IdAndStateLessThan(fromUser.getId(),ActivityStates.待评价.ordinal())) {
			throw new Exception("被绑定账户是房主，暂不能绑定");
		}
		if (circleDao.existsByManager_Id(fromUser.getId())) {
			throw new Exception("被绑定账户是圈主，不能绑定");
		}
		if (fromUser.getLockAmount()>0) {
			throw new Exception("该账号有冻结保证金，不能合并");
		}
		toUser.setAmount(toUser.getAmount()+fromUser.getAmount());
		if (!StringUtils.isEmpty(toUser.getPhone())&&!StringUtils.isEmpty(fromUser.getPhone())) {
			throw new Exception("该账号已绑定手机，请解绑。");
		}
		if (!StringUtils.isEmpty(toUser.getQqUid())&&!StringUtils.isEmpty(fromUser.getQqUid())) {
			throw new Exception("该账号已绑定QQ，请解绑。");
		}
		if (!StringUtils.isEmpty(toUser.getWxUid())&&!StringUtils.isEmpty(fromUser.getWxUid())) {
			throw new Exception("该账号已绑定微信，请解绑。");
		}
		if (!StringUtils.isEmpty(toUser.getXlwbUid())&&!StringUtils.isEmpty(fromUser.getXlwbUid())) {
			throw new Exception("该账号已绑定新浪微博，请解绑。");
		}
		if (StringUtils.isEmpty(toUser.getQqUid())) {
			toUser.setQqUid(fromUser.getQqUid());
		}
		if (StringUtils.isEmpty(toUser.getWxUid())) {
			toUser.setWxUid(fromUser.getWxUid());
		}
		if (StringUtils.isEmpty(toUser.getXlwbUid())) {
			toUser.setXlwbUid(fromUser.getXlwbUid());
		}
		if (StringUtils.isEmpty(toUser.getPhone())) {
			toUser.setPhone(fromUser.getPhone());
		}
		jdbc.update("update image set user_id=? where user_id=?",toUser.getId(),fromUser.getId());
		jdbc.update("update room set manager=? where manager=?",toUser.getId(),fromUser.getId());
		jdbc.update("delete from aug_activity_popular_girl where user_id=?", fromUser.getId());
		this.jdbc.update("update location set user_id=? where user_id=? ", toUser.getId(), fromUser.getId());
		this.jdbc.update("delete from badge_details where user_id=? ", fromUser.getId());
		this.jdbc.update("update transaction_details set user=? where user=? ", toUser.getId(), fromUser.getId());
		this.jdbc.update("delete from three_part_info where user_id=?", fromUser.getId());
		int amount = fromUser.getAmount();
		int lockAmount = fromUser.getLockAmount();
		fromUser.setAmount(0);
		fromUser.setLockAmount(0);
		toUser.setAmount(toUser.getAmount() + amount);
		toUser.setLockAmount(toUser.getLockAmount() + lockAmount);
		
		jdbc.update("update user_circle set user=? where user=?",toUser.getId(),fromUser.getId());
		PropDmo fromProp=this.propBo.findByUser_Id(fromUser.getId()),toProp=this.propBo.findByUser_Id(toUser.getId());
		toProp.setRemainMovieTicket(toProp.getRemainMovieTicket()+fromProp.getRemainMovieTicket());
		this.propDao.save(toProp);
		jdbc.update("delete from prop where user_id=?", fromUser.getId());
		jdbc.update("update note set sender_id=? where sender_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("update note set receiver_id=? where receiver_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("update sept_activity_help set user_id=? where user_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("update sept_activity_help set helper_id=? where helper_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("update aug_activity_helper set user_id=? where user_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("update aug_activity_helper set helper_id=? where helper_id=?", toUser.getId(),
				fromUser.getId());
		jdbc.update("update room_member set member=? where member=?", toUser.getId(), fromUser.getId());
		jdbc.update("update declaration set declarer_id=? where declarer_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("update declaration_evaluation set sender_id=? where sender_id=?", toUser.getId(),
				fromUser.getId());
		jdbc.update("update declaration_evaluation set receiver_id=? where receiver_id=?", toUser.getId(),
				fromUser.getId());
		jdbc.update("update user set recommender_id=? where recommender_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("update sept_activity_luck_draw set user_id=? where user_id=?", toUser.getId(),
				fromUser.getId());
		jdbc.update("update aug_activity_luck_draw set user_id=? where user_id=?", toUser.getId(), fromUser.getId());
		jdbc.update("delete from aug_activity_helper where user_id=? or helper_id=?", fromUser.getId(),
				fromUser.getId());
		jdbc.update("delete from friend_connection where owner=? or friend=?",fromUser.getId(),fromUser.getId());
		try {
			this.userDao.delete(fromUser);
			this.userDao.save(toUser);
		} catch (Exception e) {
			log.error(fromUser.getId() + ":" + e.getMessage());
			throw new Exception("合并失败");
		}

	}

	
	
	@Override
	@Transactional(rollbackOn=Throwable.class)
	public void forceUnbind(UserDmo mergeFromUser, UserDmo mergeToUser, String type) throws Exception {
		if (Keys.LoginType.QQ.equals(type)) {
			if (!StringUtils.isEmpty(mergeToUser.getQqUid())) {
				throw new Exception("该账号已经绑定过QQ");
			}
			mergeToUser.setQqUid(mergeFromUser.getQqUid());
			if (userDao.existsByQqUid("deprecated."+mergeFromUser.getQqUid())) {
				throw new Exception("只能强制绑定一次");
			}
			if (StringUtils.isEmpty(mergeFromUser.getWxUid())&&StringUtils.isEmpty(mergeFromUser.getXlwbUid())&&StringUtils.isEmpty(mergeFromUser.getPhone())) {
				mergeFromUser.setQqUid("deprecated."+mergeFromUser.getQqUid());
			}else {
				mergeFromUser.setQqUid(null);
			}
		}else if(Keys.LoginType.WECHAT.equals(type)) {
			if (!StringUtils.isEmpty(mergeToUser.getWxUid())) {
				throw new Exception("该账号已经绑定过微信");
			}
			mergeToUser.setWxUid(mergeFromUser.getWxUid());
			if (userDao.existsByWxUid("deprecated."+mergeFromUser.getWxUid())) {
				throw new Exception("只能强制绑定一次");
			}
			if (StringUtils.isEmpty(mergeFromUser.getQqUid())&&StringUtils.isEmpty(mergeFromUser.getXlwbUid())&&StringUtils.isEmpty(mergeFromUser.getPhone())) {
				mergeFromUser.setWxUid("deprecated."+mergeFromUser.getWxUid());
			}else {
				mergeFromUser.setWxUid(null);
			}
		}else if(Keys.LoginType.WEIBO.equals(type)) {
			if (!StringUtils.isEmpty(mergeToUser.getXlwbUid())) {
				throw new Exception("该账号已经绑定过新浪微博");
			}
			mergeToUser.setXlwbUid(mergeFromUser.getXlwbUid());
			if (userDao.existsByXlwbUid("deprecated."+mergeFromUser.getXlwbUid())) {
				throw new Exception("只能强制绑定一次");
			}
			if (StringUtils.isEmpty(mergeFromUser.getQqUid())&&StringUtils.isEmpty(mergeFromUser.getWxUid())&&StringUtils.isEmpty(mergeFromUser.getPhone())) {
				mergeFromUser.setXlwbUid("deprecated."+mergeFromUser.getXlwbUid());
			}else {
				mergeFromUser.setXlwbUid(null);
			}
		}else {
			throw new Exception("渠道不正确");
		}
		this.userDao.save(mergeFromUser);
		this.userDao.save(mergeToUser);
	}

	@Autowired
	PropDao propDao;
	@Autowired
	AugActivityLuckDrawDao augActivityLuckDrawDao;

	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Override
	@Transactional(rollbackOn=Throwable.class)
	public UserDmo registerAndInit(String username, String type, String nickname, Boolean gender, String avatarSignature) throws Exception {
		if (StringUtils.isEmpty(username)) {
			log.error("用户名不能为空");
			throw new Exception("用户名不能为空");
		}
		UserDmo user = new UserDmo();
		nickname = StringUtils.isEmpty(nickname) ? "用户" : nickname;
		if (Keys.LoginType.PHONE.equals(type)) {
			if (userDao.existsByPhone(username)) {
				log.error("该手机号已注册过");
				throw new Exception("该手机号已注册过");
			}
			user.setPhone(username);
			user.setNickname("用户" + (username.length() >= 10 ? username.substring(0, 10) : username));
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else if (Keys.LoginType.WECHAT.equals(type)) {
			if (userDao.existsByWxUid(username)) {
				log.error("该微信已注册过");
				throw new Exception("该微信已注册过");
			}
			user.setWxUid(username);
			user.setNickname(nickname);
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else if (Keys.LoginType.QQ.equals(type)) {
			if (userDao.existsByQqUid(username)) {
				log.error("该QQ已注册过");
				throw new Exception("该QQ已注册过");
			}
			user.setQqUid(username);
			user.setNickname(nickname);
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else if (Keys.LoginType.WEIBO.equals(type)) {
			if (userDao.existsByXlwbUid(username)) {
				log.error("该新浪微博已注册过");
				throw new Exception("该新浪微博已注册过");
			}
			user.setXlwbUid(username);
			user.setNickname(nickname);
			boolean exists = this.userDao.existsByNickname(user.getNickname());
			if (exists) {
				user.setNickname(newNickname(nickname));
			}
		} else {
			log.error("登录渠道type不正确");
			throw new Exception("登录渠道type不正确");
		}
		user.setAvatarSignature(avatarSignature);
		user.setAmount(0);
		user.setGender(gender);
		user.setLockAmount(0);
		String password = System.currentTimeMillis() + "";
		password = MD5Util.getSecurityCode(password);
		user.setPassword(password);
		this.userDao.save(user);
		this.requestNewToken(user);
		Calendar calendar = Calendar.getInstance();
		calendar.set(1990, 0, 1);
		user.setBirthday(calendar.getTime());
		user.setIsInit(true);
		user.setRegisterTime(new Date());
		this.userDao.save(user);
		this.badgeBo.register(user);
		return user;
	}


}
