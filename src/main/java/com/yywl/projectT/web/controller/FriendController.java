package com.yywl.projectT.web.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.enums.FriendInvitationState;
import com.yywl.projectT.bo.FriendBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.FriendInvitationDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.FriendDmo;
import com.yywl.projectT.dmo.FriendInvitationDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.EvaluationVo;
import com.yywl.projectT.vo.FriendForEvaluatingVo2;
import com.yywl.projectT.vo.FriendInvitationVo;
import com.yywl.projectT.vo.UserForEvaluatingVo;

@RestController
@RequestMapping("friend")
public class FriendController {
	private final static Log log = LogFactory.getLog(FriendController.class);

	@Autowired
	FriendBo friendBo;

	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;

	@Autowired
	FriendDao friendDao;

	@Autowired
	RoomMemberDao RoomMemberDao;

	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	FriendInvitationDao friendInvitationDao;

	@PostMapping("setRemarks")
	public Callable<ResultModel> setRemarks(long userId,long friendId,String token,String remarks){
		return ()->{
			this.userBo.loginByToken(userId, token);
			FriendDmo friendDmo=this.friendDao.findByOwner_IdAndFriend_Id(userId, friendId);
			if (friendDmo==null||friendDmo.getPoint()==0) {
				log.error("owner:"+userId+",friendId:"+friendId);
				throw new Exception("你们不是好友关系");
			}
			friendDmo.setRemarks(remarks);
			this.friendDao.save(friendDmo);
			return new ResultModel(true);
		};
	}
	
	@PostMapping("becameFriend")
	public Callable<ResultModel> becameFriend(long senderId, String token, long receiverId, String origin) {
		return () -> {
			if (senderId == receiverId) {
				log.error("不能和自己成为好友");
				return new ResultModel(false, "不能和自己成为好友", null);
			}
			this.userBo.loginByToken(receiverId, token);
			FriendInvitationDmo invitationDmo1 = this.friendInvitationDao.findByOwner_IdAndFriend_Id(senderId,
					receiverId);
			if (invitationDmo1 == null) {
				invitationDmo1 = new FriendInvitationDmo(null, new UserDmo(senderId), new UserDmo(receiverId),
						new Date(), FriendInvitationState.已同意.ordinal());
			} else {
				invitationDmo1.setCreateTime(new Date());
				invitationDmo1.setState(FriendInvitationState.已同意.ordinal());
			}
			invitationDmo1.setOrigin(origin);
			this.friendInvitationDao.save(invitationDmo1);
			FriendInvitationDmo invitationDmo2 = this.friendInvitationDao.findByOwner_IdAndFriend_Id(receiverId,
					senderId);
			if (invitationDmo2 == null) {
				invitationDmo2 = new FriendInvitationDmo(null, new UserDmo(receiverId), new UserDmo(senderId),
						new Date(), FriendInvitationState.已同意.ordinal());
			} else {
				invitationDmo2.setCreateTime(new Date());
				invitationDmo2.setState(FriendInvitationState.已同意.ordinal());
			}
			invitationDmo2.setOrigin(origin);
			this.friendInvitationDao.save(invitationDmo2);
			this.friendBo.becameBlueFriends(senderId, receiverId, origin);
			return new ResultModel();
		};
	}

	/**
	 * 给好友添加评分
	 * 
	 * @param userId
	 * @param token
	 * @param otherId
	 * @param point
	 * @return
	 */
	@PostMapping(value = "evalute", produces = "application/json;charset=UTF-8")
	public Callable<ResultModel> evalute(@RequestBody(required = true) String params) {
		return () -> {
			if (StringUtils.isEmpty(params)) {
				log.error("传参失败");
				throw new Exception("传参失败");
			}
			log.info(params);
			@SuppressWarnings("unchecked")
			Map<String, Object> param = Formatter.gson.fromJson(params, Map.class);
			Long userId = (Long.valueOf(param.get("userId") + ""));
			Long roomId = (Long.valueOf(param.get("roomId") + ""));
			String token = (String) param.get("token");
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> evaluationVos = (List<Map<String, Object>>) param.get("evaluations");
			List<EvaluationVo> vos = new LinkedList<>();
			for (Map<String, Object> map : evaluationVos) {
				@SuppressWarnings("unchecked")
				EvaluationVo vo = new EvaluationVo((Long.valueOf("" + map.get("friendId"))),
						map.get("friendPoint") == null ? 5 : Integer.valueOf(map.get("friendPoint") + ""),
						(List<String>) map.get("labels") == null ? new LinkedList<>()
								: (List<String>) map.get("labels"));
				// 获取活动表现评价
				int roomEvaluationPoint = Integer.valueOf(map.get("roomEvaluationPoint") + "");
				vo.setRoomEvaluationPoint(roomEvaluationPoint);
				vos.add(vo);
			}
			this.friendBo.evalute(userId, token, roomId, vos);
			return new ResultModel();
		};
	}

