package com.yywl.projectT.bo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.yywl.projectT.vo.UserActivityVo;

@Service
@Transactional(rollbackOn = Throwable.class)
public class UserActivityBoImpl implements UserActivityBo {

	@Autowired
	JdbcTemplate jdbc;

	@Override
	public List<UserActivityVo> findByGame_Id(int gameId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select member,nickname,game_id,game_name,point,count");
		sql.append(",avatar_signature from v_room_member v where v.game_id=? ORDER BY POINT DESC limit 0,10");
		List<UserActivityVo> list = jdbc.query(sql.toString(), new Object[] { gameId }, (rs, num) -> {
			UserActivityVo vo = new UserActivityVo(rs.getLong("member"), rs.getString("nickname"), rs.getInt("point"),
					num + 1, gameId, rs.getString("game_name"), rs.getString("avatar_signature"), rs.getInt("count"));
			vo.setGameName(rs.getString("game_name"));
			return vo;
		});
		return list;
	}

	@Override
	public List<UserActivityVo> findByUser_Id(long userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select member,nickname,game_id,game_name,point,count");
		sql.append(",avatar_signature from v_room_member v where v.member=? ORDER BY POINT DESC limit 0,10");
		List<UserActivityVo> resultList = jdbc.query(sql.toString(), new Object[] { userId }, (rs, num) -> {
			UserActivityVo vo = new UserActivityVo(rs.getLong("member"), rs.getString("nickname"), rs.getInt("point"),
					num + 1, rs.getInt("game_id"), rs.getString("game_name"), rs.getString("avatar_signature"),
					rs.getInt("count"));
			return vo;
		});
		sql = new StringBuilder();
		sql.append("select member from v_room_member v where v.game_id=? order by point desc");
		for (UserActivityVo dmo : resultList) {
			jdbc.query(sql.toString(), new Object[] { dmo.getGameId() }, (rs, num) -> {
				Long memberId = rs.getLong("member");
				if (memberId.longValue() == dmo.getUserId().longValue()) {
					dmo.setRanking(num + 1);
				}
				return null;
			});
		}
		return resultList;
	}

	@Override
	public UserActivityVo findByMemberAndGame_Id(long member, int gameId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select member,nickname,game_id,game_name,point,count");
		sql.append(",avatar_signature from v_room_member v where v.game_id=? and v.member=?");
		UserActivityVo userActivityDmo = jdbc.queryForObject(sql.toString(), new Object[] { gameId, member },
				(rs, num) -> {
					UserActivityVo vo = new UserActivityVo(rs.getLong("member"), rs.getString("nickname"),
							rs.getInt("point"), 0, gameId, rs.getString("game_name"), rs.getString("avatar_signature"),
							rs.getInt("count"));
					return vo;
				});
		if (userActivityDmo == null) {
			return null;
		}
		sql = new StringBuilder();
		sql.append("select member from v_room_member where game_id=? order by point desc");
		jdbc.query(sql.toString(), new Object[] { gameId }, (rs, num) -> {
			long userId = rs.getLong("member");
			if (userId == member) {
				userActivityDmo.setRanking(num + 1);
			}
			return null;
		});
		return userActivityDmo;
	}

}
