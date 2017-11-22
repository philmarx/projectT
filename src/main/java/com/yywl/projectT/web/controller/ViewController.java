package com.yywl.projectT.web.controller;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;
import com.yywl.projectT.bean.ActivityDateBean;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.IPBean;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.RandomBean;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.Sha1Util;
import com.yywl.projectT.bean.WeixinSiginatureBean;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.bo.AliyunStsBo;
import com.yywl.projectT.bo.AugActivityLuckDrawBo;
import com.yywl.projectT.bo.RoomBo;
import com.yywl.projectT.bo.SeptActivityHelpBo;
import com.yywl.projectT.bo.SeptActivityMovieBo;
import com.yywl.projectT.bo.SmsCodeBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.AugActivityAllPrizeMoneyDao;
import com.yywl.projectT.dao.AugActivityHelperDao;
import com.yywl.projectT.dao.AugActivityLuckDrawDao;
import com.yywl.projectT.dao.AugActivityPopularGirlDao;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.SeptActivityHelpDao;
import com.yywl.projectT.dao.SeptActivityLuckDrawDao;
import com.yywl.projectT.dao.SeptActivityMovieDao;
import com.yywl.projectT.dao.SmsCodeDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.AugActivityAllPrizeMoneyDmo;
import com.yywl.projectT.dmo.AugActivityHelperDmo;
import com.yywl.projectT.dmo.AugActivityLuckDrawDmo;
import com.yywl.projectT.dmo.AugActivityPopularGirlDmo;
import com.yywl.projectT.dmo.CircleDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.SeptActivityHelpDmo;
import com.yywl.projectT.dmo.SeptActivityLuckDrawDmo;
import com.yywl.projectT.dmo.SeptActivityMovieDmo;
import com.yywl.projectT.dmo.SmsCodeDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.RoomMemberVo;

@RestController
@RequestMapping("view")
public class ViewController {

	private static final Log log = LogFactory.getLog(ViewController.class);
	@Autowired
	UserDao userDao;
	@Autowired
	RoomDao roomDao;
	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	RoomBo roomBo;

	@Autowired
	CircleDao circleDao;

	@Autowired
	SmsCodeDao smsCodeDao;

	@Autowired
	SmsCodeBo smsCodeBo;

	@Autowired
	UserBo userBo;

	@Autowired
	AugActivityLuckDrawDao luckDrawDao;

	@Autowired
	AugActivityLuckDrawBo luckDrawBo;

	@Autowired
	AugActivityAllPrizeMoneyDao allPrizeMoneyDao;

	@Autowired
	AugActivityHelperDao augActivityHelperDao;

	@Autowired
	AugActivityPopularGirlDao popularGirlDao;

	@Autowired
	SeptActivityMovieDao septActivityMovieDao;

	@Autowired
	SeptActivityMovieBo septActivityMovieBo;

	@PostMapping("findMovies")
	public Callable<ResultModel> findMovies() {
		return () -> {
			List<SeptActivityMovieDmo> list = this.septActivityMovieDao.findByIsEffective(true);
			return new ResultModel(true, null, list);
		};
	}