	@PostMapping(value = "evaluteV2", produces = "application/json;charset=UTF-8")
	public Callable<ResultModel> evaluteV2(@RequestBody(required = true) String params) {
		return () -> {
			if (StringUtils.isEmpty(params)) {
				log.error("传参失败");
				throw new Exception("传参失败");
			}
			log.info(params);
			@SuppressWarnings("unchecked")
			Map<String, Object> param = Formatter.gson.fromJson(params, Map.class);
			Long userId = (Long.valueOf(param.get("userId") + ""));
			Long roomId = (Long.valueOf(param.get("roomId") + ""));
			String token = (String) param.get("token");
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> evaluationVos = (List<Map<String, Object>>) param.get("evaluations");
			List<EvaluationVo> vos = new LinkedList<>();
			for (Map<String, Object> map : evaluationVos) {
				String lable=map.containsKey("label")?map.get("label")+"":"";
				Long friendId=(Long.valueOf(map.get("friendId")+""));
				Integer friendPoint=map.get("friendPoint") == null ? 5 : Integer.valueOf(map.get("friendPoint") + "");
				EvaluationVo vo = new EvaluationVo(friendId,friendPoint,Arrays.asList(lable));
				// 获取活动表现评价
				int roomEvaluationPoint = Integer.valueOf(map.get("roomEvaluationPoint") + "");
				vo.setRoomEvaluationPoint(roomEvaluationPoint);
				vos.add(vo);
			}
			this.friendBo.evalute(userId, token, roomId, vos);
			return new ResultModel();
		};
	}

