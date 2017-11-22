package com.yywl.projectT.bo;

import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.dao.BadgeDetailsDao;
import com.yywl.projectT.dao.CircleDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.UserCircleDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.CircleDmo;
import com.yywl.projectT.dmo.UserCircleDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.CircleVo;
import com.yywl.projectT.vo.UserVo;

import io.rong.messages.InfoNtfMessage;

@Service
@Transactional(rollbackOn = Throwable.class)
public class CircleBoImpl implements CircleBo {

	private static final Log LOGGER = LogFactory.getLog(CircleBoImpl.class);
	@Autowired
	UserBo userBo;
	@Autowired
	UserDao userDao;

	@Autowired
	CircleDao circleDao;

	@Autowired
	BadgeBo badgeBo;

	@Autowired
	RongCloudBean rongCloud;

	@Autowired
	BadgeDetailsDao badgeDetailsDao;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	UserCircleDao userCircleDao;

	@Autowired
	RoomDao roomDao;

	@Override
	public CircleDmo create(long userId, String token, String name, String city, String place, String notice,
			double longitude, double latitude) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			LOGGER.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		if (user.getBadge() < Keys.Circle.badgeSpend) {
			LOGGER.error("叶子不够");
			throw new Exception("叶子不够");
		}
		if (StringUtils.isEmpty(name)) {
			LOGGER.error("圈子名不能为空");
			throw new Exception("圈子名不能为空");
		}
		if (StringUtils.isEmpty(city)) {
			LOGGER.error("城市不能为空");
			throw new Exception("城市不能为空");
		}
		if (StringUtils.isEmpty(place)) {
			LOGGER.error("地址不能为空");
			throw new Exception("地址不能为空");
		}
		if (!user.isAuthorized()) {
			LOGGER.error("请实名认证");
			throw new Exception("请实名认证");
		}
		boolean exists = this.circleDao.existsByName(name);
		if (exists) {
			LOGGER.error("圈子不能重名");
			throw new Exception("圈子不能重名");
		}
		long count=this.userCircleDao.countByUser_Id(userId);
		if (count>=Keys.Circle.JOIN_MAX) {
			LOGGER.error("最多加入"+Keys.Circle.JOIN_MAX+"个圈子");
			throw new Exception("最多加入"+Keys.Circle.JOIN_MAX+"个圈子");
		}
		this.badgeBo.createCircle(user);
		CircleDmo circleDmo = new CircleDmo();
		circleDmo.setName(name);
		circleDmo.setNotice(notice);
		circleDmo.setCity(city);
		circleDmo.setPlace(place);
		circleDmo.setManager(user);
		circleDmo.setLatitude(latitude);
		circleDmo.setLongitude(longitude);
		circleDmo.setMemberCount(1);
		circleDao.save(circleDmo);
		UserCircleDmo userCircle = new UserCircleDmo();
		userCircle.setUser(user);
		userCircle.setCircle(circleDmo);
		userCircle.setExperience(Keys.CREATE_BADGE_ADD_EXPERIENCE);
		userCircleDao.save(userCircle);
		new Thread(()->{
			try {
				rongCloud.createGroup("" + userId, Keys.Circle.PREFIX + circleDmo.getId(), circleDmo.getName());
				rongCloud.sendGroup(""+userId, circleDmo.getId()+"", new InfoNtfMessage("创建【"+circleDmo.getName()+"】成功", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		return circleDmo;
	}

	private static final Log log=LogFactory.getLog(CircleBoImpl.class);
	
	@Override
	public List<CircleDmo> findByManager_Id(Long userId) {
		List<CircleDmo> circles = circleDao.findByManager_Id(userId);
		return circles;
	}

	@Override
	public List<CircleDmo> findByNameLike(String name, int page, int size) {
		Page<CircleDmo> plist = this.circleDao.findByNameLike("%" + name + "%",
				new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size)));
		return plist.getContent();
	}

	@Override
	public List<CircleDmo> findNearBy(double longitude, double latitude) {
		StringBuilder builder = new StringBuilder(
				"select id,name,city,place,manager,notice,longitude,latitude,avatar_signature,bg_signature,hot,member_count ");
		builder.append(" from circle order by (POW(POW((latitude-?),2)+POW((longitude-?),2),0.5)) asc limit 0,8");
		List<CircleDmo> circles = jdbc.query(builder.toString(), new Object[] { latitude, longitude }, (rs, num) -> {
			CircleDmo circle = new CircleDmo();
			circle.setCity(rs.getString("city"));
			long creatorId = rs.getLong("manager");
			UserDmo userDmo = this.userDao.findOne(creatorId);
			circle.setManager(userDmo);
			circle.setId(rs.getLong("id"));
			circle.setLatitude(rs.getDouble("latitude"));
			circle.setLongitude(rs.getDouble("longitude"));
			circle.setName(rs.getString("name"));
			circle.setNotice(rs.getString("notice"));
			circle.setPlace(rs.getString("place"));
			circle.setAvatarSignature(rs.getString("avatar_signature"));
			circle.setBgSignature(rs.getString("bg_signature"));
			circle.setHot(rs.getInt("hot"));
			circle.setMemberCount(rs.getInt("member_count"));
			return circle;
		});
		return circles;
	}

	@Override
	public List<CircleDmo> findPage(int page, int size) {
		Page<CircleDmo> circlePage = this.circleDao
				.findAll(new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size)));
		return circlePage.getContent();
	}