	@SuppressWarnings("unchecked")
	@PostMapping("voteForMovie")
	public Callable<ResultModel> voteForMovie(String params) {
		return () -> {
			List<Double> movieIdDoubles = null;
			List<Integer> movieIds = new LinkedList<>();
			try {
				movieIdDoubles = Formatter.gson.fromJson(params, List.class);
				for (Double d : movieIdDoubles) {
					movieIds.add(d.intValue());
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				return new ResultModel(false, "参数格式不正确", null);
			}
			if (null == movieIds || movieIds.isEmpty()) {
				return new ResultModel(false);
			}
			this.septActivityMovieBo.vote(movieIds);
			return new ResultModel();
		};
	}

	@PostMapping("allPrizeInfo")
	public Callable<ResultModel> allPrizeInfo() {
		return () -> {
			AugActivityAllPrizeMoneyDmo dmo = this.allPrizeMoneyDao.findOne(0L);
			Map<String, Object> data = new HashMap<>();
			data.put("allMoney", dmo.getAllMoney());
			data.put("joinMember", dmo.getJoinMember());
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("findFrequency")
	public Callable<ResultModel> findFrequency(long userId, String token) {
		return () -> {
			Map<String, Object> data = new HashMap<>();
			UserDmo user = this.userBo.loginByToken(userId, token);
			long helpCount = this.augActivityHelperDao.countByHelper_Id(userId);
			long frequency = helpCount < 5 ? 1 : 2;
			frequency = frequency - this.luckDrawDao.countByUser_Id(user.getId());
			data.put("frequency", frequency);// 可抽奖次数
			data.put("helpCount", helpCount);
			return new ResultModel(true, "", data);
		};
	}

	/**
	 * 根据被助力者id查看 助力的人
	 * 
	 * @param userId
	 * @param token
	 * @return
	 */
	@PostMapping("findHelped")
	public Callable<ResultModel> findHelped(long userId) {
		return () -> {
			List<Map<String, Object>> list = new LinkedList<>();
			List<AugActivityHelperDmo> dmos = this.augActivityHelperDao.findByHelper_Id(userId);
			for (AugActivityHelperDmo dmo : dmos) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", dmo.getUser().getId());
				map.put("nickname", dmo.getUser().getNickname());
				map.put("avatarSignature", dmo.getUser().getAvatarSignature());
				list.add(map);
			}
			return new ResultModel(true, "", list);
		};
	}

	// 查看达人榜
	@PostMapping("findMaster")
	public Callable<ResultModel> findMaster() {
		return () -> {
			Map<String, Object> master = this.jdbcDao.findMaster();
			return new ResultModel(true, "", master);
		};
	};

	@PostMapping("findMyMaster")
	public Callable<ResultModel> findMyMaster(long userId, String token) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			Map<String, Object> master = this.jdbcDao.findMyMaster(userId);
			master.put("nickname", user.getNickname());
			return new ResultModel(true, "", master);
		};
	}

