package com.yywl.projectT.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bo.UserActivityBo;
import com.yywl.projectT.dao.GameScoreHistoryDao;
import com.yywl.projectT.dao.ImageDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.UserCircleDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.GameDmo;
import com.yywl.projectT.dmo.GameScoreHistoryDmo;
import com.yywl.projectT.dmo.ImageDmo;
import com.yywl.projectT.dmo.UserCircleDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.OrderCircleVo;
import com.yywl.projectT.vo.UserActivityVo;

@RequestMapping("order")
@RestController
public class OrderController {
	private final static Log log = LogFactory.getLog(OrderController.class);

	@Autowired
	UserActivityBo userActivityBo;

	@Autowired
	GameScoreHistoryDao gameScoreHistoryDao;

	@Autowired
	UserCircleDao userCircleDao;

	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	ImageDao imageDao;

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	UserDao userDao;

	@PostMapping("findGameRankingByUserId")
	public Callable<ResultModel> findGameRankingByUserId(long userId, int gameId) {
		return () -> {
			UserDmo user = this.userDao.findOne(userId);
			if (user == null) {
				log.error("用户不存在");
				throw new Exception("用户不存在");
			}
			GameDmo game = this.entityManager.find(GameDmo.class, gameId);
			if (game == null) {
				log.error("活动不存在");
				throw new Exception("活动不存在");
			}
			UserActivityVo vo = new UserActivityVo(userId, user.getNickname(), 1000, 0, gameId, game.getName(),
					user.getAvatarSignature(), 0);
			vo.setGender(user.getGender());
			Date maxDate = this.jdbcDao.findMaxDateFromGameScoreHistory();
			GameScoreHistoryDmo scoreDmo = this.gameScoreHistoryDao.findByUser_IdAndGame_IdAndCreateDate(userId, gameId,
					maxDate);
			if (scoreDmo != null) {
				vo.setRanking(scoreDmo.getRanking());
				vo.setCount(scoreDmo.getCount());
				vo.setPoint(scoreDmo.getScore());
				int point = vo.getPoint() < 1000 ? 1000 : vo.getPoint();
				vo.setPoint(point);
			}
			return new ResultModel(true, "", vo);
		};
	}

	@PostMapping("getOrderByUserId")
	public Callable<ResultModel> getOrderByUserId(long userId) {
		return () -> {
			UserDmo user = userDao.findOne(userId);
			if (user == null) {
				log.error("用户不存在");
				throw new Exception("用户不存在");
			}
			Map<String, Object> map = new HashMap<>();
			map.put("birthday", user.getBirthday() == null ? "" : Formatter.dateFormatter.format(user.getBirthday()));
			map.put("id", user.getId());
			map.put("nickname", user.getNickname());
			map.put("gender", user.getGender());
			map.put("labels", user.getLabels());
			map.put("avatarSignature", user.getAvatarSignature());
			map.put("isVip", jdbcDao.isVip(userId));
			ImageDmo imageDmo = imageDao.findByUserId(userId);
			imageDmo = imageDmo == null ? new ImageDmo() : imageDmo;
			map.put("imageSignatures", imageDmo);
			List<UserActivityVo> list = new LinkedList<>();
			Date maxDate = this.jdbcDao.findMaxDateFromGameScoreHistory();
			List<GameScoreHistoryDmo> scores = gameScoreHistoryDao.findByUser_IdAndGame_IsShowAndCreateDate(userId,
					true, maxDate);
			for (GameScoreHistoryDmo dmo : scores) {
				UserActivityVo vo = new UserActivityVo(dmo.getUser().getId(), dmo.getUser().getNickname(),
						dmo.getScore(), dmo.getRanking(), dmo.getGame().getId(), dmo.getGame().getName(),
						dmo.getUser().getAvatarSignature(), dmo.getCount());
				vo.setGender(user.getGender());
				int point = vo.getPoint() < 1000 ? 1000 : vo.getPoint();
				vo.setPoint(point);
				list.add(vo);
			}
			map.put("orders", list);
			List<UserCircleDmo> userCircleDmos = this.userCircleDao.findByUser_Id(userId);
			List<OrderCircleVo> circles = new LinkedList<>();
			if (!userCircleDmos.isEmpty()) {
				for (UserCircleDmo userCircleDmo : userCircleDmos) {
					circles.add(new OrderCircleVo(userCircleDmo.getCircle().getId(),
							userCircleDmo.getCircle().getName(), userCircleDmo.getExperience()));
				}
			}
			map.put("circles", circles);
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("getOrders")
	public Callable<ResultModel> getOrders(int gameId) {
		return () -> {
			List<UserActivityVo> list = new LinkedList<>();
			Date maxDate = this.jdbcDao.findMaxDateFromGameScoreHistory();
			Page<GameScoreHistoryDmo> gameScores = this.gameScoreHistoryDao.findByGame_IdAndCreateDate(gameId, maxDate,
					new PageRequest(0, Keys.ORDER_SIZE, Direction.DESC, "score"));
			for (GameScoreHistoryDmo dmo : gameScores) {
				UserActivityVo vo = new UserActivityVo(dmo.getUser().getId(), dmo.getUser().getNickname(),
						dmo.getScore(), dmo.getRanking(), dmo.getGame().getId(), dmo.getGame().getName(),
						dmo.getUser().getAvatarSignature(), dmo.getCount());
				vo.setPoint(vo.getPoint());
				list.add(vo);
			}
			return new ResultModel(true, "", list);
		};
	}

}