	@Override
	public List<CircleDmo> findRecommand() {
		StringBuilder builder = new StringBuilder(
				"select c.id,c.name,c.city,c.place,c.manager,c.notice,c.longitude,c.latitude ");
		builder.append(" ,c.avatar_signature,c.bg_signature,c.hot,c.member_count memberCount");
		builder.append(" from circle c ");
		builder.append(" left join user_circle uc on uc.circle=c.id ");
		builder.append(" left join room r on r.belong_circle=c.id ");
		builder.append(" group by c.id ");
		builder.append(" order by c.hot desc limit 0,8 ");
		List<CircleDmo> circles = jdbc.query(builder.toString(), (rs, num) -> {
			CircleDmo circle = new CircleDmo();
			circle.setCity(rs.getString("city"));
			long creatorId = rs.getLong("manager");
			UserDmo userDmo = this.userDao.findOne(creatorId);
			circle.setManager(userDmo);
			circle.setId(rs.getLong("id"));
			circle.setLatitude(rs.getDouble("latitude"));
			circle.setLongitude(rs.getDouble("longitude"));
			circle.setName(rs.getString("name"));
			circle.setNotice(rs.getString("notice"));
			circle.setPlace(rs.getString("place"));
			circle.setAvatarSignature(rs.getString("avatar_signature"));
			circle.setBgSignature(rs.getString("bg_signature"));
			circle.setHot(rs.getInt("hot"));
			circle.setMemberCount(rs.getInt("memberCount"));
			int roomCount = (int) jdbc.queryForObject("select count(1) from room where belong_circle=" + circle.getId(),
					Integer.class);
			circle.setRoomCount(roomCount);
			return circle;
		});
		return circles;
	}

