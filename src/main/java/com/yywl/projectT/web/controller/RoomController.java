package com.yywl.projectT.web.controller;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;
import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.IPBean;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bean.component.RestTemplateBean;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bo.AliyunStsBo;
import com.yywl.projectT.bo.RoomBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.ComplaintDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomEvaluationDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.ComplaintDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.GameVo;
import com.yywl.projectT.vo.HomeRoomVo;
import com.yywl.projectT.vo.PersonalCenterRoomVo;
import com.yywl.projectT.vo.RoomMemberVo;
import com.yywl.projectT.vo.RoomResultMemberVo;
import com.yywl.projectT.vo.RoomVo;
import com.yywl.projectT.vo.UserVo;

@RestController
@RequestMapping("room")
public class RoomController {

	private static final Log log = LogFactory.getLog(RoomController.class);

	@Autowired
	RoomBo roomBo;

	@Autowired
	RoomEvaluationDao roomEvaluationDao;

	@Autowired
	RongCloudBean rongCloud;

	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	UserBo userBo;

	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	RoomDao roomDao;
	@Autowired
	ComplaintDao complaintDao;

	@Autowired
	UserDao userDao;
	
	@PostMapping("findRoomInfo")
	public Callable<ResultModel> findRoomInfo(String token ,long userId,long roomId){
		return ()->{
			this.userBo.loginByToken(userId, token);
			RoomMemberDmo roomMemberDmo=this.roomMemberDao.findByMember_IdAndRoom_Id(userId, roomId);
			if (roomMemberDmo==null) {
				throw new Exception("您没有加入过该房间");
			}
			Map<String,Object> data=new HashMap<>();
			data.put("evaluated", roomMemberDmo.isEvaluated());
			data.put("state", roomMemberDmo.getRoom().getState());
			return new ResultModel(true, "", data);
		};
	}
	
	@PostMapping("attend")
	public Callable<ResultModel> attend(long userId, String token, long roomId) {
		return () -> {
			this.roomBo.attend(userId, token, roomId);
			return new ResultModel();
		};
	}

	@PostMapping("cancelReady")
	public Callable<ResultModel> cancelReady(long userId, String token, long roomId) {
		return () -> {
			roomBo.cancelReady(userId, token, roomId);
			return new ResultModel();
		};
	}

