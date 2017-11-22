package com.yywl.projectT.dao;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.yywl.projectT.bean.ActivityDateBean;
import com.yywl.projectT.bean.DateFactory;
import com.yywl.projectT.bean.RandomLabels;
import com.yywl.projectT.dmo.CircleDmo;
import com.yywl.projectT.dmo.GameDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.FriendForEvaluatingVo2;
import com.yywl.projectT.vo.HomeRoomVo.UserVo;
import com.yywl.projectT.vo.TempEvaluationVo;
import com.yywl.projectT.vo.UserForEvaluatingVo;

@Repository
public class JdbcDao {
	@Autowired
	JdbcTemplate jdbc;

	public List<Long> findMember_IdsByRoom_Id(Long id) {
		String sql = "select member from room_member where room=" + id;
		List<Long> ids = jdbc.query(sql, (ResultSet resultSet, int num) -> {
			return resultSet.getLong("member");
		});
		return ids;
	}

	public long countRecommender(long userId) {
		String sql = "select count(1) count from user u1 inner join user u2 on u1.recommender_id=u2.id and u2.is_init=1 and u1.recommender_id=?";
		List<Long> list = jdbc.query(sql, (ResultSet rs, int num) -> {
			return rs.getLong("count");
		}, userId);

		return list.isEmpty() ? 0L : list.get(0);
	}

	public String findLables(long userId) {
		String sql = "select labels from user where id= ?";
		List<String> list = jdbc.query(sql, (ResultSet rs, int num) -> {
			return rs.getString("labels");
		}, userId);
		return list.isEmpty() ? "" : list.get(0);
	}

	@PersistenceContext
	EntityManager entityManager;

