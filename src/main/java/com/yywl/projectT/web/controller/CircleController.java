package com.yywl.projectT.web.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bo.CircleBo;
import com.yywl.projectT.bo.RoomBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.UserCircleDao;
import com.yywl.projectT.dmo.CircleDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.UserCircleDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.CircleVo;
import com.yywl.projectT.vo.GameVo;
import com.yywl.projectT.vo.HomeRoomVo;
import com.yywl.projectT.vo.UserVo;

@RestController
@RequestMapping("circle")
public class CircleController {

	private final static Log log = LogFactory.getLog(CircleController.class);

	@Autowired
	CircleBo circleBo;

	@Autowired
	RoomBo roomBo;

	@Autowired
	CircleDao circleDao;

	@Autowired
	UserBo userBo;

	@Autowired
	RoomDao roomDao;

	@Autowired
	UserCircleDao userCircleDao;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	JdbcDao jdbcDao;

	@PostMapping("create")
	public Callable<ResultModel> create(long userId, String token, String place, String name, String city,
			String notice, double longitude, double latitude) {
		return () -> {
			CircleDmo dmo = circleBo.create(userId, token, name, city, place, notice, longitude, latitude);
			Map<String, Object> map = new HashMap<>();
			map.put("id", dmo.getId());
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("findByName")
	public Callable<ResultModel> findByName(String name, int page, int size) {
		return () -> {
			List<CircleDmo> circles = this.circleBo.findByNameLike(name, page, size);
			List<CircleVo> vos = new LinkedList<>();
			for (CircleDmo dmo : circles) {
				CircleVo vo = new CircleVo(dmo.getId(), dmo.getName(), dmo.getCity(), dmo.getPlace(), dmo.getNotice(),
						new UserVo(dmo.getManager().getId(), dmo.getManager().getNickname()), dmo.getMemberCount(),
						dmo.getLongitude(), dmo.getLatitude(), dmo.getHot(), dmo.getAvatarSignature(),
						dmo.getBgSignature());
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findByPage")
	public Callable<ResultModel> findByPage(int page, int size) {
		return () -> {
			List<CircleDmo> circles = this.circleBo.findPage(page, size);
			List<CircleVo> vos = new LinkedList<>();
			for (CircleDmo dmo : circles) {
				CircleVo vo = new CircleVo(dmo.getId(), dmo.getName(), dmo.getCity(), dmo.getPlace(), dmo.getNotice(),
						new UserVo(dmo.getManager().getId(), dmo.getManager().getNickname()), dmo.getMemberCount(),
						dmo.getLongitude(), dmo.getLatitude(), dmo.getHot(), dmo.getAvatarSignature(),
						dmo.getBgSignature());
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findCircleInfo")
	public Callable<ResultModel> findCircleInfo(long circleId, long userId, String token) {
		return () -> {
			userBo.loginByToken(userId, token);
			CircleDmo circleDmo = this.circleDao.findOne(circleId);
			if (null == circleDmo) {
				return new ResultModel();
			}
			int roomCount = (int) jdbc.queryForObject("select count(1) from room where belong_circle=" + circleId,
					Integer.class);
			circleDmo.setRoomCount(roomCount);
			UserCircleDmo userCircleDmo = userCircleDao.findByUser_IdAndCircle_Id(userId, circleId);
			CircleVo vo = new CircleVo(circleDmo.getId(), circleDmo.getName(), circleDmo.getCity(),
					circleDmo.getPlace(), circleDmo.getNotice(),
					new UserVo(circleDmo.getManager().getId(), circleDmo.getManager().getNickname(),
							circleDmo.getManager().getAvatarSignature()),
					circleDmo.getMemberCount(), circleDmo.getLongitude(), circleDmo.getLatitude(), circleDmo.getHot(),
					circleDmo.getAvatarSignature(), circleDmo.getBgSignature());
			vo.setRoomCount(roomCount);
			vo.setSign(userCircleDmo == null ? false : userCircleDmo.isSign());
			Map<String, Object> map = new HashMap<>();
			map.put("circle", vo);
			boolean isCircleMember = this.userCircleDao.existsByUser_IdAndCircle_Id(userId, circleId);
			if (isCircleMember) {
				map.put("experience", userCircleDmo.getExperience());
				map.put("joinCount", userCircleDmo.getJoinCount());
				map.put("createCount", userCircleDmo.getCreateCount());
			} else {
				map.put("experience", -1);
				map.put("joinCount", 0);
				map.put("createCount", 0);
			}
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("findCircleNearby")
	public Callable<ResultModel> findCircleNearby(double latitude, double longitude) {
		return () -> {
			List<CircleDmo> circles = this.circleBo.findNearBy(longitude, latitude);
			List<CircleVo> vos = new LinkedList<>();
			for (CircleDmo dmo : circles) {
				CircleVo vo = new CircleVo(dmo.getId(), dmo.getName(), dmo.getCity(), dmo.getPlace(), dmo.getNotice(),
						new UserVo(dmo.getManager().getId(), dmo.getManager().getNickname()), dmo.getMemberCount(),
						dmo.getLongitude(), dmo.getLatitude(), dmo.getHot(), dmo.getAvatarSignature(),
						dmo.getBgSignature());
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findMembers")
	public Callable<ResultModel> findMembers(long circleId) {
		return () -> {
			List<UserCircleDmo> userCircleDmos = this.userCircleDao.findByCircle_IdOrderByExperienceDesc(circleId);
			List<Map<String, Object>> list = new LinkedList<>();
			for (UserCircleDmo userCircleDmo : userCircleDmos) {
				UserDmo userDmo = userCircleDmo.getUser();
				Map<String, Object> map = new HashMap<>();
				map.put("id", userDmo.getId());
				map.put("nickname", userDmo.getNickname());
				map.put("avatarSignature", userDmo.getAvatarSignature());
				map.put("experience", userCircleDmo.getExperience());
				map.put("createCount", userCircleDmo.getCreateCount());
				map.put("joinCount", userCircleDmo.getJoinCount());
				map.put("isVip", jdbcDao.isVip(userDmo.getId()));
				list.add(map);
			}
			return new ResultModel(true, "", list);
		};
	}

	@PostMapping("findMyCircle")
	public Callable<ResultModel> findMyCircle(long userId, String token, int page, int size) {
		return () -> {
			List<CircleVo> vos = this.circleBo.findMyCircle(userId, token, page, size);
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findRecommand")
	public Callable<ResultModel> findRecommand() {
		return () -> {
			List<CircleDmo> circles = this.circleBo.findRecommand();
			List<CircleVo> vos = new LinkedList<>();
			for (CircleDmo dmo : circles) {
				UserVo manager = new UserVo(dmo.getManager().getId(), dmo.getManager().getNickname(),
						dmo.getManager().getAvatarSignature());
				CircleVo vo = new CircleVo(dmo.getId(), dmo.getName(), dmo.getCity(), dmo.getPlace(), dmo.getNotice(),
						manager, dmo.getMemberCount(), dmo.getLongitude(), dmo.getLatitude(), dmo.getHot(),
						dmo.getAvatarSignature(), dmo.getBgSignature());
				vo.setRoomCount(dmo.getRoomCount());
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findRoomsByCircle")
	public Callable<ResultModel> findRoomsByCircle(long circleId, int page, int size,Integer state) {
		return () -> {
			if (!circleDao.exists(circleId)) {
				return new ResultModel(false, "圈子不存在", null);
			}
			List<RoomDmo> roomDmos = this.jdbcDao.findCircleRooms(circleId,state,page,size);
			List<HomeRoomVo> vos = new LinkedList<>();
			for (RoomDmo dmo : roomDmos) {
				List<com.yywl.projectT.vo.HomeRoomVo.UserVo> userVos = this.jdbcDao.findUserVoByRoomId(dmo.getId());
				HomeRoomVo vo = new HomeRoomVo(dmo.getId(), dmo.getBeginTime(), dmo.getEndTime(),
						new GameVo(dmo.getGame().getId(), dmo.getGame().getName()), dmo.getLongitude(),
						dmo.getLatitude(), dmo.isLocked(), dmo.getMoney(), dmo.getName(), dmo.getPlace(),
						dmo.getJoinMember(), dmo.getJoinWomanMember(), dmo.getJoinManMember(), userVos,
						dmo.getManCount(), dmo.getWomanCount(), dmo.getMemberCount(), dmo.getState());
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findSimpleCircleInfo")
	public Callable<ResultModel> findSimpleCircleInfo(long circleId) {
		return () -> {
			CircleDmo circle = this.circleDao.findOne(circleId);
			if (circle == null) {
				log.error("圈子id不存在");
				return new ResultModel(false, "圈子id不存在", null);
			} else {
				Map<String, Object> map = new HashMap<>();
				map.put("id", circle.getId());
				map.put("name", circle.getName());
				map.put("avatarSignature", circle.getAvatarSignature());
				return new ResultModel(true, "", map);
			}
		};
	}

	@PostMapping("join")
	public Callable<ResultModel> join(long userId, String token, long circleId) {
		return () -> {
			circleBo.join(userId, token, circleId);
			return new ResultModel();
		};
	}

	@PostMapping("quit")
	public Callable<ResultModel> quit(long userId, String token, long circleId) {
		return () -> {
			circleBo.quit(userId, token, circleId);
			return new ResultModel();
		};
	}

	@PostMapping("remove")
	public Callable<ResultModel> remove(long userId, String token, long circleId) {
		return () -> {
			circleBo.remove(userId, token, circleId);
			return new ResultModel();
		};
	}

	@PostMapping("setOpen")
	public Callable<ResultModel> setOpen(long roomId, long userId, String token, boolean isOpen) {
		return () -> {
			this.roomBo.setOpen(userId, token, roomId, isOpen);
			return new ResultModel();
		};
	}

	@PostMapping("sign")
	public Callable<ResultModel> sign(long userId, String token, long circleId) {
		return () -> {
			this.circleBo.sign(userId, token, circleId);
			return new ResultModel();
		};
	}

	@PostMapping("update")
	public Callable<ResultModel> updateNotice(long userId, String token, long circleId, String notice) {
		return () -> {
			circleBo.update(userId, token, circleId, notice);
			return new ResultModel();
		};
	}

	@PostMapping("updateInfo")
	public Callable<ResultModel> updateInfo(long userId, String token, long circleId, String notice,String avatarSignature,String bgSignature) {
		return () -> {
			circleBo.update(userId, token, circleId, notice,avatarSignature,bgSignature);
			return new ResultModel();
		};
	}
	
	@PostMapping("updateImage")
	public Callable<ResultModel> updateImage(long circleId, long userId, String token, String signature, int type) {
		return () -> {
			this.circleBo.updateImage(userId, token, circleId, signature, type);
			return new ResultModel();
		};
	}

	@PostMapping("useBadge")
	public Callable<ResultModel> useBadge(long userId, String token, int badge, long circleId) {
		return () -> {
			this.circleBo.useBadge(userId, token, circleId, badge);
			return new ResultModel();
		};
	}
}