	@PostMapping("complaint")
	public Callable<ResultModel> complaint(long userId, String token, Long roomId, long personId, String content,
			String photoUrl) {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (!userDao.exists(personId)) {
				log.error("投诉人不存在");
				throw new Exception("投诉人不存在");
			}
			ComplaintDmo complaintDmo = new ComplaintDmo();
			complaintDmo.setContent(content);
			complaintDmo.setCreateTime(new Date());
			complaintDmo.setOwner(user);
			if (roomId != null && roomId.longValue() != 0 && roomDao.exists(roomId)) {
				complaintDmo.setRoom(new RoomDmo(roomId));
			}
			complaintDmo.setPhotoUrl(photoUrl);
			complaintDmo.setPerson(new UserDmo(personId));
			this.complaintDao.save(complaintDmo);
			return new ResultModel();
		};
	}

	@Autowired
	RestTemplateBean restTemplateBean;
	
	@Autowired
	AliyunStsBo aliyunStsBo;
	
	/**
	 * 创建房间
	 */
	@RequestMapping(value = "createRoom", method = RequestMethod.POST)
	public Callable<ResultModel> createRoom(long userId, String token, String name, String password, String beginTime,
			String endTime, String place, int money, int memberCount, int manCount, int womanCount, String description,
			double longitude, double latitude, int gameId, String city, long belongCircle, boolean open, int gameMode)
			throws Exception {
		return () -> {
			RoomDmo dmo = this.roomBo.createRoom(userId, token, name, password, beginTime, endTime, place, money,
					memberCount, manCount, womanCount, description, longitude, latitude, gameId, city, belongCircle,
					open, gameMode);
			final long roomId=dmo.getId();
			Map<String, Object> map = new HashMap<>();
			map.put("id", roomId);
			new Thread(()->{
				Credentials credentials=null;
				try {
					credentials = aliyunStsBo.getStsTokenForRoom();
					RestTemplate rest = new RestTemplate(new SimpleClientHttpRequestFactory());
					rest.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
					StringBuilder url = new StringBuilder();
					url.append("https://api.weixin.qq.com/cgi-bin/token?");
					url.append("appid=" + Keys.Weixin.LITTLE_APP_ID + "&secret=" + Keys.Weixin.LITTLE_APP_SECRET);
					url.append("&grant_type=client_credential");
					String resultStr = rest.getForObject(url.toString(), String.class);
					@SuppressWarnings("unchecked")
					Map<String, Object> result = Formatter.gson.fromJson(resultStr, Map.class);
					String accessToken = (String) result.get("access_token");
					url = new StringBuilder();
					url.append("https://api.weixin.qq.com/wxa/getwxacode?access_token=" + accessToken);
					String paramsBody = "{\"path\":\"pages/roomturn/roomturn?roomId="+roomId+"\"}";
					HttpHeaders headers = new HttpHeaders();
					HttpEntity<String> httpEntity = new HttpEntity<>(paramsBody, headers);
					byte[] bytes = rest.postForObject(url.toString(), httpEntity, byte[].class);
					// 创建OSSClient实例
					CredentialsProvider credentialsProvider = new DefaultCredentialProvider(new DefaultCredentials(
							credentials.getAccessKeyId(), credentials.getAccessKeySecret(), credentials.getSecurityToken()));
					OSSClient ossClient = new OSSClient(Keys.Aliyun.STS_ENDPOINT,credentialsProvider);
					// 上传
					ossClient.putObject(Keys.Aliyun.STS_BUCKET_NAME, "roomQRcode/"+roomId, new ByteArrayInputStream(bytes));
					// 关闭client
					ossClient.shutdown();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
			return new ResultModel(true, "", map);
		};
	}

	/**
	 * 查找本人创建的房间
	 * 
	 * @param userId
	 * @param token
	 * @param page从0开始
	 * @param size
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "findMyCreateRooms", method = RequestMethod.POST)
	public Callable<ResultModel> findMyCreateRooms(long userId, String token, int page, int size) throws Exception {
		return () -> {
			List<RoomDmo> rooms = this.roomBo.findMyCreateRooms(userId, token, page, ValidatorBean.size(size));
			List<RoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : rooms) {
				UserVo manager = new UserVo(dmo.getManager().getId(), dmo.getManager().getNickname());
				manager.setAvatarSignature(dmo.getManager().getAvatarSignature());
				manager.setLabels(Formatter.gson.toJson(dmo.getManager().getLabels()));
				RoomVo vo = new RoomVo(dmo.getId(), dmo.getName(), dmo.getPlace(), manager, dmo.isOpen(),
						dmo.getBeginTime(), dmo.getEndTime(), dmo.getCreateTime(), dmo.getState(), dmo.isLocked(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getMoney(), dmo.getJoinMember(),
						dmo.getJoinManMember(), dmo.getJoinWomanMember(), dmo.getMemberCount(), dmo.getManCount(),
						dmo.getWomanCount(), dmo.getDescription(), dmo.getLongitude(), dmo.getLatitude(),
						dmo.getPrepareTime(), dmo.getJoinMembers(), dmo.getCity());
				vo.setAnonymous(dmo.isAnonymous());
				vos.add(vo);
			}
			return new ResultModel(true, "查找成功", vos);
		};
	}

	/**
	 * 查找我加入的房间
	 * 
	 * @param userId
	 * @param token
	 * @param page
	 * @param size
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "findMyJoinRooms", method = RequestMethod.POST)
	public Callable<ResultModel> findMyJoinRooms(Long userId, String token, int page, int size) throws Exception {
		return () -> {
			UserDmo userDmo = this.userBo.loginByToken(userId, token);
			List<RoomDmo> roomDmos = this.roomBo.findMyJoinRooms(userDmo, ValidatorBean.page(page),
					ValidatorBean.size(size));
			List<PersonalCenterRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.PersonalCenterRoomVo.UserVo> userVos = new LinkedList<>();
				if (dmo.getJoinMembers() != null && !dmo.getJoinMembers().isEmpty()) {
					for (RoomMemberVo user : dmo.getJoinMembers()) {
						com.yywl.projectT.vo.PersonalCenterRoomVo.UserVo userVo = new com.yywl.projectT.vo.PersonalCenterRoomVo.UserVo();
						userVo.setAvatarSignature(user.getAvatarSignature());
						userVo.setId(user.getId());
						userVos.add(userVo);
					}
				}
				boolean isManagerVip = jdbcDao.isVip(dmo.getManager().getId());
				PersonalCenterRoomVo vo = new PersonalCenterRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), isManagerVip);
				vo.setAnonymous(dmo.isAnonymous());
				vo.setState(dmo.getState());
				RoomMemberDmo roomMemberDmo = roomMemberDao.findByRoom_IdAndMember_Id(dmo.getId(), userId);
				boolean isEvaluated = roomMemberDmo == null ? false : roomMemberDmo.isEvaluated();
				vo.setEvaluated(isEvaluated);
				vos.add(vo);
			}
			return new ResultModel(true, "查找成功", vos);
		};
	}

	@RequestMapping(value = "findMyJoinRoomsV2", method = RequestMethod.POST)
	public Callable<ResultModel> findMyJoinRoomsV2(Long userId, String token, int page, int size) throws Exception {
		return () -> {
			UserDmo userDmo = this.userBo.loginByToken(userId, token);
			List<RoomDmo> roomDmos = this.roomBo.findMyJoinRoomsV2(userDmo, ValidatorBean.page(page),
					ValidatorBean.size(size));
			List<PersonalCenterRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.PersonalCenterRoomVo.UserVo> userVos = new LinkedList<>();
				if (dmo.getJoinMembers() != null && !dmo.getJoinMembers().isEmpty()) {
					for (RoomMemberVo user : dmo.getJoinMembers()) {
						com.yywl.projectT.vo.PersonalCenterRoomVo.UserVo userVo = new com.yywl.projectT.vo.PersonalCenterRoomVo.UserVo();
						userVo.setAvatarSignature(user.getAvatarSignature());
						userVo.setId(user.getId());
						userVos.add(userVo);
					}
				}
				boolean isManagerVip = jdbcDao.isVip(dmo.getManager().getId());
				PersonalCenterRoomVo vo = new PersonalCenterRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), isManagerVip);
				vo.setAnonymous(dmo.isAnonymous());
				vo.setState(dmo.getState());
				RoomMemberDmo roomMemberDmo = roomMemberDao.findByRoom_IdAndMember_Id(dmo.getId(), userId);
				boolean isEvaluated = roomMemberDmo == null ? false : roomMemberDmo.isEvaluated();
				vo.setEvaluated(isEvaluated);
				vos.add(vo);
			}
			return new ResultModel(true, "查找成功", vos);
		};
	}
	
	@PostMapping("findRoom")
	public Callable<ResultModel> findRoom(long roomId) {
		return () -> {
			RoomDmo dmo = roomBo.findOne(roomId);
			UserVo manager = new UserVo();
			manager.setId(dmo.getManager().getId());
			manager.setAvatarSignature(dmo.getManager().getAvatarSignature());
			manager.setNickname(dmo.getManager().getNickname());
			RoomVo vo = new RoomVo(dmo.getId(), dmo.getName(), dmo.getPlace(), manager, dmo.isOpen(),
					dmo.getBeginTime(), dmo.getEndTime(), dmo.getCreateTime(), dmo.getState(), dmo.isLocked(),
					new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getMoney(), dmo.getJoinMember(),
					dmo.getJoinManMember(), dmo.getJoinWomanMember(), dmo.getMemberCount(), dmo.getManCount(),
					dmo.getWomanCount(), dmo.getDescription(), dmo.getLongitude(), dmo.getLatitude(),
					dmo.getPrepareTime(), dmo.getJoinMembers(), dmo.getCity());
			vo.setAnonymous(dmo.isAnonymous());
			return new ResultModel(true, "", vo);
		};
	}

	@PostMapping("findRoomResult")
	public Callable<ResultModel> findRoomResult(long roomId) {
		return () -> {
			Map<String, Object> map = new HashMap<>();
			RoomDmo dmo = roomDao.findOne(roomId);
			RoomVo roomVo = new RoomVo(dmo.getId(), dmo.getName(), dmo.getPlace(),
					new UserVo(dmo.getManager().getId(), dmo.getManager().getNickname()), dmo.isOpen(),
					dmo.getBeginTime(), dmo.getEndTime(), dmo.getCreateTime(), dmo.getState(), dmo.isLocked(),
					new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getMoney(), dmo.getJoinMember(),
					dmo.getJoinManMember(), dmo.getJoinWomanMember(), dmo.getMemberCount(), dmo.getManCount(),
					dmo.getWomanCount(), dmo.getDescription(), dmo.getLongitude(), dmo.getLatitude(),
					dmo.getPrepareTime(), dmo.getJoinMembers(), dmo.getCity());
			roomVo.setAnonymous(dmo.isAnonymous());
			map.put("room", roomVo);
			List<RoomResultMemberVo> members = new LinkedList<>();
			List<RoomMemberDmo> roomMemberDmos = roomMemberDao.findByRoom_IdOrderByPointDesc(roomId);
			for (RoomMemberDmo roomMemberDmo : roomMemberDmos) {
				UserDmo user = roomMemberDmo.getMember();
				RoomResultMemberVo memberVo = new RoomResultMemberVo(user.getId(), user.getNickname(), true,
						user.getAvatarSignature());
				memberVo.setPoint(roomMemberDmo.getPoint());
				memberVo.setBadge(roomMemberDmo.getBadge());
				long gameOrder=this.jdbcDao.findRankingByUserIdAndGameId(user.getId(),dmo.getGame().getId());
				memberVo.setGlobalRanking(gameOrder);
				members.add(memberVo);
			}
			map.put("members", members);
			return new ResultModel(true, "", map);
		};

	}

	/**
	 * 查找房间
	 * 
	 * @param gameId
	 *            game的ID
	 * @param page
	 *            从0开始
	 * @param size
	 *            每页大小
	 * @param longitude
	 *            经度
	 * @param latitude
	 *            纬度
	 * @param sort
	 *            排序优先级,可以是join_member，distance,create_time；如果为空则是综合排序
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "findRoomsByGameOrder", method = RequestMethod.POST)
	public Callable<ResultModel> findRoomsByGameOrder(int gameId, int page, int size, Double longitude, Double latitude,
			int state, String games, String city) throws Exception {
		return () -> {
			List<RoomDmo> roomDmos = this.roomBo.findRoomsByGameOrder(gameId, games, state,
					longitude == null ? 0 : longitude, latitude == null ? 0 : latitude, page, ValidatorBean.size(size),
					city);
			List<HomeRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.HomeRoomVo.UserVo> userVos = new LinkedList<>();
				if (!dmo.getJoinMembers().isEmpty()) {
					for (RoomMemberVo user : dmo.getJoinMembers()) {
						com.yywl.projectT.vo.HomeRoomVo.UserVo userVo = new com.yywl.projectT.vo.HomeRoomVo.UserVo();
						userVo.setAvatarSignature(user.getAvatarSignature());
						userVo.setId(user.getId());
						userVos.add(userVo);
					}
				}
				HomeRoomVo vo = new HomeRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), dmo.getState());
				vo.setAnonymous(dmo.isAnonymous());
				boolean isVip = this.jdbcDao.isVip(dmo.getManager().getId());
				vo.setVip(isVip);
				vos.add(vo);
			}
			return new ResultModel(true, null, vos);
		};
	}

	@RequestMapping(value = "findRoomsByGameOrderV2", method = RequestMethod.POST)
	public Callable<ResultModel> findRoomsByGameOrderV2(int gameId, int page, int size, Double longitude, Double latitude,
			int state, String games, String city) throws Exception {
		return () -> {
			List<RoomDmo> roomDmos = this.roomBo.findRoomsByGameOrderV2(gameId, games, state,
					longitude == null ? 0 : longitude, latitude == null ? 0 : latitude, page, ValidatorBean.size(size),
					city);
			List<HomeRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.HomeRoomVo.UserVo> userVos = new LinkedList<>();
				if (!dmo.getJoinMembers().isEmpty()) {
					for (RoomMemberVo user : dmo.getJoinMembers()) {
						com.yywl.projectT.vo.HomeRoomVo.UserVo userVo = new com.yywl.projectT.vo.HomeRoomVo.UserVo();
						userVo.setAvatarSignature(user.getAvatarSignature());
						userVo.setId(user.getId());
						userVos.add(userVo);
					}
				}
				HomeRoomVo vo = new HomeRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), dmo.getState());
				vo.setAnonymous(dmo.isAnonymous());
				boolean isVip = this.jdbcDao.isVip(dmo.getManager().getId());
				vo.setVip(isVip);
				vos.add(vo);
			}
			return new ResultModel(true, null, vos);
		};
	}
	
	@PostMapping("findChatRooms")
	public Callable<ResultModel> findChatRooms(int page,int size){
		return () -> {
			List<RoomDmo> roomDmos = this.roomBo.findByGame_Id(30,ValidatorBean.page(page),ValidatorBean.size(size));
			List<HomeRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.HomeRoomVo.UserVo> userVos = new LinkedList<>();
					if (!dmo.getJoinMembers().isEmpty()) {
						for (RoomMemberVo user : dmo.getJoinMembers()) {
							com.yywl.projectT.vo.HomeRoomVo.UserVo userVo = new com.yywl.projectT.vo.HomeRoomVo.UserVo();
							userVo.setAvatarSignature(user.getAvatarSignature());
							userVo.setId(user.getId());
							userVo.setGender(user.isGender());
							userVos.add(userVo);
						}
					}
				HomeRoomVo vo = new HomeRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), dmo.getState());
				vo.setJoinMembers(userVos);
				vo.setAnonymous(dmo.isAnonymous());
				boolean isVip = this.jdbcDao.isVip(dmo.getManager().getId());
				vo.setVip(isVip);
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("getRoomTime")
	public Callable<ResultModel> getRoomTime(long roomId) {
		return () -> {
			RoomDmo room = this.roomDao.findOne(roomId);
			ResultModel result = new ResultModel();
			if (room.getPrepareTime() != null) {
				log.info(room.getPrepareTime().getTime());
				result.setData(room.getPrepareTime().getTime());
			}
			return result;
		};
	}

	@PostMapping("isJoined")
	public Callable<ResultModel> isJoined(long userId, String token, long roomId) {
		return () -> {
			boolean isJoined = roomBo.isJoined(userId, token, roomId);
			return new ResultModel(true, "", isJoined);
		};
	}

	@PostMapping("createChatRoom")
	public Callable<ResultModel> createChatRoom(long userId,String token,String name,boolean anonymous,String password){
		return ()->{
			UserDmo user= this.userBo.loginByToken(userId, token);
			RoomDmo room= this.roomBo.createChatRoom(user,name,anonymous, password);
			Map<String,Object> map=new HashMap<>();
			final long roomId=room.getId();
			map.put("id", roomId);
			map.put("anonymous", room.isAnonymous());
			new Thread(()->{
				Credentials credentials=null;
				try {
					credentials = aliyunStsBo.getStsTokenForRoom();
					RestTemplate rest = new RestTemplate(new SimpleClientHttpRequestFactory());
					rest.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
					StringBuilder url = new StringBuilder();
					url.append("https://api.weixin.qq.com/cgi-bin/token?");
					url.append("appid=" + Keys.Weixin.LITTLE_APP_ID + "&secret=" + Keys.Weixin.LITTLE_APP_SECRET);
					url.append("&grant_type=client_credential");
					String resultStr = rest.getForObject(url.toString(), String.class);
					@SuppressWarnings("unchecked")
					Map<String, Object> result = Formatter.gson.fromJson(resultStr, Map.class);
					String accessToken = (String) result.get("access_token");
					url = new StringBuilder();
					url.append("https://api.weixin.qq.com/wxa/getwxacode?access_token=" + accessToken);
					String paramsBody = "{\"path\":\"pages/roomturn/roomturn?roomId="+roomId+"\"}";
					HttpHeaders headers = new HttpHeaders();
					HttpEntity<String> httpEntity = new HttpEntity<>(paramsBody, headers);
					byte[] bytes = rest.postForObject(url.toString(), httpEntity, byte[].class);
					// 创建OSSClient实例
					CredentialsProvider credentialsProvider = new DefaultCredentialProvider(new DefaultCredentials(
							credentials.getAccessKeyId(), credentials.getAccessKeySecret(), credentials.getSecurityToken()));
					OSSClient ossClient = new OSSClient(Keys.Aliyun.STS_ENDPOINT,credentialsProvider);
					// 上传
					ossClient.putObject(Keys.Aliyun.STS_BUCKET_NAME, "roomQRcode/"+roomId, new ByteArrayInputStream(bytes));
					// 关闭client
					ossClient.shutdown();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				
								
			}).start();
			return new ResultModel(true, "", map);
		};
	}
	
	/**
	 * 加入房间
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param password
	 *            房间的密码如果没有可为空
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "joinRoom", method = RequestMethod.POST)
	public Callable<ResultModel> joinRoom(Long userId, String token, Long roomId, String password,
			HttpServletResponse res) throws Exception {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			this.roomBo.join(user, roomId, password);
			return new ResultModel();
		};
	}

	@PostMapping("leave")
	public Callable<ResultModel> leave(long userId, String token, long roomId) {
		return () -> {
			roomBo.leave(userId, token, roomId);
			return new ResultModel();
		};
	}

	@PostMapping("managerCancelRoom")
	public Callable<ResultModel> managerCancelRoom(long userId, String token, long roomId) {
		return () -> {
			this.roomBo.managerCancelRoom(userId, token, roomId);
			return new ResultModel();
		};
	}

	@PostMapping("notLateWithReason")
	public Callable<ResultModel> notLateWithReason(long userId, String token, long roomId, String reason,
			String photoUrl, long certifierId) {
		return () -> {
			this.roomBo.notLate(userId, token, roomId, reason, photoUrl, certifierId);
			return new ResultModel();
		};
	}

	@PostMapping("quitRoom")
	public Callable<ResultModel> quitRoom(long userId, String token, long roomId) {
		return () -> {
			roomBo.quit(userId, token, roomId);
			return new ResultModel();
		};
	}

	@PostMapping("ready")
	public Callable<ResultModel> ready(long userId, String token, long roomId) {
		return () -> {
			roomBo.ready(userId, token, roomId);
			return new ResultModel();
		};
	}

	@PostMapping("roomStart")
	public Callable<ResultModel> roomStart(long userId, String token, long roomId) {
		return () -> {
			UserDmo user = userBo.loginByToken(userId, token);
			RoomDmo room = this.roomDao.findOne(roomId);
			if (room == null) {
				log.error("房间不正确");
				throw new Exception("房间不正确");
			}
			
			long count = jdbcDao.countJoinMembersReady(roomId);
			if (count < 2) {
				log.error("房间准备的人数不能小于2");
				throw new Exception("房间准备的人数不能小于2");
			}
			if (room.getMemberCount().intValue()==0) {
				if (count<room.getJoinMember()) {
					log.error("房间有人未准备");
					throw new Exception("房间有人未准备");
				}
			}else {
				if (room.getJoinMember().intValue() < room.getMemberCount().intValue()) {
					log.error("房间人数没满");
					throw new Exception("房间人数没满");
				}
				if (count<room.getJoinMember().intValue()) {
					log.error("房间有人未准备");
					throw new Exception("房间有人未准备");
				}
			}
		
			long now = System.currentTimeMillis();
			long beginTime = room.getBeginTime().getTime();
			if (now > beginTime) {
				log.error("房间已经过期");
				throw new Exception("房间已经过期");
			}
			if (room.getPrepareTime() != null) {
				log.error("已经点击过开始");
				throw new Exception("已经点击过开始");
			}
			long joinMemberCount = this.roomMemberDao.countByRoom_IdAndReady(roomId, true);
			if (joinMemberCount < 2) {
				log.error("房间至少2人");
				throw new Exception("房间至少2人");
			}
			roomBo.begin(user, room, now);
			return new ResultModel();
		};
	}

	/**
	 * 由前端收到cmd命令sendLocation时调用。
	 * 
	 * @return
	 */
	@PostMapping("sendLocation")
	public Callable<ResultModel> sendLocation(long userId, String token, long roomId, double latitude, double longitude,
			String place, HttpServletRequest request) {
		return () -> {
			this.roomBo.sendLocation(userId, token, roomId, latitude, longitude, place, IPBean.getIpAddress(request));
			return new ResultModel();
		};
	}
	@PostMapping("sendLocationV2")
	public Callable<ResultModel> sendLocationV2(long userId, String token, Long roomId, double latitude, double longitude,
			String place,String udid, HttpServletRequest request) {
		return () -> {
			this.roomBo.sendLocationV2(userId, token, roomId, latitude, longitude, place, udid,IPBean.getIpAddress(request));
			return new ResultModel();
		};
	}

	/**
	 * 签到
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	@PostMapping("sign")
	public Callable<ResultModel> sign(long userId, String token, long roomId, double latitude, double longitude,
			HttpServletRequest request) {
		return () -> {
			this.roomBo.sign(userId, token, roomId, latitude, longitude, IPBean.getIpAddress(request));
			return new ResultModel();
		};
	}

	@PostMapping("signAgain")
	public Callable<ResultModel> signAgain(long userId, String token, long roomId, double latitude, double longitude,
			HttpServletRequest request) {
		return () -> {
			this.roomBo.signAgain(userId, token, roomId, latitude, longitude, IPBean.getIpAddress(request));
			return new ResultModel();
		};
	}

	@PostMapping("tiRen")
	public Callable<ResultModel> tiRen(long userId, String token, long roomId, long memberId, String reason) {
		return () -> {
			roomBo.tiRen(userId, token, roomId, memberId, reason);
			return new ResultModel();
		};
	}

	@PostMapping("updateRoom")
	public Callable<ResultModel> updateRoom(long userId, String token, long roomId, String name, String description,
			String beginTime, String endTime, int memberCount, int manCount, int womanCount) {
		return () -> {
			roomBo.updateRoom(userId, token, roomId, name, description, beginTime, endTime, memberCount, manCount,
					womanCount);
			return new ResultModel();
		};
	}

	@PostMapping("setOnline")
	public Callable<ResultModel> setOnline(long userId, String token, long roomId, boolean online) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			this.roomBo.setOnline(userId, roomId, online);
			return new ResultModel();
		};
	}

	@PostMapping("findMemberLocation")
	public Callable<ResultModel> findMemberLocation(long roomId, long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<Map<String, Object>> data = this.jdbcDao.findMemberLocationsByRoomId(roomId);
			return new ResultModel(true, "", data);
		};
	}

	
	@PostMapping("findMyRunningRooms")
	public Callable<ResultModel> findMyRunningRooms(long userId, String token, Integer gameId) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<RoomDmo> roomDmos = this.roomBo.findMyRunningRooms(userId, gameId);
			List<HomeRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.HomeRoomVo.UserVo> userVos = new LinkedList<>();
				if (!dmo.getJoinMembers().isEmpty()) {
					for (RoomMemberVo user : dmo.getJoinMembers()) {
						com.yywl.projectT.vo.HomeRoomVo.UserVo userVo = new com.yywl.projectT.vo.HomeRoomVo.UserVo();
						userVo.setAvatarSignature(user.getAvatarSignature());
						userVo.setId(user.getId());
						userVos.add(userVo);
					}
				}
				HomeRoomVo vo = new HomeRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), dmo.getState());
				vo.setPrepareTime(dmo.getPrepareTime());
				vo.setAnonymous(dmo.isAnonymous());
				boolean isVip = this.jdbcDao.isVip(dmo.getManager().getId());
				vo.setVip(isVip);
				vos.add(vo);
			}
			return new ResultModel(true, null, vos);
		};
	}
	@PostMapping("findMyRunningRoomsV2")
	public Callable<ResultModel> findMyRunningRoomsV2(long userId, String token, Integer gameId) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<RoomDmo> roomDmos = this.roomBo.findMyRunningRoomsV2(userId, gameId);
			List<HomeRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.HomeRoomVo.UserVo> userVos = new LinkedList<>();
				if (!dmo.getJoinMembers().isEmpty()) {
					for (RoomMemberVo user : dmo.getJoinMembers()) {
						com.yywl.projectT.vo.HomeRoomVo.UserVo userVo = new com.yywl.projectT.vo.HomeRoomVo.UserVo();
						userVo.setAvatarSignature(user.getAvatarSignature());
						userVo.setId(user.getId());
						userVos.add(userVo);
					}
				}
				HomeRoomVo vo = new HomeRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), dmo.getState());
				vo.setPrepareTime(dmo.getPrepareTime());
				vo.setAnonymous(dmo.isAnonymous());
				boolean isVip = this.jdbcDao.isVip(dmo.getManager().getId());
				vo.setVip(isVip);
				vos.add(vo);
			}
			return new ResultModel(true, null, vos);
		};
	}
	@PostMapping("findStrangers")
	public Callable<ResultModel> findStrangers(long roomId){
		return ()->{
			List<Map<String,Object>> list=this.jdbcDao.findStrangers(roomId);
			return new ResultModel(true, "", list);
		};
	}
}