	public List<UserForEvaluatingVo> findFriendsToEvalute(long userId, long roomId) {
		RoomDmo roomDmo = entityManager.find(RoomDmo.class, roomId);
		boolean isScoring = roomDmo.getGame().isScoring() && roomDmo.getMemberCount() > 2;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT rm.member,u.nickname,u.avatar_signature,u.labels,f.evaluate_point point ")
				.append(" from room_member rm ").append(" inner join user u on rm.member=u.id ")
				.append(" inner join friend_connection f on f.owner=? and f.friend=rm.member ")
				.append(" where rm.room=? and rm.member<>?  and rm.ready=1 ");
		List<UserForEvaluatingVo> vos = jdbc.query(sql.toString(), new Object[] { userId, roomId, userId },
				(ResultSet rs, int num) -> {
					UserForEvaluatingVo userVo = new UserForEvaluatingVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getString("avatar_signature"));
					userVo.setPoint(rs.getInt("point"));
					userVo.setScoring(isScoring);
					Random random = new Random(System.currentTimeMillis());
					userVo.getLabels().add(RandomLabels.LABELS[random.nextInt(RandomLabels.LABELS.length)]);
					userVo.getLabels().add(RandomLabels.LABELS[random.nextInt(RandomLabels.LABELS.length)]);
					userVo.getLabels().add(RandomLabels.LABELS[random.nextInt(RandomLabels.LABELS.length)]);
					userVo.getLabels().add(RandomLabels.LABELS[random.nextInt(RandomLabels.LABELS.length)]);
					userVo.getLabels().add(RandomLabels.LABELS[random.nextInt(RandomLabels.LABELS.length)]);
					userVo.getLabels().add(RandomLabels.LABELS[random.nextInt(RandomLabels.LABELS.length)]);
					return userVo;
				});
		return vos;
	}

	@Transactional(rollbackOn = Throwable.class)
	public void cleanRoom() {
		String sql = "update room set state=3 where date_add(end_time, interval 24 hour)>now() and end_time<now() and state!=4";
		jdbc.update(sql);
		sql = "update room set state=4 where date_add(end_time, interval 24 hour)<=now() and state!=4";
		jdbc.update(sql);
		sql = "SELECT l.id FROM location l where not exists(select 1 from room r where r.id= l.room_id) and l.room_id is not null";
		List<Long> locations = jdbc.queryForList(sql, Long.class);
		if (!locations.isEmpty()) {
			String collection = locations.toString().replace("[", "(").replace("]", ")");
			sql = "update location set room_id=null where id in " + collection;
			jdbc.update(sql);
		}
		sql = "SELECT l.id FROM complaint l where not exists(select 1 from room r where r.id= l.room_id) and l.room_id is not null";
		List<Long> complaints = jdbc.queryForList(sql, Long.class);
		if (!complaints.isEmpty()) {
			String collection = complaints.toString().replace("[", "(").replace("]", ")");
			sql = "update complaint set room_id=null where id in " + collection;
			jdbc.update(sql);
		}

	}

	public List<TempEvaluationVo> findTempEvaluationsByRoomId(Long roomId) {
		StringBuilder sql = new StringBuilder(
				"select re.other_id user_id,avg(re.point) avg_point from room_evalutation re ");
		sql.append(" where re.room_id=? group by re.other_id order by avg_point desc");
		List<TempEvaluationVo> temps = jdbc.query(sql.toString(), new Object[] { roomId }, (ResultSet rs, int num) -> {
			TempEvaluationVo vo = new TempEvaluationVo();
			vo.setUserId(rs.getLong("user_id"));
			vo.setAvgPoint(rs.getDouble("avg_point"));
			return vo;
		});
		return temps;
	}

	/**
	 * 查看圈子里经验值最高的用户
	 * 
	 * @param circleId
	 * @return
	 */
	public Long findCircleMaxExperienceUserId(long circleId) {
		String sql = "select user from user_circle where circle=? order by experience DESC LIMIT 0,1 ";
		List<Long> list = jdbc.query(sql, (ResultSet rs, int num) -> {
			return rs.getLong("user");
		}, circleId);
		return list.isEmpty() ? 0L : list.get(0);
	}

	/**
	 * 查询用户在活动中是排名
	 * 
	 * @param userId
	 * @param gameId
	 * @return
	 */
	public long findRankingByUserIdAndGameId(long userId, int gameId) {
		String sql = "select count(1)+1 c from game_score where score>(select score from game_score where user_id="
				+ userId + " and game_id=" + gameId + ") and game_id=" + gameId;
		List<Long> list = jdbc.query(sql, (ResultSet rs, int num) -> {
			return rs.getLong("c");
		});
		return list.isEmpty() ? 0L : list.get(0);
	}

	/**
	 * 查看除了管理员之外的成员id
	 * 
	 * @param roomId
	 * @param managerId
	 * @return
	 */
	public List<Long> findMember_IdsByRoom_IdAndMember_IdNot(Long roomId, Long managerId) {
		String sql = "select member from room_member where room=" + roomId + " and member<>" + managerId;
		List<Long> ids = jdbc.query(sql, (ResultSet resultSet, int num) -> {
			return resultSet.getLong("member");
		});
		return ids;
	}

	@Transactional(rollbackOn = Throwable.class)
	public void clearSignCircle() {
		String sql = "update user_circle set is_sign=0";
		jdbc.execute(sql);
	}

	/**
	 * 查看是否是vip会员
	 * 
	 * @param userId
	 * @return
	 */
	public boolean isVip(long userId) {
		String sql = "select p.vip_expire_date>now() is_vip FROM prop p where p.user_id=" + userId;
		List<Boolean> list = jdbc.query(sql, (ResultSet rs, int num) -> {
			return rs.getBoolean("is_vip");
		});
		return list.isEmpty() ? false : list.get(0);
	}

	/**
	 * 查看房间内的成员
	 * 
	 * @param id
	 * @return
	 */
	public List<com.yywl.projectT.vo.HomeRoomVo.UserVo> findUserVoByRoomId(Long id) {
		String sql = "select u.id,u.avatar_signature from room_member rm inner join user u on u.id=rm.member where rm.room="
				+ id;
		List<UserVo> userVos = this.jdbc.query(sql, (ResultSet rs, int num) -> {
			UserVo userVo = new UserVo();
			userVo.setId(rs.getLong("id"));
			userVo.setAvatarSignature(rs.getString("avatar_signature"));
			return userVo;
		});
		return userVos;
	}

	public List<Long> findRoomIdByStateLessThan(int state) {
		String sql = "select id from room where state<" + state;
		List<Long> ids = jdbc.query(sql, (ResultSet rs, int num) -> {
			Long id = rs.getLong("id");
			return id;
		});
		return ids;
	}

	public int findUserCircleLevel(Long userId, Long circleId) {
		String sql = "SELECT experience FROM user_circle where user=" + userId + " and circle=" + circleId;
		List<Integer> list=jdbc.query(sql, (ResultSet rs,int num)->{
			return rs.getInt("experience");
		});
		int experience = list.isEmpty()?0:list.get(0);
		if (experience < 100) {
			return 1;
		}
		if (experience < 200) {
			return 2;
		}
		if (experience < 500) {
			return 3;
		}
		if (experience < 1000) {
			return 4;
		}
		return 5;
	}

	public Date findMaxDateFromGameScoreHistory() {
		String sql = "select max(create_date) from game_score_history";
		List<Date> list=this.jdbc.queryForList(sql,Date.class);
		return list.isEmpty()?new Date():list.get(0);
	}

	public List<RoomDmo> findCircleRooms(long circleId, Integer state,int page, int size) {
		StringBuilder sql = new StringBuilder(
				"SELECT r.id,r.state,r.name,r.belong_circle,r.manager,r.place,r.begin_time,r.evaluate_time,");
		sql.append(" r.end_time,r.create_time,r.password,r.game,r.money,r.member_count,r.man_count,r.woman_count,");
		sql.append(" r.description,r.join_member,r.join_man_member,r.join_woman_member,r.longitude,r.latitude,");
		sql.append(
				" r.locked,r.city,r.prepare_time,r.open,r.game_mode,r.remaining_money,ABS(UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(r.begin_time)) timediff, ");
		sql.append(" g.name gname");
		sql.append(" FROM room r inner join game g on g.id=r.game where r.belong_circle=" + circleId);
		if (state!=null) {
			sql.append(" and r.state="+state.intValue()+" ");
		}
		sql.append(" order by r.state ,timediff");
		sql.append(" limit " + page * size + "," + size);
		List<RoomDmo> list = this.jdbc.query(sql.toString(), (ResultSet rs, int num) -> {
			RoomDmo dmo = new RoomDmo();
			dmo.setId(rs.getLong("id"));
			dmo.setState(rs.getInt("state"));
			dmo.setName(rs.getString("name"));
			dmo.setBelongCircle(new CircleDmo(rs.getLong("belong_circle")));
			dmo.setManager(new UserDmo(rs.getLong("manager")));
			dmo.setPlace(rs.getString("place"));
			dmo.setBeginTime(rs.getTimestamp("begin_time"));
			dmo.setEvaluateTime(rs.getTimestamp("evaluate_time"));
			dmo.setEndTime(rs.getTimestamp("end_time"));
			dmo.setCreateTime(rs.getTimestamp("create_time"));
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
			dmo.setPrepareTime(rs.getTimestamp("prepare_time"));
			dmo.setOpen(rs.getBoolean("open"));
			dmo.setGameMode(rs.getInt("game_mode"));
			dmo.setRemainingMoney(rs.getInt("remaining_money"));
			return dmo;
		});
		return list;
	}

	public List<Map<String, Object>> findMemberLocationsByRoomId(long roomId) {
		List<Map<String, Object>> data = new LinkedList<>();
		List<Long> list = this.findMember_IdsByRoom_Id(roomId);
		for (Long userId : list) {
			StringBuilder sql = new StringBuilder(
					"select u.id userId,u.nickname ,u.avatar_signature avatarSignature,l.longitude,l.latitude ");
			sql.append(" from location l inner join user u on u.id=l.user_id ");
			sql.append(" where l.user_id=" + userId + " order by l.send_time desc limit 0,1 ");
			List<Map<String, Object>> locations=this.jdbc.queryForList(sql.toString());
			if (!locations.isEmpty()) {
				data.addAll(locations);
			}
		}
		return data;
	}

	@Transactional(rollbackOn = Throwable.class)
	public void deletePropOrderByCreateTimeLessAndFinishedTime() {
		String sql = "delete from prop_order where create_time < ? and finish_time is null";
		this.jdbc.update(sql, DateFactory.getTodayStartTime());
	}

	@Transactional(rollbackOn = Throwable.class)
	public void updateMemberNickname(long userId, String nickname) throws Exception {
		String sql = "update room_member set nickname='" + nickname + "' where member=" + userId;
		jdbc.update(sql);
	}

	public List<Long> findGirlIdByRoom_Id(Long id) {
		String sql = "select u.id from user u inner join room_member rm on rm.member=u.id and u.gender=0 and rm.room="
				+ id;
		List<Long> ids = this.jdbc.queryForList(sql, Long.class);
		return ids;
	}

	public Map<String, Object> findMaster() {
		Map<String, Object> result = new HashMap<>();
		// 组织活动最多
		StringBuilder sql = new StringBuilder();
		sql.append("select u.id,u.nickname,count(1) count from room r ");
		sql.append("inner join user u on u.id=r.manager ");
		sql.append("where r.member_count>=5 and r.state>=2  and (r.end_time between ? and ?)");
		sql.append("group by u.id order by count desc limit 0,5 ");
		result.put("createRoomMaster",
				jdbc.queryForList(sql.toString(), ActivityDateBean.start(), ActivityDateBean.end()));
		// 狼人杀排行第一
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,g.score count from user u  ");
		sql.append("inner join game_score_history g on g.user_id=u.id where g.game_id=6 and ");
		sql.append(" g.create_date=(SELECT max(create_date) from game_score_history) ");
		sql.append("group by u.id order by count desc limit 0,5  ");
		result.put("werewolfMaster", jdbc.queryForList(sql.toString()));
		// 好友最多
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,count(1) count ");
		sql.append("from friend_connection f inner join user u on f.owner=u.id and f.point>0 ");
		sql.append("GROUP BY u.id order by count desc limit 0,5 ");
		result.put("friendMaster", jdbc.queryForList(sql.toString()));
		// 排行榜分数总和最高
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,sum(g.score) count from game_score_history g  ");
		sql.append(
				"inner join user u on u.id=g.user_id where g.create_date=(SELECT max(create_date) from game_score_history)");
		sql.append("group by u.id order by count desc limit 0,5 ");
		result.put("maxScoreMaster", jdbc.queryForList(sql.toString()));
		// 狼人杀参与次数最多
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,count(1) count from room_member rm ");
		sql.append(
				"inner join room r on rm.room=r.id inner join user u on u.id=rm.member and r.end_time between ? and ? ");
		sql.append("where r.member_count>=8 and r.state>=2 and r.game=6 ");
		sql.append("group by u.id order by count desc LIMIT 0,5 ");
		result.put("joinWerewolfMaster",
				jdbc.queryForList(sql.toString(), ActivityDateBean.start(), ActivityDateBean.end()));
		// 异性好友最多的男生
		sql = new StringBuilder();
		sql.append("select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
		sql.append("inner join user u1 on f1.owner=u1.id and u1.gender=1 ");
		sql.append("inner join user u2 on f1.friend=u2.id and u2.gender=0 ");
		sql.append("group by u1.id order by count desc limit 0,5 ");
		result.put("manFriendMaster", jdbc.queryForList(sql.toString()));
		// 异性好友最多的女生
		sql = new StringBuilder();
		sql.append("select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
		sql.append("inner join user u1 on f1.owner=u1.id and u1.gender=0 ");
		sql.append("inner join user u2 on f1.friend=u2.id and u2.gender=1 ");
		sql.append("group by u1.id order by count desc limit 0,5 ");
		result.put("womanFriendMaster", jdbc.queryForList(sql.toString()));
		// 金色好友最多
		sql = new StringBuilder();
		sql.append("select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
		sql.append("inner join user u1 on f1.owner=u1.id where f1.point>=8 ");
		sql.append("group by u1.id order by count desc limit 0,5 ");
		result.put("goldenFriendMaster", jdbc.queryForList(sql.toString()));
		return result;
	}

	public int findPopularGirlOrder(Long id) {
		String sql = "SELECT count(1) c FROM aug_activity_popular_girl p ";
		sql=sql+" where p.point>(select g.point from aug_activity_popular_girl g where g.id=?)";
		List<Integer> list=this.jdbc.query(sql, (ResultSet rs,int num)->{
			return rs.getInt("c");
		},id);
		int order = list.isEmpty()?0:list.get(0);
		return order + 1;
	}

	public Map<String, Object> findMyMaster(long userId) {
		Map<String, Object> result = new HashMap<>();
		Long count = 0L;
		Map<String, Object> map = null;
		List<Map<String, Object>> list = null;
		// 组织活动最多
		StringBuilder sql = new StringBuilder();
		sql.append("select u.id,u.nickname,count(1) count from room r ");
		sql.append("inner join user u on u.id=r.manager ");
		sql.append("where r.member_count>=5 and r.state>=2  and u.id= " + userId);
		sql.append(" and (r.end_time between ? and ?) ");
		sql.append(" group by u.id order by count desc limit 0,1 ");
		list = jdbc.queryForList(sql.toString(), ActivityDateBean.start(), ActivityDateBean.end());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("select count(s.id)+1 ");
			sql.append(" from (select u.id,u.nickname,count(1) count from room r ");
			sql.append(" inner join user u on u.id=r.manager ");
			sql.append(" where r.member_count>=5 and r.state>=2 ");
			sql.append(" group by u.id ) s").append(" where s.count>" + count);
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("createRoomMaster", list);
		// 狼人杀排行第一
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,g.score count from user u  ");
		sql.append("inner join game_score_history g on g.user_id=u.id where g.game_id=6 and ");
		sql.append("g.create_date=(SELECT max(create_date) from game_score_history) and u.id= " + userId);
		sql.append(" group by u.id order by count desc limit 0,1  ");
		list = jdbc.queryForList(sql.toString());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("select count(s.id)+1 from ");
			sql.append(" (select u.id,u.nickname,g.score count from user u ");
			sql.append(" inner join game_score_history g on g.user_id=u.id where g.game_id=6 and ");
			sql.append(" g.create_date=(SELECT max(create_date) from game_score_history) ");
			sql.append(" group by u.id) s where s.count>" + count);
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("werewolfMaster", list);
		// 好友最多
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,count(1) count ");
		sql.append("from friend_connection f inner join user u on f.owner=u.id and f.point>0 and u.id= " + userId);
		sql.append(" GROUP BY u.id order by count desc limit 0,1 ");
		list = jdbc.queryForList(sql.toString());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("SELECT count(s.id)+1 from(select u.id,u.nickname,count(1) count ");
			sql.append(" from friend_connection f inner join user u on f.owner=u.id and f.point>0 ");
			sql.append(" GROUP BY u.id) s where s.count>" + count);
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("friendMaster", list);
		// 排行榜分数总和最高
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,sum(g.score) count from game_score_history g  ");
		sql.append("inner join user u on u.id=g.user_id ");
		sql.append("where g.create_date=(SELECT max(create_date) from game_score_history) and u.id= " + userId);
		sql.append(" group by u.id order by count desc limit 0,1 ");
		list = jdbc.queryForList(sql.toString());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("select count(s.count)+1 from ");
			sql.append(" (select u.id,u.nickname,sum(g.score) count from game_score_history g ");
			sql.append(" inner join user u on u.id=g.user_id ");
			sql.append(" where g.create_date=(SELECT max(create_date) from game_score_history) ");
			sql.append(" group by u.id ) s where s.count>" + count);
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("maxScoreMaster", list);
		// 狼人杀参与次数最多
		sql = new StringBuilder();
		sql.append("select u.id,u.nickname,count(1) count from room_member rm ");
		sql.append("inner join room r on rm.room=r.id inner join user u on u.id=rm.member ");
		sql.append("where r.member_count>=8 and r.state>=2 and r.game=6 and u.id= " + userId);
		sql.append(" and r.end_time between ? and ?  ");
		sql.append(" group by u.id order by count desc LIMIT 0,1 ");
		list = jdbc.queryForList(sql.toString(), ActivityDateBean.start(), ActivityDateBean.end());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("select count(s.count)+1 from ");
			sql.append(" (select u.id,u.nickname,count(1) count from room_member rm ");
			sql.append(" inner join room r on rm.room=r.id inner join user u on u.id=rm.member ");
			sql.append(" where r.member_count>=8 and r.state>=2 and r.game=6 ");
			sql.append(" group by u.id ) s where s.count>" + count);
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("joinWerewolfMaster", list);
		// 异性好友最多的男生
		sql = new StringBuilder();
		sql.append("select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
		sql.append("inner join user u1 on f1.owner=u1.id and u1.gender=1 ");
		sql.append("inner join user u2 on f1.friend=u2.id and u2.gender=0 and u1.id= " + userId);
		sql.append(" group by u1.id order by count desc limit 0,1 ");
		list = jdbc.queryForList(sql.toString());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("select count(s.id)+1 from (select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
			sql.append(" inner join user u1 on f1.owner=u1.id and u1.gender=1 ");
			sql.append(" inner join user u2 on f1.friend=u2.id and u2.gender=0 ");
			sql.append(" group by u1.id ) s where s.count>" + count);
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("manFriendMaster", list);
		// 异性好友最多的女生
		sql = new StringBuilder();
		sql.append("select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
		sql.append("inner join user u1 on f1.owner=u1.id and u1.gender=0 ");
		sql.append("inner join user u2 on f1.friend=u2.id and u2.gender=1 and u1.id= " + userId);
		sql.append("  group by u1.id order by count desc limit 0,1 ");
		list = jdbc.queryForList(sql.toString());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("select count(s.id)+1 from (select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
			sql.append(" inner join user u1 on f1.owner=u1.id and u1.gender=0 ");
			sql.append(" inner join user u2 on f1.friend=u2.id and u2.gender=1 ");
			sql.append(" group by u1.id ) s where s.count>1");
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("womanFriendMaster", list);
		// 金色好友最多
		sql = new StringBuilder();
		sql.append("select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
		sql.append("inner join user u1 on f1.owner=u1.id where f1.point>=8 and u1.id= " + userId);
		sql.append(" group by u1.id order by count desc limit 0,1 ");
		list = jdbc.queryForList(sql.toString());
		map = list.isEmpty() ? null : list.get(0);
		if (map != null && map.containsKey("count")) {
			count = map.get("count") != null ? Long.valueOf(map.get("count") + "") : 0L;
			sql = new StringBuilder();
			sql.append("SELECT count(s.id)+1 from(select u1.id,u1.nickname,count(1) count from friend_connection f1 ");
			sql.append(" inner join user u1 on f1.owner=u1.id where f1.point>=8 ");
			sql.append(" group by u1.id) s where s.count>" + count);
			Integer order = jdbc.queryForObject(sql.toString(), Integer.class);
			map.put("order", order);
		}
		result.put("goldenFriendMaster", list);
		return result;
	}

	public int deleteTransactionDetails(Long id, String description) {
		String sql = "delete from transaction_details where user=? and description=?";
		return this.jdbc.update(sql, id, description);
	}

	public long countWithdrawalsByUser_IdAndImeiAndCreateTimeBetween(long userId, String imei, Date startTime,
			Date endTime) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(1) c FROM withdrawals w where  ");
		sql.append(" (w.user=? or w.imei=?) and w.create_time between ? and ? and w.state in (0,1,3)");
		List<Long> list = jdbc.query(sql.toString(), (ResultSet resultSet, int num) -> {
			return resultSet.getLong("c");
		}, userId, imei, startTime,endTime);
		return list.isEmpty() ? 0L : list.get(0);
	}

	public long countWithdrawalsByUser_IdAndCreateTimeBetween(long userId, Date startTime, Date endTime) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(1) c FROM withdrawals w where w.user=? ");
		sql.append(" and w.create_time between ? and ? and w.state in (0,1,3) ");
		List<Long> list = jdbc.query(sql.toString(), (ResultSet resultSet, int num) -> {
			return resultSet.getLong("c");
		}, userId, startTime, endTime);
		return list.isEmpty() ? 0L : list.get(0);
	}

	public int calcRefundMoney(long userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT sum(po.total_amount-po.refund_amount) refund FROM pay_order po");
		sql.append(" where po.out_trade_no like ?");
		List<Integer> list = jdbc.query(sql.toString(), (ResultSet rs, int num) -> {
			return rs.getInt("refund");
		}, userId + "a%");
		return list.isEmpty() ? 0 : list.get(0);
	}

	public List<FriendForEvaluatingVo2> findFriendsToEvaluteV2(long userId, long roomId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT rm.member,u.nickname,u.avatar_signature,u.labels,f.evaluate_point,rm.is_signed ")
				.append(" from room_member rm ").append(" inner join user u on rm.member=u.id ")
				.append(" inner join friend_connection f on f.owner=? and f.friend=rm.member ")
				.append(" where rm.room=? and rm.member<>?  and rm.ready=1 ");
		List<FriendForEvaluatingVo2> vos = jdbc.query(sql.toString(), new Object[] { userId, roomId, userId },
				(ResultSet rs, int num) -> {
					FriendForEvaluatingVo2 userVo = new FriendForEvaluatingVo2(rs.getLong("member"), rs.getString("nickname"),
							rs.getString("avatar_signature"));
					userVo.setFriendPoint(rs.getInt("evaluate_point"));
					userVo.setSigned(rs.getBoolean("is_signed"));
					Random random = new Random(System.currentTimeMillis());
					while(userVo.getLabels().size()<6) {
						userVo.getLabels().add(RandomLabels.LABELS[random.nextInt(RandomLabels.LABELS.length)]);
					}
					return userVo;
				});
		return vos;
	}

	public long countJoinMembersReady(long roomId) {
		StringBuilder sql=new StringBuilder("select count(1)+sum(rm.friend_cards) as readyCount from room_member rm");
		sql.append("  inner join room r on rm.room=r.id where r.id=? and rm.ready=1");
		List<Long> list=jdbc.query(sql.toString(),new Object[] {roomId},(ResultSet rs,int num)->{
			return rs.getLong("readyCount");
		});
		return list.isEmpty()?0:list.get(0);
	}

	public List<Map<String, Object>> findMyEvaluation(long userId, int page, int size) {
		StringBuilder sql=new StringBuilder();
		sql.append("SELECT de.id,de.content,de.create_time createTime,");
		sql.append(" u1.id senderId,u1.nickname senderName,u1.avatar_signature senderAvatarSignature,");
		sql.append(" u2.id declarerId,u2.nickname declarerName,d.content declarationContent ,d.id declarationId");
		sql.append(" FROM declaration_evaluation de left join user u1 on u1.id=de.sender_id ");
		sql.append(" left join declaration d on d.id=de.declaration_id");
		sql.append(" left join user u2 on d.declarer_id=u2.id");
		sql.append(" WHERE (de.declaration_id in (SELECT id FROM declaration WHERE declarer_id=?)) OR de.receiver_id=?");
		sql.append(" order by de.create_time desc limit ?,?");
		List<Map<String,Object>> list=this.jdbc.queryForList(sql.toString(), userId,userId,page*size,size);
		return list;
	}

	public List<Map<String, Object>> findOctRewardUsers(int size) {
		StringBuilder sql=new StringBuilder();
		sql.append(" SELECT u.nickname,o.bounty,ro.name roomName");
		sql.append(" FROM oct_room_user o left join user u on u.id=o.user_id");
		sql.append(" left join oct_room r on r.room_id=o.room_id inner join room ro on ro.id=r.room_id ");
		sql.append(" where o.bounty<>0  and o.state in (1,2)");
		sql.append(" order by r.reward_time  desc limit 0,?");
		List<Map<String,Object>> list=jdbc.queryForList(sql.toString(),size);
		return list;
	}

	public int findOctMoneyByUserId(long userId) {
		String sql="select sum(o.bounty) money from oct_room_user o where o.user_id=?";
		List<Integer> list=jdbc.query(sql, new Object[] {userId}, (ResultSet rs,int num)->{
			return rs.getInt("money");
		});
		return list.isEmpty()?0:list.get(0);
	}


	public List<Map<String, Object>> findMyJoinedRoom(long userId) {
		StringBuilder sql=new StringBuilder();
		sql.append("select oru.id,r.name,oru.state,oru.bounty,oru.reason,oru.is_foul from oct_room_user oru ");
		sql.append(" inner join room r on r.id=oru.room_id left join oct_room orm on orm.room_id=oru.room_id");
		sql.append(" where oru.user_id=? order by orm.reward_time desc ");
		List<Map<String,Object>> list=jdbc.query(sql.toString(),new Object[] {userId}, (ResultSet rs,int num)->{
			Map<String,Object> map=new HashMap<>();
			map.put("id", rs.getLong("id"));
			map.put("roomName", rs.getString("name"));
			map.put("state", rs.getInt("state"));
			map.put("bounty", rs.getInt("bounty"));
			map.put("message", rs.getString("reason"));
			map.put("foul", rs.getBoolean("is_foul"));
			return map;
		});
		return list;
	}

	public List<Map<String, Object>> findStrangers(long roomId) {
		StringBuilder sql=new StringBuilder();
		sql.append("select j.user_id,count(j.user_id) join_count,u.nickname,u.avatar_signature from join_room_log j ");
		sql.append("inner join room r on r.id=j.room_id ");
		sql.append("inner join user u on u.id=j.user_id ");
		sql.append("where j.room_id=? and r.manager<>j.user_id and  ");
		sql.append("not exists(select 1 from room_member rm where rm.room=j.room_id and rm.member=j.user_id ) ");
		sql.append("group by j.user_id ");
		List<Map<String,Object>> list=jdbc.query(sql.toString(),new Object[] {roomId}, (ResultSet rs,int num)->{
			Map<String,Object> map=new HashMap<>();
			map.put("userId", rs.getLong("user_id"));
			map.put("joinCount", rs.getInt("join_count"));
			map.put("nickname", rs.getString("nickname"));
			map.put("avatarSignature", rs.getString("avatar_signature"));
			return map;
		});
		return list;
	}
	

}