	@PostMapping("findAllFriends")
	public Callable<ResultModel> findAllFriends(Long userId, String token) throws Exception {
		return () -> {
			UserDmo user = this.userBo.loginByToken(userId, token);
			List<FriendDmo> friends = friendBo.findFriends(user.getId());
			List<Map<String,Object>> vos = new LinkedList<>();
			for (FriendDmo conn : friends) {
				if (conn.getPoint() == 0) {
					continue;
				}
				Map<String,Object> map=new HashMap<>();
				UserDmo friend = conn.getFriend();
				String avatarSignature = friend.getAvatarSignature();
				map.put("id", friend.getId());
				map.put("point", (int) Math.floor(conn.getPoint()));
				map.put("nickname", friend.getNickname());
				map.put("avatarSignature", avatarSignature);
				map.put("vip", jdbcDao.isVip(friend.getId()));
				map.put("remarks", conn.getRemarks());
				vos.add(map);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findInvitations")
	public Callable<ResultModel> findInvitations(long userId, String token) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<FriendInvitationVo> voList = new LinkedList<>();
			List<FriendInvitationDmo> dmoList = this.friendInvitationDao.findByFriend_IdOrderByCreateTimeDesc(userId);
			for (FriendInvitationDmo dmo : dmoList) {
				FriendInvitationVo vo = new FriendInvitationVo();
				vo.setCreateTime(dmo.getCreateTime());
				vo.setGender(dmo.getOwner().getGender());
				vo.setId(dmo.getId());
				vo.setNickname(dmo.getOwner().getNickname());
				vo.setAvatarSignature(dmo.getOwner().getAvatarSignature());
				vo.setState(dmo.getState());
				vo.setUserId(dmo.getOwner().getId());
				vo.setOrigin(dmo.getOrigin());
				voList.add(vo);
			}
			return new ResultModel(true, "", voList);
		};
	}

	@PostMapping("findOtherInfo")
	public Callable<ResultModel> findOtherInfo(long otherId) {
		return () -> {
			UserDmo user = userDao.findOne(otherId);
			if (user == null) {
				log.error(otherId+"用户不存在");
				return new ResultModel(false, "用户不存在", null);
			}
			Map<String, Object> result = new HashMap<>();
			result.put("id", user.getId());
			result.put("avatarSignature", user.getAvatarSignature());
			result.put("nickname", user.getNickname());
			return new ResultModel(true, "", result);
		};
	}

	/**
	 * 查找房间内的好友
	 * 
	 * @param userId
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "findRoomFriends", method = RequestMethod.POST)
	public Callable<ResultModel> findRoomFriends(long userId, String token, long roomId) throws Exception {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<UserForEvaluatingVo> vos = jdbcDao.findFriendsToEvalute(userId, roomId);
			return new ResultModel(true, "", vos);
		};
	}

	@Autowired
	RoomDao roomDao;

	@RequestMapping(value = "findRoomFriendsV2", method = RequestMethod.POST)
	public Callable<ResultModel> findRoomFriendsV2(long userId, String token, long roomId) throws Exception {
		return () -> {
			this.userBo.loginByToken(userId, token);
			RoomDmo room = this.roomDao.findOne(roomId);
			if (null == room) {
				log.error(roomId + ":房间不存在");
				throw new Exception("房间不存在");
			}
			List<FriendForEvaluatingVo2> vos = jdbcDao.findFriendsToEvaluteV2(userId, roomId);
			Map<String, Object> data = new HashMap<>(), roomResult = new HashMap<>();
			boolean isScoring = room.getGame().isScoring();
			if (room.getMemberCount() == 0 && room.getJoinMember() < 2) {
				isScoring = false;
			} else if (room.getMemberCount() != 0 && room.getMemberCount() < 2) {
				isScoring = false;
			}
			roomResult.put("isScoring", isScoring);
			data.put("room", roomResult);
			data.put("evaluations", vos);
			return new ResultModel(true, "", data);
		};
	}

	@PostMapping("invitateFriend")
	public Callable<ResultModel> invitateFriend(long userId, String token, long friendId, String origin) {
		return () -> {
			if (userId == friendId) {
				log.error("不能邀请自己");
				return new ResultModel(false, "不能邀请自己", null);
			}
			this.friendBo.invitateFriend(userId, token, friendId, origin == null ? "" : origin);
			return new ResultModel();
		};
	}

	@PostMapping("loadFriendsByPhones")
	public Callable<ResultModel> loadFriendsByPhones(long userId, String token, String phoneStr) {
		return () -> {
			@SuppressWarnings("unchecked")
			List<String> phones = Formatter.gson.fromJson(phoneStr, List.class);
			if (null == phones || phones.isEmpty()) {
				return new ResultModel(true, "", new LinkedList<Map<String, Object>>());
			}
			List<Map<String, Object>> users = new LinkedList<>();
			for (String phone : phones) {
				UserDmo user = this.userDao.findByPhone(phone);
				if (null != user) {
					Map<String, Object> map = new HashMap<>();
					map.put("id", user.getId());
					map.put("nickname", user.getNickname());
					map.put("avatarSignature", user.getAvatarSignature());
					FriendDmo conn = this.friendDao.findByOwner_IdAndFriend_Id(userId, user.getId());
					if (null == conn) {
						map.put("friend", 0);
					} else {
						map.put("friend", conn.getPoint());
					}
					users.add(map);
					map.put("phone", phone);
				}
			}
			return new ResultModel(true, "", users);
		};
	}

	@PostMapping("receiveInvitation")
	public Callable<ResultModel> receiveInvitation(long userId, String token, long invitationId, int state) {
		return () -> {
			this.friendBo.receiveInvitation(userId, token, invitationId, state);
			return new ResultModel();
		};
	}

	@PostMapping("removeInvitation")
	public Callable<ResultModel> removeInvitation(long userId, String token, long invitationId) {
		return () -> {
			this.friendBo.removeInvitation(userId, token, invitationId);
			return new ResultModel();
		};
	}
}
