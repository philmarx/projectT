package com.yywl.projectT.web.controller;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.MD5Util;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.ThreePartType;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bean.component.RestTemplateBean;
import com.yywl.projectT.bean.enums.WithdrawalsEnum;
import com.yywl.projectT.bo.SmsCodeBo;
import com.yywl.projectT.bo.TransactionDetailsBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.SmsCodeDao;
import com.yywl.projectT.dao.SuggestionDao;
import com.yywl.projectT.dao.ThreePartInfoDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dao.WithdrawalsDao;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.SmsCodeDmo;
import com.yywl.projectT.dmo.SuggestionDmo;
import com.yywl.projectT.dmo.ThreePartInfoDmo;
import com.yywl.projectT.dmo.TransactionDetailsDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.dmo.WithdrawalsDmo;
import com.yywl.projectT.vo.TransactionDetailsVo;

@RestController
@RequestMapping("user")
public class UserController {

	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;

	@Autowired
	Keys keys;

	@Autowired
	SmsCodeBo smsCodeBo;

	@Autowired
	SmsCodeDao smsCodeDao;

	@Autowired
	RestTemplateBean restTemplate;

	@Autowired
	SuggestionDao suggestionDao;

	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Autowired
	TransactionDetailsBo transactionDetailsBo;

	@Autowired
	RoomMemberDao roomMemberDao;