	@PostMapping("findPopularGirls")
	public Callable<ResultModel> findPopularGirls(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			AugActivityPopularGirlDmo me = this.popularGirlDao.findByUser_Id(userId);
			Page<AugActivityPopularGirlDmo> popularGirls = this.popularGirlDao
					.findAll(new PageRequest(0, 10, Direction.DESC, "point"));
			List<AugActivityPopularGirlDmo> dmoList = popularGirls.getContent();
			List<Map<String, Object>> resultList = new LinkedList<>();
			Map<String, Object> data = new HashMap<>();
			int myOrder = -1;
			for (int i = 0; i < dmoList.size(); i++) {
				AugActivityPopularGirlDmo dmo = dmoList.get(i);
				if (me != null && dmo.getUser().getId().longValue() == me.getUser().getId().longValue()) {
					myOrder = i + 1;
				}
				Map<String, Object> map = new HashMap<>();
				map.put("order", i + 1);
				map.put("nickname", dmo.getUser().getNickname());
				map.put("point", dmo.getPoint());
				map.put("userId", dmo.getUser().getId());
				resultList.add(map);
			}
			data.put("list", resultList);
			if (me == null) {
				data.put("me", null);
			} else {
				Map<String, Object> map = new HashMap<>();
				map.put("order", myOrder == -1 ? this.jdbcDao.findPopularGirlOrder(me.getId()) : myOrder);
				map.put("nickname", me.getUser().getNickname());
				map.put("point", me.getPoint());
				map.put("userId", me.getUser().getId());
				data.put("me", map);
			}
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("findPrizeList")
	public Callable<ResultModel> findPrizeList(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<Map<String, Object>> data = new LinkedList<>();
			Sort sort = new Sort(new Sort.Order(Direction.DESC, "drawTime"));
			List<AugActivityLuckDrawDmo> luckDraws = this.luckDrawDao.findByUser_Id(userId, sort);
			for (AugActivityLuckDrawDmo dmo : luckDraws) {
				Map<String, Object> map = new HashMap<>();
				map.put("drawTime", Formatter.dateTimeFormatter.format(dmo.getDrawTime()));
				map.put("money", dmo.getMoney());
				data.add(map);
			}
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("findRecommenders")
	public Callable<ResultModel> findRecommenders(long userId) {
		return () -> {
			List<Map<String, Object>> data = new LinkedList<>();
			List<UserDmo> recommanders = this.userDao.findByRecommenderId(userId);
			for (UserDmo userDmo : recommanders) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", userDmo.getId());
				map.put("nickname", userDmo.getNickname());
				data.add(map);
			}
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("findUserInfo")
	public Callable<ResultModel> findUserInfo(long userId) {
		return () -> {
			Map<String, Object> data = new HashMap<>();
			UserDmo user = this.userDao.findOne(userId);
			if (null == user) {
				log.error("userId不存在");
				return new ResultModel(false, "userId不存在", null);
			}
			data.put("id", user.getId());
			data.put("nickname", user.getNickname());
			data.put("avatarSignature", user.getAvatarSignature());
			data.put("gender", user.getGender());
			return new ResultModel(true, "", data);
		};
	}

	@RequestMapping(value = "getSmsCode", method = RequestMethod.POST)
	public Object getSmsCode(@RequestParam String phone) throws Exception {
		UserDmo user = this.userDao.findByPhone(phone);
		if (user != null) {
			log.error("您已注册过");
			return new ResultModel(false, "您已注册过", null);
		}
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
		RestTemplate restTemplate = new RestTemplate();
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
		String str = restTemplate.postForObject("https://api.sms.jpush.cn/v1/messages", formEntity, String.class);
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

	@SuppressWarnings("unchecked")
	@RequestMapping("getWXSignature")
	public Object getWXSignature(String url) {
		Map<String, Object> data = new HashMap<String, Object>();
		WeixinSiginatureBean bean = WeixinSiginatureBean.local.get();
		if (bean != null) {
			long now = System.currentTimeMillis();
			if ((now - bean.getExpire()) < 7200 * 1000) {
				String paramsA = "jsapi_ticket=" + bean.getTicket() + "&noncestr=" + bean.getNoncestr() + "&timestamp="
						+ bean.getTimestamp() + "&url=" + url;
				String signature = Sha1Util.getSha1(paramsA);
				data.put("signature", signature);
				data.put("noncestr", bean.getNoncestr());
				data.put("timestamp", bean.getTimestamp());
				return new ResultModel(true, "", data);
			}
		}
		bean = new WeixinSiginatureBean();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		StringBuilder builder = null;
		String str = null;
		Map<String, Object> map = null;
		builder = new StringBuilder("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential")
				.append("&appid=" + Keys.Weixin.SUBSCRIBE_ID + "&secret=" + Keys.Weixin.SUBSCRIBE_SECRET);
		str = restTemplate.getForObject(builder.toString(), String.class);
		map = Formatter.gson.fromJson(str, Map.class);
		if (!map.containsKey("access_token")) {
			return new ResultModel(false, "获取access_token失败", null);
		}
		// 获取access_token
		String access_token = map.get("access_token") + "";
		builder = new StringBuilder("https://api.weixin.qq.com/cgi-bin/ticket/getticket?")
				.append("access_token=" + access_token).append("&type=jsapi");
		str = restTemplate.getForObject(builder.toString(), String.class);
		map = Formatter.gson.fromJson(str, Map.class);
		if (!map.containsKey("ticket")) {
			return new ResultModel(false, "获取ticket失败", null);
		}
		// 获取ticket
		String jsapi_ticket = map.get("ticket") + "";
		String noncestr = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		String timestamp = System.currentTimeMillis() / 1000 + "";
		String paramsA = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url="
				+ url;
		String signature = Sha1Util.getSha1(paramsA);
		data.put("signature", signature);
		data.put("noncestr", noncestr);
		data.put("timestamp", timestamp);
		bean.setExpire(Long.valueOf(timestamp) * 1000);
		bean.setAccessToken(access_token);
		bean.setTicket(jsapi_ticket);
		bean.setNoncestr(noncestr);
		bean.setTimestamp(timestamp);
		WeixinSiginatureBean.local.set(bean);
		return new ResultModel(true, "", data);
	}

	@PostMapping("help")
	public Object help(long userId, String token, long helperId, HttpServletRequest request) {
		try {
			if (userId == helperId) {
				log.error("自己不能给自己助力");
				return new ResultModel(false, "自己不能给自己助力", null);
			}
			UserDmo user = this.userBo.loginByToken(userId, token);
			boolean exists = this.augActivityHelperDao.existsByUser_IdAndHelper_Id(userId, helperId);
			if (exists) {
				log.error("助力已存在");
				return new ResultModel(false, "助力已存在", null);
			}
			long augCount = this.augActivityHelperDao.countByUser_Id(user.getId());
			if (augCount >= 1) {
				log.error("最多助力1个人");
				return new ResultModel(false, "最多助力1个人", null);
			}
			long helpCount = this.augActivityHelperDao.countByHelper_Id(helperId);
			if (helpCount >= 5) {
				log.error("助力失败");
				return new ResultModel(false, "助力失败", null);
			}
			AugActivityHelperDmo dmo = new AugActivityHelperDmo(null, user, new UserDmo(helperId),
					IPBean.getIpAddress(request));
			this.augActivityHelperDao.save(dmo);
			return new ResultModel();
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ResultModel(false, e.getMessage(), null);
		}
	}

	@Autowired
	SeptActivityHelpBo septActivityHelpBo;

	@PostMapping("septHelp")
	public Callable<ResultModel> septHelp(long userId, String token, long helperId, String movieName,
			HttpServletRequest request) {
		return () -> {
			try {
				this.septActivityHelpBo.help(userId, token, helperId, movieName, IPBean.getIpAddress(request));
				return new ResultModel(true);
			} catch (Exception e) {
				log.error(e.getMessage());
				return new ResultModel(false, e.getMessage(), null);
			}
		};
	}

	@PostMapping("findSeptHelped")
	public Callable<ResultModel> findSeptHelped(String movieName, long userId) {
		return () -> {
			List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
			List<SeptActivityHelpDmo> list = this.septActivityHelpDao.findByHelper_IdAndMovieName(userId, movieName);
			for (SeptActivityHelpDmo dmo : list) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", dmo.getUser().getId());
				map.put("nickname", dmo.getUser().getNickname());
				map.put("avatarSignature", dmo.getUser().getAvatarSignature());
				data.add(map);
			}
			return new ResultModel(true, null, data);
		};
	}

	@Autowired
	SeptActivityHelpDao septActivityHelpDao;

	@RequestMapping("inviteCircle")
	public Callable<ResultModel> inviteCircle(long userId, long circleId) {
		return () -> {
			UserDmo userDmo = this.userDao.findOne(userId);
			if (userDmo == null) {
				log.error("邀请者不存在");
				return new ResultModel(false, "邀请者不存在", null);
			}
			CircleDmo circleDmo = this.circleDao.findOne(circleId);
			if (circleDmo == null) {
				log.error("圈子不存在");
				return new ResultModel(false, "圈子不存在", null);
			}
			Map<String, Object> data = new TreeMap<>();
			Map<String, Object> inviter = new TreeMap<>();
			inviter.put("id", userDmo.getId());
			inviter.put("nickname", userDmo.getNickname());
			data.put("inviter", inviter);
			Map<String, Object> circle = new TreeMap<>();
			circle.put("id", circleDmo.getId());
			circle.put("name", circleDmo.getName());
			circle.put("notice", circleDmo.getNotice());
			data.put("circle", circle);
			return new ResultModel(true, "", data);
		};
	}

	@Autowired
	SeptActivityLuckDrawDao septActivityLuckDrawDao;

	@Autowired
	JdbcTemplate jdbc;

	@PostMapping("findSeptLuckDrawFrequence")
	public Callable<ResultModel> findSeptLuckDrawFrequence(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			Map<String, Object> map = new HashMap<>();
			String sql = "SELECT count(1) FROM spread_user s where s.user_id=" + userId;
			int count = this.jdbc.queryForObject(sql, Integer.class);
			if (count != 0) {
				map.put("remainDraw", 0);// 剩余抽奖机会
				map.put("remainRecommend", 0);// 剩余推荐人数
				return new ResultModel(true, "", map);
			}
			Long recomends = this.userDao.countByRecommenderIdAndIsInit(userId, true);
			Long drawFrequence = this.septActivityLuckDrawDao.countByUser_Id(userId);
			long remainDraw = (recomends - 3 * drawFrequence) / 3;
			long remainRecommend = 3 - (recomends - 3 * drawFrequence) % 3;
			map.put("remainDraw", remainDraw);// 剩余抽奖机会
			map.put("remainRecommend", remainRecommend);// 剩余推荐人数
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("listSeptLuckDraw")
	public Callable<ResultModel> listSeptLuckDraw(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<Map<String, Object>> data = new LinkedList<>();
			Sort sort = new Sort(Direction.DESC, "drawTime");
			List<SeptActivityLuckDrawDmo> list = this.septActivityLuckDrawDao.findByUser_Id(userId, sort);
			for (SeptActivityLuckDrawDmo dmo : list) {
				Map<String, Object> map = new HashMap<>();
				map.put("drawTime", dmo.getDrawTime());
				map.put("money", dmo.getMoney());
				data.add(map);
			}
			return new ResultModel(true, "", data);
		};
	}

	@RequestMapping("inviteRoom")
	public Callable<ResultModel> inviteRoom(long userId, long roomId) {
		return () -> {
			UserDmo userDmo = this.userDao.findOne(userId);
			if (userDmo == null) {
				log.error("邀请者不存在");
				return new ResultModel(false, "邀请者不存在", null);
			}
			RoomDmo roomDmo = this.roomBo.findOne(roomId);
			if (roomDmo == null) {
				log.error("房间不存在");
				return new ResultModel(false, "房间不存在", null);
			}
			if (roomDmo.getState() != ActivityStates.新建.ordinal()) {
				log.error("房间无法加入");
				return new ResultModel(false, "房间无法加入", null);
			}
			Map<String, Object> data = new TreeMap<>();
			Map<String, Object> inviter = new TreeMap<>();
			inviter.put("id", userDmo.getId());
			inviter.put("nickname", userDmo.getNickname());
			data.put("inviter", inviter);
			Map<String, Object> room = new TreeMap<>();
			room.put("id", roomDmo.getId());
			room.put("name", roomDmo.getName());
			room.put("description", roomDmo.getDescription());
			room.put("memberCount", roomDmo.getMemberCount());
			room.put("manCount", roomDmo.getManCount());
			room.put("womanCount", roomDmo.getWomanCount());
			room.put("joinMember", roomDmo.getJoinMember());
			room.put("joinManMember", roomDmo.getJoinManMember());
			room.put("joinWomanMember", roomDmo.getJoinWomanMember());
			room.put("longitude", roomDmo.getLongitude());
			room.put("latitude", roomDmo.getLatitude());
			room.put("beginTime", roomDmo.getBeginTime());
			room.put("money", roomDmo.getMoney());
			room.put("locked", roomDmo.isLocked());
			room.put("place", roomDmo.getPlace());
			room.put("city", roomDmo.getCity());
			List<Long> members = new LinkedList<>();
			for (RoomMemberVo vo : roomDmo.getJoinMembers()) {
				members.add(vo.getId());
			}
			room.put("members", members);
			room.put("gameId", roomDmo.getGame().getId());
			data.put("room", room);
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("login")
	public Callable<ResultModel> login(String type, String code, Long recommenderId) {
		return () -> {
			if (!Keys.LoginType.WECHAT.equals(StringUtils.isEmpty(type) ? Keys.LoginType.WECHAT : type)) {
				return new ResultModel(false, "暂不支持其它登录方式", null);
			}
			RestTemplate restTemplate = new RestTemplate();
			StringBuilder url = new StringBuilder().append("https://api.weixin.qq.com/sns/oauth2/access_token?")
					.append("appid=").append(Keys.Weixin.WEB_ID).append("&secret=").append(Keys.Weixin.WEB_SECRET)
					.append("&code=").append(code).append("&grant_type=authorization_code");
			String str = restTemplate.getForObject(url.toString(), String.class);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = Formatter.gson.fromJson(str, Map.class);
			if (!map.containsKey("unionid")) {
				return new ResultModel(false, "code失效", map);
			}
			String uid = map.get("unionid") + "";
			if (StringUtils.isEmpty(uid)) {
				return new ResultModel(false, "微信unionid不存在", null);
			}
			Map<String, Object> data = new HashMap<String, Object>();
			UserDmo user = this.userDao.findByWxUid(uid);
			if (user == null) {
				url = new StringBuilder("https://api.weixin.qq.com/sns/userinfo?");
				url.append("access_token=" + map.get("access_token") + "&openid=" + map.get("openid"));
				url.append("&lang=zh_CN");
				str = restTemplate.getForObject(url.toString(), String.class);
				@SuppressWarnings("unchecked")
				Map<String, Object> userInfoMap = Formatter.gson
						.fromJson(new String(str.getBytes("ISO-8859-1"), Charset.forName("UTF-8")), Map.class);
				String nickname = userInfoMap.get("nickname") + "";
				Boolean gender = null;
				if (map.containsKey("sex")) {
					int sex = Integer.valueOf(userInfoMap.get("sex") + "");
					switch (sex) {
					case 1:
						gender = true;
						break;
					case 2:
						gender = false;
						break;
					}
				}
				String avatarSignature = userInfoMap.get("headimgurl") + "";
				if (avatarSignature.startsWith("http:")) {
					avatarSignature = avatarSignature.replaceFirst("http:", "https:").trim();
				}
				try {
					this.userBo.register(uid, Keys.LoginType.WECHAT, nickname, gender, avatarSignature, recommenderId);
				} catch (Exception e) {
					log.error(e.getMessage());
					return new ResultModel(false, e.getMessage(), null);
				}
				user = this.userDao.findByWxUid(uid);
				data.put("newUser", true);
			} else {
				data.put("newUser", false);
			}
			data.put("id", user.getId());
			data.put("token", user.getToken());
			data.put("access_token", map.get("access_token"));
			data.put("refresh_token", map.get("refresh_token"));
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("lettleAppLogin")
	public Callable<ResultModel> lettleAppLogin(String code, Long recommenderId) {
		return () -> {
			RestTemplate restTemplate = new RestTemplate();
			StringBuilder url = new StringBuilder().append("https://api.weixin.qq.com/sns/oauth2/access_token?")
					.append("appid=").append(Keys.Weixin.LITTLE_APP_ID).append("&secret=")
					.append(Keys.Weixin.LITTLE_APP_SECRET).append("&code=").append(code)
					.append("&grant_type=authorization_code");
			String str = restTemplate.getForObject(url.toString(), String.class);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = Formatter.gson.fromJson(str, Map.class);
			if (!map.containsKey("unionid")) {
				return new ResultModel(false, "code失效", map);
			}
			String uid = map.get("unionid") + "";
			if (StringUtils.isEmpty(uid)) {
				return new ResultModel(false, "微信unionid不存在", null);
			}
			Map<String, Object> data = new HashMap<String, Object>();
			UserDmo user = this.userDao.findByWxUid(uid);
			if (user == null) {
				url = new StringBuilder("https://api.weixin.qq.com/sns/userinfo?");
				url.append("access_token=" + map.get("access_token") + "&openid=" + map.get("openid"));
				url.append("&lang=zh_CN");
				str = restTemplate.getForObject(url.toString(), String.class);
				if (str.contains("errcode")) {
					try {
						this.userBo.register(uid, Keys.LoginType.WECHAT, null, null, null, recommenderId);
					} catch (Exception e) {
						log.error(e.getMessage());
						return new ResultModel(false, e.getMessage(), null);
					}
				} else {
					@SuppressWarnings("unchecked")
					Map<String, Object> userInfoMap = Formatter.gson
							.fromJson(new String(str.getBytes("ISO-8859-1"), Charset.forName("UTF-8")), Map.class);
					String nickname = userInfoMap.get("nickname") + "";
					Boolean gender = null;
					if (map.containsKey("sex")) {
						int sex = Integer.valueOf(userInfoMap.get("sex") + "");
						switch (sex) {
						case 1:
							gender = true;
							break;
						case 2:
							gender = false;
							break;
						}
					}
					String avatarSignature = userInfoMap.get("headimgurl") + "";
					if (avatarSignature.startsWith("http:")) {
						avatarSignature = avatarSignature.replaceFirst("http:", "https:").trim();
					}
					try {
						this.userBo.register(uid, Keys.LoginType.WECHAT, nickname, gender, avatarSignature,
								recommenderId);
					} catch (Exception e) {
						log.error(e.getMessage());
						return new ResultModel(false, e.getMessage(), null);
					}
				}
				user = this.userDao.findByWxUid(uid);
				data.put("register", true);
				data.put("id", user.getId());
				data.put("token", user.getToken());
				data.put("isInit", user.getIsInit());
				return new ResultModel(true, "", data);
			}
			data.put("id", user.getId());
			data.put("token", user.getToken());
			data.put("isInit", user.getIsInit());
			boolean flag = false;
			flag = StringUtils.isEmpty(user.getQqUid()) && StringUtils.isEmpty(user.getXlwbUid());
			boolean existsFriend = this.friendDao.existsByOwner_IdOrFriend_Id(user.getId(), user.getId());
			if (existsFriend || user.getIsInit()) {
				flag = false;
			}
			data.put("register", flag);
			return new ResultModel(true, "", data);
		};
	}

	@Autowired
	FriendDao friendDao;

	@PostMapping("luckDraw")
	public Callable<ResultModel> luckDraw(long userId, String token, HttpServletRequest request) {
		return () -> {
			Date now = new Date();
			if (ActivityDateBean.luckDrawStart() == null || now.before(ActivityDateBean.luckDrawStart())) {
				return new ResultModel(false, "活动未开始", null);
			}
			if (ActivityDateBean.luckDrawEnd() == null || now.after(ActivityDateBean.luckDrawEnd())) {
				return new ResultModel(false, "活动已结束", null);
			}
			UserDmo user = this.userBo.loginByToken(userId, token);
			Map<String, Object> data = this.luckDrawBo.draw(user);
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("septluckDraw")
	public Callable<ResultModel> septluckDraw(long userId, String token, HttpServletRequest request) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			Map<String, Object> data = this.luckDrawBo.septDraw(user);
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("testLuckDraw")
	public Callable<ResultModel> testLuckDraw(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			Map<String, Object> map = new HashMap<>();
			Random random = new Random(System.currentTimeMillis());
			int money = 0;
			int value = random.nextInt(10000);
			if (value < 5) {// 100元
				money = 10000;
			} else if (value < 88) {// 50元
				money = 5000;
			} else if (value < 950) {// 10元
				money = 1000;
			} else {// 5元
				money = 500;
			}
			map.put("money", money);
			map.put("frequency", RandomBean.random.nextInt(1));
			map.put("joinMember", 20);
			map.put("allMoney", 1800000);
			return new ResultModel(true, "", map);
		};
	}

	@RequestMapping(value = "autoInitInfo", method = RequestMethod.POST)
	public Callable<ResultModel> autoInitInfo(String code,String avatarUrl,String nickname,boolean gender) throws Exception {
		return () -> {
			RestTemplate restTemplate = new RestTemplate();
			StringBuilder url = new StringBuilder().append("https://api.weixin.qq.com/sns/jscode2session?")
					.append("appid=").append(Keys.Weixin.LITTLE_APP_ID).append("&secret=").append(Keys.Weixin.LITTLE_APP_SECRET)
					.append("&js_code=").append(code).append("&grant_type=authorization_code");
			String str = restTemplate.getForObject(url.toString(), String.class);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = Formatter.gson.fromJson(str, Map.class);
			if (!map.containsKey("unionid")) {
				return new ResultModel(false, "code失效", map);
			}
			String uid = map.get("unionid") + "";
			if (StringUtils.isEmpty(uid)) {
				return new ResultModel(false, "微信unionid不存在", null);
			}
			UserDmo user = this.userDao.findByWxUid(uid);
			String avatarSignature = System.currentTimeMillis()+"";
			//如果替换为https，则无法用restTemplate获取图片
			//Certificates does not conform to algorithm constraints; nested exception is javax.net.ssl.SSLHandshakeException: java.security.cert.CertificateException: Certificates does not conform to algorithm constraints
		/*	if (avatarSignature.startsWith("http:")) {
				avatarSignature = avatarSignature.replaceFirst("http:", "https:").trim();
			}*/
			if (user == null) {
				user = this.userBo.registerAndInit(uid, Keys.LoginType.WECHAT, nickname, gender, avatarSignature);
			} else {
				if (user.getIsInit()!=null&&user.getIsInit()) {
					throw new Exception("您已做过初始化");
				}
				user.setIsInit(true);
				user.setNickname(this.userBo.newNickname(nickname));
				user.setGender(gender);
				user.setAvatarSignature(avatarSignature);
				this.userDao.save(user);
			}
			final Long userId = user.getId();
			final String avatar = avatarUrl;
			Credentials credentials = null;
			try {
				credentials = aliyunStsBo.getStsTokenByUserId(userId);
				RestTemplate rest = new RestTemplate();
				byte[] bytes = rest.getForObject(avatar, byte[].class);
				// 创建OSSClient实例
				CredentialsProvider credentialsProvider = new DefaultCredentialProvider(
						new DefaultCredentials(credentials.getAccessKeyId(), credentials.getAccessKeySecret(),
								credentials.getSecurityToken()));
				OSSClient ossClient = new OSSClient(Keys.Aliyun.STS_ENDPOINT, credentialsProvider);
				// 上传
				ossClient.putObject(Keys.Aliyun.STS_BUCKET_NAME, "user/" + userId + "/avatar",
						new ByteArrayInputStream(bytes));
				// 关闭client
				ossClient.shutdown();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			return new ResultModel();
		};
	}

	@Autowired
	AliyunStsBo aliyunStsBo;
}