	@Override
	public void join(long userId, String token, long circleId) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			LOGGER.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		if (user.getBadge() < Keys.Circle.badgeSpend) {
			LOGGER.error("徽章不够");
			throw new Exception("徽章不够");
		}
		this.badgeBo.joinCircle(user);
		CircleDmo circle = circleDao.findOne(circleId);
		if (null == circle) {
			LOGGER.error("圈子不存在");
			throw new Exception("圈子不存在");
		}
		if (userCircleDao.existsByUser_IdAndCircle_Id(userId, circleId)) {
			LOGGER.error("您已经在圈子中");
			throw new Exception("您已经在圈子中");
		}
		long count=this.userCircleDao.countByUser_Id(userId);
		if (count>=Keys.Circle.JOIN_MAX) {
			LOGGER.error("最多加入"+Keys.Circle.JOIN_MAX+"个圈子");
			throw new Exception("最多加入"+Keys.Circle.JOIN_MAX+"个圈子");
		}
		circle.setMemberCount(circle.getMemberCount() + 1);
		UserCircleDmo userCircle = new UserCircleDmo();
		userCircle.setCircle(circle);
		userCircle.setUser(user);
		userCircle.setExperience(Keys.JOIN_CIRCLE_INIT_EXPERIENCE);
		userCircleDao.save(userCircle);
		new Thread(()->{
			try {
				rongCloud.joinGroup(userId + "", Keys.Circle.PREFIX + circle.getId(), circle.getName());
				rongCloud.sendGroup(Keys.RONGCLOUD_SYSTEM_ID, circleId + "",
						new InfoNtfMessage("欢迎【" + user.getNickname() + "】加入我们的圈子", ""));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	public void quit(long userId, String token, long circleId) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		UserCircleDmo userCircleDmo = userCircleDao.findByUser_IdAndCircle_Id(userId, circleId);
		if (null == userCircleDmo) {
			LOGGER.error("用户不在圈子里");
			throw new Exception("用户不在圈子里");
		}
		CircleDmo circle = userCircleDmo.getCircle();
		if (circle.getManager().getId().longValue() == user.getId().longValue()) {
			List<UserCircleDmo> list = this.userCircleDao.findByUser_IdNotAndCircle_Id(user.getId(),circle.getId(),new PageRequest(0, 1, Direction.DESC, "experience"))
					.getContent();
			if (null == list || list.isEmpty()) {
				jdbc.update("update room set belong_circle=null,open=1 where belong_circle=" + circleId + "");
				jdbc.update("delete from user_circle where circle=" + circleId);
				jdbc.update("delete from circle where id=" + circle.getId());
				new Thread(()->{
					try {
						rongCloud.destoryGroup(userId + "", Keys.Circle.PREFIX + circleId);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}).start();
			} else {
				UserCircleDmo newManager = list.get(0);
				circle.setManager(newManager.getUser());
				this.circleDao.save(circle);
			}
		} else {
			jdbc.update("delete from user_circle where user=" + user.getId() + " and circle=" + circleId);
			circle.setMemberCount(circle.getMemberCount() - 1);
			circleDao.save(circle);
		}
		new Thread(()->{
			try {
				this.rongCloud.sendGroup(userId + "", circleId + "", new InfoNtfMessage(user.getNickname() + "退出圈子", ""));
				this.rongCloud.quitGroup(userId + "", circleId + "");
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	public void remove(long userId, String token, long circleId) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		CircleDmo circle = circleDao.findOne(circleId);
		if (null == circle) {
			LOGGER.error("圈子ID不存在");
			throw new Exception("圈子ID不存在");
		}
		jdbc.update("update room set belong_circle=null,open=1 where belong_circle=" + circleId + "");
		jdbc.update("delete from user_circle where user=" + user.getId() + " and circle=" + circleId);
		circleDao.delete(circle);
		new Thread(()->{
			try {
				rongCloud.destoryGroup(userId + "", Keys.Circle.PREFIX + circleId);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	@Override
	public void sign(long userId, String token, long circleId) throws Exception {
		this.userBo.loginByToken(userId, token);
		UserCircleDmo userCircleDmo = this.userCircleDao.findByUser_IdAndCircle_Id(userId, circleId);
		if (userCircleDmo.isSign()) {
			LOGGER.error("今天已经签到过了");
			throw new Exception("今天已经签到过了");
		}
		userCircleDmo.setSign(true);
		userCircleDmo.setExperience(userCircleDmo.getExperience() + Keys.Circle.SIGN_EXPERIENCE_ADD);
		this.userCircleDao.save(userCircleDmo);
	}

	@Override
	public void update(long userId, String token, long circleId, String notice, String avatarSignature,
			String bgSignature) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		CircleDmo circle = circleDao.findOne(circleId);
		if (null == circle) {
			LOGGER.error("圈子ID不存在");
			throw new Exception("圈子ID不存在");
		}
		if (circle.getManager().getId() != user.getId()) {
			LOGGER.error("您无权修改");
			throw new Exception("您无权修改");
		}
		circle.setNotice(notice);
		circle.setAvatarSignature(avatarSignature);
		circle.setBgSignature(bgSignature);
		circleDao.save(circle);
		new Thread(()->{
			try {
				rongCloud.refreshGroup(Keys.Circle.PREFIX + circle.getId(), circle.getName());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

	/**
	 * 修改圈子头像，type=1修改主头像，type=2修改背景头像
	 * 
	 * @param userId
	 * @param token
	 * @param circleId
	 * @param signature
	 * @param type
	 * @throws Exception
	 */
	@Override
	public void updateImage(long userId, String token, long circleId, String signature, int type) throws Exception {
		UserDmo user = this.userBo.loginByToken(userId, token);
		CircleDmo circle = this.circleDao.findOne(circleId);
		if (user.getId() != circle.getManager().getId()) {
			LOGGER.error("您没有权限修改");
			throw new Exception("您没有权限修改");
		}
		switch (type) {
		case 1:
			circle.setAvatarSignature(signature);
			break;
		case 2:
			circle.setBgSignature(signature);
			break;
		default:
			LOGGER.error("类型不正确");
			throw new Exception("类型不正确");
		}
		circleDao.save(circle);
	}

	@Override
	public void useBadge(long userId, String token, long circleId, int badge) throws Exception {
		UserDmo user = this.userBo.loginByToken(userId, token);
		if (user.getBadge() < badge) {
			LOGGER.error("叶子不够");
			throw new Exception("叶子不够");
		}
		UserCircleDmo userCircleDmo = this.userCircleDao.findByUser_IdAndCircle_Id(userId, circleId);
		if (null == userCircleDmo) {
			LOGGER.error("您不在圈子里面");
			throw new Exception("您不在圈子里面");
		}
		badgeBo.useBadgeInCircle(userCircleDmo, badge);
	}

	@Override
	public List<CircleVo> findMyCircle(long userId, String token, int page, int size) throws Exception {
		List<CircleVo> vos = new LinkedList<>();
		UserDmo user = userBo.loginByToken(userId, token);
		Pageable pageable = new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size));
		Page<UserCircleDmo> userCirclesPage = userCircleDao.findByUser_Id(user.getId(), pageable);
		List<UserCircleDmo> userCircles = userCirclesPage.getContent();
		if (userCircles == null || userCircles.isEmpty()) {
			return vos;
		}
		for (UserCircleDmo userCircleDmo : userCircles) {
			CircleDmo circle = userCircleDmo.getCircle();
			Long count = this.roomDao.countByBelongCircle_Id(circle.getId());
			circle.setRoomCount(count.intValue());
			CircleVo vo = new CircleVo(circle.getId(), circle.getName(), circle.getCity(), circle.getPlace(),
					circle.getNotice(), new UserVo(circle.getManager().getId(), circle.getManager().getNickname()),
					circle.getMemberCount(), circle.getLongitude(), circle.getLatitude(), circle.getHot(),
					circle.getAvatarSignature(), circle.getBgSignature());
			vo.setRoomCount(circle.getRoomCount());
			vo.setSign(userCircleDmo.isSign());
			vos.add(vo);
		}
		return vos;
	}

	@Override
	public void update(long userId, String token, long circleId, String notice) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		CircleDmo circle = circleDao.findOne(circleId);
		if (null == circle) {
			LOGGER.error("圈子ID不存在");
			throw new Exception("圈子ID不存在");
		}
		if (circle.getManager().getId() != user.getId()) {
			LOGGER.error("您无权修改");
			throw new Exception("您无权修改");
		}
		circle.setNotice(notice);
		circleDao.save(circle);
		new Thread(()->{
			try {
				rongCloud.refreshGroup(Keys.Circle.PREFIX + circle.getId(), circle.getName());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
	}

}