	@PostMapping("findMyThreePartInfo")
	public Callable<ResultModel> findMyThreePartInfo(long userId, String token, String type) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			if (Keys.LoginType.QQ.equals(type) || Keys.LoginType.WECHAT.equals(type)
					|| Keys.LoginType.WEIBO.equals(type)) {
				List<ThreePartInfoDmo> list = this.threePartInfoDao.findByUserIdAndType(userId, type);
				if (list.isEmpty()) {
					return new ResultModel(false, "信息不存在", null);
				}
				ThreePartInfoDmo dmo = list.get(0);
				Map<String, Object> data = new HashMap<>();
				data.put("nickname", dmo.getNickname());
				data.put("photoUrl", dmo.getPhotoUrl());
				return new ResultModel(true, "", data);
			}
			log.error(userId + ":类型不正确");
			throw new Exception("类型不正确");
		};
	}

	private static final Log log = LogFactory.getLog(UserController.class);

	@PostMapping("existsByPhone")
	public Callable<ResultModel> existsByPhone(String phone) {
		return () -> {
			boolean exists = this.userDao.existsByPhone(phone);
			Map<String, Object> map = new HashMap<>();
			map.put("exists", exists);
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("findIdByAccount")
	public Callable<ResultModel> findIdByAccount(String account) {
		return () -> {
			if (StringUtils.isEmpty(account)) {
				log.error("account不能为空");
				return new ResultModel(true, "account不能为空", null);
			}
			List<UserDmo> users = this.userDao.findByAccount(account);
			if (users.isEmpty()) {
				log.error(account + "不存在");
				return new ResultModel(true, account + "不存在", null);
			}
			if (users.size() > 1) {
				log.error(account + "重复");
				return new ResultModel(true, account + "重复", null);
			}
			UserDmo user = users.get(0);
			Map<String, Object> map = new HashMap<>();
			map.put("id", user.getId());
			map.put("nickname", user.getNickname());
			map.put("avatarSignature", user.getAvatarSignature());
			return new ResultModel(true, "", map);
		};
	}

	@Autowired
	WithdrawalsDao withdrawalsDao;

	@PostMapping("findLockMoneyDetails")
	public Callable<ResultModel> findLockMoneyDetails(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<Map<String, Object>> data = new LinkedList<>();
			List<WithdrawalsDmo> withdrawalsDmos = this.withdrawalsDao.findByUser_IdAndStateInOrderByCreateTimeDesc(
					userId, new Integer[] { WithdrawalsEnum.处理中.ordinal(), WithdrawalsEnum.金额异常.ordinal() });
			for (WithdrawalsDmo dmo : withdrawalsDmos) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", dmo.getId());
				map.put("time", Formatter.dateTimeFormatter.format(dmo.getCreateTime()));
				map.put("money", dmo.getMoney());
				map.put("title", "提现");
				map.put("state", -1);
				map.put("gameId", -1);
				data.add(map);
			}
			List<RoomMemberDmo> roomMembers = this.roomMemberDao
					.findByMember_IdAndIsLockMoneyOrderByRoom_EndTimeDesc(userId, true);
			for (RoomMemberDmo roomMemberDmo : roomMembers) {
				Map<String, Object> map = new HashMap<>();
				RoomDmo room = roomMemberDmo.getRoom();
				map.put("id", roomMemberDmo.getId());
				map.put("time", Formatter.dateTimeFormatter.format(room.getBeginTime()));
				map.put("money", room.getMoney());
				map.put("title", room.getName());
				map.put("state", room.getState());
				map.put("gameId", room.getGame().getId());
				data.add(map);
			}
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("findRecomendCounts")
	public Callable<ResultModel> findRecomendCounts(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			long count = userDao.countByRecommenderIdAndIsInit(userId, true);
			return new ResultModel(true, "", count);
		};
	}

	@RequestMapping(value = "authLogin", method = RequestMethod.POST)
	public Callable<ResultModel> authLogin(String type, String uid, Long recommenderId) {
		return () -> {
			UserDmo user = userBo.findByUsernameAndType(uid, type);
			Map<String, Object> map = new HashMap<>();
			if (user == null) {
				user = userBo.register(uid, type, recommenderId);
				map.put("id", user.getId());
				map.put("token", user.getToken());
				map.put("isInit", user.getIsInit());
				boolean flag = true;
				if (Keys.LoginType.PHONE.equals(type)) {
					flag = false;
				}
				map.put("register", flag);
				return new ResultModel(true, "注册成功", map);
			} else {
				map.put("id", user.getId());
				map.put("token", userBo.requestNewToken(user));
				map.put("isInit", user.getIsInit());
				boolean flag = false;
				if (Keys.LoginType.QQ.equals(type)) {
					flag = StringUtils.isEmpty(user.getWxUid()) && StringUtils.isEmpty(user.getXlwbUid());
				} else if (Keys.LoginType.WECHAT.equals(type)) {
					flag = StringUtils.isEmpty(user.getQqUid()) && StringUtils.isEmpty(user.getXlwbUid());
				} else if (Keys.LoginType.WEIBO.equals(type)) {
					flag = StringUtils.isEmpty(user.getQqUid()) && StringUtils.isEmpty(user.getWxUid());
				}
				boolean existsFriend = this.friendDao.existsByOwner_IdOrFriend_Id(user.getId(), user.getId());
				if (existsFriend || user.getIsInit()) {
					flag = false;
				}
				map.put("register", flag);
				return new ResultModel(true, "登录成功", map);
			}
		};
	}

	@Autowired
	FriendDao friendDao;

	@PostMapping("merge")
	public Callable<ResultModel> merge(long userId, String token, String phone, String password) {
		return () -> {
			UserDmo authUser = this.userBo.loginByToken(userId, token);
			if (authUser.getIsInit()) {
				log.error("新账号已经初始化过，不能合并");
				return new ResultModel(false, "新账号已经初始化过，不能合并", null);
			}
			UserDmo phoneUser = this.userBo.loginByPassword(phone, password, Keys.LoginType.PHONE);
			this.userBo.merge(authUser, phoneUser);
			Map<String, Object> data = new HashMap<>();
			data.put("userId", phoneUser.getId());
			data.put("token", phoneUser.getToken());
			data.put("isInit", phoneUser.getIsInit());
			data.put("register", false);
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("mergeV2")
	public Callable<ResultModel> mergeV2(long userId, String token, String phone, String password) {
		return () -> {
			UserDmo authUser = this.userBo.loginByToken(userId, token);
			if (authUser.getIsInit()) {
				log.error("新账号已经初始化过，不能合并");
				return new ResultModel(false, "新账号已经初始化过，不能合并", null);
			}
			UserDmo phoneUser = this.userBo.loginByPassword(phone, password, Keys.LoginType.PHONE);
			this.userBo.merge(authUser, phoneUser);
			Map<String, Object> data = new HashMap<>();
			data.put("id", phoneUser.getId());
			data.put("token", phoneUser.getToken());
			data.put("isInit", phoneUser.getIsInit());
			data.put("register", false);
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("authorized")
	public Callable<ResultModel> authorized(long userId, String token, String realName, String idCard) {
		return () -> {
			this.userBo.authorized(userId, token, realName, idCard);
			return new ResultModel();
		};
	}

	@Autowired
	RoomDao roomDao;

	@Autowired
	CircleDao circleDao;

	@PostMapping("newMarge")
	public Callable<ResultModel> newMarge(long userId, String token, long mergeToId, long mergeFromId,
			String password) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			UserDmo mergeToUser = userDao.findOne(mergeToId), mergeFromUser = userDao.findOne(mergeFromId);
			if (!mergeFromUser.getPassword().equals(MD5Util.getSecurityCode(password))) {
				throw new Exception("密码错误，如果确认该帐号是您本人所有，请用该帐号登录后重置密码。");
			}
			this.userBo.merge(mergeFromUser, mergeToUser);
			return new ResultModel();
		};
	}

	@PostMapping("forceUnbind")
	public Callable<ResultModel> forceUnbind(long userId, String token, long mergeToId, long mergeFromId,
			String password, String type) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			UserDmo mergeToUser = userDao.findOne(mergeToId), mergeFromUser = userDao.findOne(mergeFromId);
			if (!mergeFromUser.getPassword().equals(MD5Util.getSecurityCode(password))) {
				throw new Exception("密码错误，如果确认该帐号是您本人所有，请用该帐号登录后重置密码。");
			}
			this.userBo.forceUnbind(mergeFromUser, mergeToUser, type);
			return new ResultModel();
		};
	}

	@PostMapping("bind3Part")
	public Callable<ResultModel> bind3Part(long userId, String token, String uid, String type) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			Map<String, Object> data = new HashMap<>();
			if (Keys.LoginType.QQ.equals(type)) {
				UserDmo user2 = this.userDao.findByQqUid(uid);
				if (user2 == null) {
					user.setQqUid(uid);
					this.userDao.save(user);
				} else {
					if (user2.getIsInit()) {
						data.put("userId2", user2.getId());
						data.put("nickname2", user2.getNickname());
						data.put("avatarSignature2", user2.getAvatarSignature());
						String msg = "可强制解除，请登录新版操作";
						return new ResultModel(false, msg, data);
					}
					this.userBo.merge(user2, user);
				}
			} else if (Keys.LoginType.WECHAT.equals(type)) {
				UserDmo user2 = this.userDao.findByWxUid(uid);
				if (user2 == null) {
					user.setWxUid(uid);
					this.userDao.save(user);
				} else {
					if (user2.getIsInit()) {
						data.put("userId2", user2.getId());
						data.put("nickname2", user2.getNickname());
						data.put("avatarSignature2", user2.getAvatarSignature());
						String msg = "可强制解除，请登录新版操作";
						return new ResultModel(false, msg, data);
					}
					this.userBo.merge(user2, user);
				}
			} else if (Keys.LoginType.WEIBO.equals(type)) {
				UserDmo user2 = this.userDao.findByXlwbUid(uid);
				if (user2 == null) {
					user.setXlwbUid(uid);
					this.userDao.save(user);
				} else {
					if (user2.getIsInit()) {
						data.put("userId2", user2.getId());
						data.put("nickname2", user2.getNickname());
						data.put("avatarSignature2", user2.getAvatarSignature());
						String msg = "可强制解除，请登录新版操作";
						return new ResultModel(false, msg, data);
					}
					this.userBo.merge(user2, user);
				}
			} else {
				throw new Exception("参数不正确");
			}
			return new ResultModel();
		};
	}

	@PostMapping("unBind3Part")
	public Callable<ResultModel> unBind3Part(long userId, String token, String password, String type) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (!user.getPassword().equals(MD5Util.getSecurityCode(password))) {
				log.error(userId + ":解绑时密码不正确");
				return new ResultModel(false, "密码不正确", null);
			}
			this.userBo.unbind3Part(user, type);
			return new ResultModel();
		};
	}

	@Autowired
	ThreePartInfoDao threePartInfoDao;

	@PostMapping("saveThreePartInfo")
	public Callable<ResultModel> saveThreePartInfo(long userId, String token, String type, String nickname,
			String photoUrl) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			boolean isTypeTrue = ThreePartType.checkType(type);
			if (!isTypeTrue) {
				log.error(userId + ":类型不正确");
				return new ResultModel(false, "类型不正确", null);
			}
			List<ThreePartInfoDmo> list = this.threePartInfoDao.findByUserIdAndType(userId, type);
			if (list.isEmpty()) {
				ThreePartInfoDmo dmo = new ThreePartInfoDmo(null, photoUrl, type, nickname, userId);
				this.threePartInfoDao.save(dmo);
			} else {
				ThreePartInfoDmo dmo = list.get(0);
				dmo.setNickname(nickname);
				dmo.setPhotoUrl(photoUrl);
				dmo.setType(type);
				this.threePartInfoDao.save(dmo);
			}
			return new ResultModel();
		};
	}

	@PostMapping("bindPhone")
	public Callable<ResultModel> bindPhone(long userId, String token, String phone, String smsCode) {
		return () -> {
			if (StringUtils.isEmpty(phone)) {
				log.error("手机号不能为空");
				return new ResultModel(false, "手机号不能为空", null);
			}
			if (StringUtils.isEmpty(smsCode)) {
				log.error("验证码不能为空");
				return new ResultModel(false, "验证码不能为空", null);
			}
			boolean exists = this.userDao.existsByPhone(phone);
			if (exists) {
				log.error("该手机已被绑定");
				return new ResultModel(false, "该手机已被绑定", null);
			}
			userBo.bindPhone(userId, token, phone, smsCode);
			return new ResultModel();
		};
	}

	@RequestMapping(value = "findByToken", method = RequestMethod.POST)
	public Callable<ResultModel> findByToken(Long userId, String token) throws Exception {
		return () -> {
			UserDmo user = userBo.loginByToken(userId, token);
			// Map<String, Object> map = new TreeMap<>();
			// map.put("id", user.getId());
			// map.put("nickname", user.getNickname());
			// map.put("amount", user.getAmount());
			// map.put("realName", user.getRealName() == null ? "" : user.getRealName());
			// map.put("isVip", user.getIsInit());
			// map.put("badge", user.getBadge());
			// map.put("lockAmount", user.getLockAmount());
			// map.put("phone", user.getPhone());
			// map.put("authorized", user.isAuthorized());
			// map.put("gender", user.getGender());
			// map.put("isInit", user.getIsInit());
			// map.put("labels", user.getLabels());
			// map.put("avatarSignature", user.getAvatarSignature());
			// map.put("birthday", user.getBirthday() == null ? "" :
			// Formatter.dateFormatter.format(user.getBirthday()));
			return new ResultModel(true, "登录成功", user);
		};
	}

	@PostMapping("setAccount")
	public Callable<ResultModel> setAccount(long userId, String token, String account) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (!StringUtils.isEmpty(user.getAccount())) {
				log.error(userId + ":账号不能修改");
				return new ResultModel(false, "账号不能修改", null);
			}
			boolean exists = this.userDao.existsByAccount(account);
			if (exists) {
				log.error(userId + ":账号不能重复");
				return new ResultModel(false, "账号不能重复", null);
			}
			if (StringUtils.isEmpty(account)) {
				log.error(userId + ":账号不能为空");
				return new ResultModel(false, "账号不能为空", null);
			}
			if (account.length() < 5) {
				log.error(userId + ":账号至少5位");
				return new ResultModel(false, "账号至少5位", null);
			}
			user.setAccount(account);
			this.userDao.save(user);
			return new ResultModel(true);
		};
	}

	@PostMapping("findOtherInfo")
	public Callable<ResultModel> findOtherInfo(long userId) {
		return () -> {
			UserDmo user = userDao.findOne(userId);
			if (null == user) {
				log.error("用户ID不存在");
				throw new Exception("用户ID不存在");
			}
			Map<String, Object> map = new HashMap<>();
			map.put("nickname", user.getNickname());
			map.put("id", user.getId());
			map.put("isInit", user.getIsInit());
			map.put("gender", user.getGender());
			map.put("labels", user.getLabels());
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("findSuggestions")
	public Callable<ResultModel> findSuggestions(Integer page, Integer size) {
		return () -> {
			Integer newPage = page == null ? 0 : page;
			Integer newSize = size == null ? 10 : size;
			Page<SuggestionDmo> p = suggestionDao.findAll(new PageRequest(ValidatorBean.page(newPage),
					ValidatorBean.size(newSize), Direction.DESC, "createTime"));
			return new ResultModel(true, null, p.getContent());
		};
	}

	@PostMapping("findTransactionDetails")
	public Callable<ResultModel> findTransactionDetails(long userId, String token, int page, int size) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			Pageable pageable = new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC,
					"createTime");
			List<TransactionDetailsVo> vos = new LinkedList<>();
			Page<TransactionDetailsDmo> p = this.transactionDetailsDao.findByUser_Id(userId, pageable);
			for (TransactionDetailsDmo d : p.getContent()) {
				TransactionDetailsVo vo = new TransactionDetailsVo(d.getDescription(), d.getMoney(), d.getCreateTime());
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	/**
	 * @param phone
	 * @throws Exception
	 */
	@RequestMapping(value = "getSmsCode", method = RequestMethod.POST)
	public Object getSmsCode(@RequestParam String phone) throws Exception {
		SmsCodeDmo codeDmo = smsCodeDao.findByPhone(phone);
		if (codeDmo != null) {
			Timestamp sendExpiration = new Timestamp(codeDmo.getExpiration().getTime() - 29 * 60 * 1000);
			Timestamp now = new Timestamp(System.currentTimeMillis());
			if (now.before(sendExpiration)) {
				String second = Math.ceil(
						(codeDmo.getExpiration().getTime() - 29 * 60 * 1000 - System.currentTimeMillis()) / 1000) + "";
				second = second.replace(".0", "");
				log.error("请于" + second + "秒后获取验证码");
				throw new Exception("请于" + second + "秒后获取验证码");
			}
		}
		String code = smsCodeBo.getSmsCode();
		RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Authorization", Keys.JPhsh.AUTHORIZATION);
		Map<String, Object> jsonMap = new HashMap<>(), temp = new HashMap<>();
		jsonMap.put("mobile", phone);
		jsonMap.put("temp_id", Keys.JPhsh.TEMP_ID);
		temp.put("code", code);
		jsonMap.put("temp_para", temp);
		HttpEntity<String> formEntity = new HttpEntity<String>(Formatter.gson.toJson(jsonMap), headers);
		String str;
		try {
			str = restTemplate.postForObject("https://api.sms.jpush.cn/v1/messages", formEntity, String.class);
		} catch (Exception e) {
			log.error(e.getMessage());
			if (e.getMessage().contains("403")) {
				throw new Exception("手机号码不正确");
			}
			throw e;
		}
		// 请求成功
		if (str.contains("msg_id")) {
			// 如果发送成功就把验证码和手机号存进数据库
			if (codeDmo != null) {
				codeDmo.setSmscode(code);
				codeDmo.setExpiration(new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000));
				smsCodeDao.save(codeDmo);
			} else {
				codeDmo = new SmsCodeDmo();
				codeDmo.setPhone(phone);
				codeDmo.setSmscode(code);
				codeDmo.setExpiration(new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000));
				smsCodeDao.save(codeDmo);
			}
			return new ResultModel();
		} else {
			// 失败，返回给客户端发送失败
			log.error("发送失败");
			return new ResultModel(false, "发送失败", null);
		}
	}

	@RequestMapping(value = "initInfo", method = RequestMethod.POST)
	public Callable<ResultModel> initInfo(long userId, String token, String nickname, String password, boolean gender,
			String birthday, String recommenderAccount) throws Exception {
		return () -> {
			Date birth = null;
			try {
				birth = StringUtils.isEmpty(birthday) ? null : Formatter.dateFormatter.parse(birthday);
			} catch (Exception e) {
				log.error("日期转换不正确");
				return new ResultModel(false, "日期转换不正确", null);
			}
			if (birth != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(birth);
				Calendar now = Calendar.getInstance();
				int yeardiff = (now.get(Calendar.YEAR) - cal.get(Calendar.YEAR));
				if (yeardiff < 13 || yeardiff > 100) {
					log.error("年龄最小为13岁，最大为100岁");
					return new ResultModel(false, "年龄最小为13岁，最大为100岁", null);
				}
			}
			UserDmo user = userBo.initInfo(userId, token, nickname, password, gender, birth, recommenderAccount);
			return new ResultModel(true, "保存成功", user);
		};
	}

	@PostMapping("isBind3Part")
	public Callable<ResultModel> isBind3Part(long userId, String token) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			Map<String, Boolean> map = new HashMap<>();
			map.put(Keys.LoginType.QQ, !StringUtils.isEmpty(user.getQqUid()));
			map.put(Keys.LoginType.WECHAT, !StringUtils.isEmpty(user.getWxUid()));
			map.put(Keys.LoginType.WEIBO, !StringUtils.isEmpty(user.getXlwbUid()));
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("login")
	public Callable<ResultModel> login(String phone, String password) throws Exception {
		return () -> {
			UserDmo user = userBo.loginByPassword(phone, password, "PHONE");
			Map<String, Object> map = new HashMap<>();
			map.put("id", user.getId());
			String token = userBo.requestNewToken(user);
			map.put("token", token);
			map.put("isInit", user.getIsInit());
			return new ResultModel(true, "登录成功", map);
		};
	}

	@RequestMapping(value = "loginBySmsCode", method = RequestMethod.POST)
	public Callable<ResultModel> loginBySmsCode(String phone, String smsCode, Long recommenderId) throws Exception {
		return () -> {
			if (StringUtils.isEmpty(phone)) {
				log.error("手机号不能为空");
				throw new Exception("手机号不能为空");
			}
			SmsCodeDmo smsCodeDmo = smsCodeDao.findByPhone(phone);
			if (null == smsCodeDmo || StringUtils.isEmpty(smsCodeDmo.getSmscode())) {
				log.error("请获取验证码");
				throw new Exception("请获取验证码");
			}
			if (smsCodeDmo.getExpiration().before(new Date())) {
				log.error("验证码失效");
				throw new Exception("验证码失效");
			}
			if (!smsCodeDmo.getSmscode().equals(smsCode)) {
				log.error("验证码不正确");
				throw new Exception("验证码不正确");
			}
			UserDmo user = userDao.findByPhone(phone);
			Map<String, Object> map = new HashMap<>();
			if (user == null) {
				user = userBo.register(phone, Keys.LoginType.PHONE, recommenderId);
				map.put("id", user.getId());
				map.put("token", user.getToken());
				map.put("isInit", user.getIsInit());
				map.put("register", true);
				return new ResultModel(true, "注册成功", map);
			} else {
				map.put("id", user.getId());
				map.put("isInit", user.getIsInit());
				String token = userBo.requestNewToken(user);
				map.put("token", token);
				map.put("register", false);
				return new ResultModel(true, "登录成功", map);
			}
		};
	}

	@PostMapping("removeLabel")
	public Callable<ResultModel> removeLabel(String token, long userId, String removedLabel) {
		return () -> {
			this.userBo.removeLabel(userId, token, removedLabel);
			return new ResultModel();
		};
	}

	@PostMapping("suggest")
	public Callable<ResultModel> suggest(Long userId, String token, String content, String photoUrl) {
		return () -> {
			userBo.suggest(userId, token, content, photoUrl);
			return new ResultModel();
		};
	}

	@RequestMapping(method = RequestMethod.POST, value = "updateNickname")
	public Callable<ResultModel> updateNickname(String nickname, String token, long userId) throws Exception {
		return () -> {
			userBo.updateNickname(userId, token, nickname);
			return new ResultModel();
		};
	}

	@RequestMapping(method = RequestMethod.POST, value = "updatePassword")
	public Callable<ResultModel> updatePassword(Long userId, String token, String password, String password2)
			throws Exception {
		return () -> {
			if (StringUtils.isEmpty(password2)) {
				log.error("新密码不能为空");
				throw new Exception("新密码不能为空");
			}
			UserDmo userDmo = userBo.loginByToken(userId, token);
			if (!MD5Util.getSecurityCode(password).equals(userDmo.getPassword())) {
				log.error("原密码不正确");
				throw new Exception("原密码不正确");
			}
			userDmo.setPassword(MD5Util.getSecurityCode(password2));
			userDao.save(userDmo);
			return new ResultModel(true, "修改成功", null);
		};
	}

	@RequestMapping(method = RequestMethod.POST, value = "updatePasswordByCode")
	public Callable<ResultModel> updatePasswordByCode(String phone, String password, String smsCode) throws Exception {
		return () -> {
			SmsCodeDmo smscodeDmo = smsCodeDao.findByPhone(phone);
			if (smscodeDmo == null) {
				log.error("请重新获取验证码");
				throw new Exception("请重新获取验证码");
			}
			if (smscodeDmo.getExpiration().before(new Date())) {
				log.error("验证码失效");
				throw new Exception("验证码失效");
			}
			if (!smscodeDmo.getSmscode().equals(smsCode)) {
				log.error("验证码不正确");
				throw new Exception("验证码不正确");
			}
			if (StringUtils.isEmpty(password)) {
				log.error("密码不能为空");
				throw new Exception("密码不能为空");
			}
			UserDmo userDmo = userDao.findByPhone(phone);
			if (null == userDmo) {
				log.error("该手机没有被注册");
				throw new Exception("该手机没有被注册");
			}
			userDmo.setPassword(MD5Util.getSecurityCode(password));
			userDao.save(userDmo);
			return new ResultModel();
		};
	}

	@PostMapping("updatePasswordByToken")
	public Callable<ResultModel> updatePasswordByToken(long userId, String token, String password) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (StringUtils.isEmpty(user.getPhone())) {
				user.setPassword(MD5Util.getSecurityCode(password));
				this.userDao.save(user);
				return new ResultModel();
			}
			throw new Exception("请通过验证码修改");
		};
	}

	@PostMapping("updateUserInfo")
	public Callable<ResultModel> updateUserInfo(long userId, String token, String birthday) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			Date birth = null;
			try {
				birth = Formatter.dateFormatter.parse(birthday);
				user.setBirthday(birth);
				this.userDao.save(user);
				return new ResultModel();
			} catch (Exception e) {
				log.error(e.getMessage());
				return new ResultModel(false, "日期格式不正确", null);
			}

		};
	}

	@PostMapping("validateSmscode")
	public Callable<ResultModel> validateSmscode(String phone, String smscode) {
		return () -> {
			SmsCodeDmo smscodeDmo = this.smsCodeDao.findByPhone(phone);
			if (smscodeDmo == null || StringUtils.isEmpty(smscodeDmo.getSmscode())) {
				log.error("请发送验证码");
				throw new Exception("请发送验证码");
			}
			if (smscodeDmo.getSmscode().equals(smscode)) {
				return new ResultModel();
			} else {
				log.error("验证码不正确");
				return new ResultModel(false, "验证码不正确", null);
			}
		};
	}

	@RequestMapping("isPhoneNotExists")
	public Callable<ResultModel> isPhoneNotExists(String phone) {
		return () -> {
			boolean isPhoneExists = this.userDao.existsByPhone(phone);
			if (isPhoneExists) {
				return new ResultModel(false, "该手机号已被绑定", null);
			}
			return new ResultModel();
		};
	}

	@Autowired
	JdbcDao jdbcDao;

	@PostMapping("calcRefundMoney")
	public Callable<ResultModel> calcRefundMoney(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			int refundMoney = this.jdbcDao.calcRefundMoney(userId);
			return new ResultModel(true, "", refundMoney);
		};
	}
}
