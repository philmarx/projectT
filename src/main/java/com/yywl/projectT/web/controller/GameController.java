package com.yywl.projectT.web.controller;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bo.GameBo;
import com.yywl.projectT.dao.ApplicationDao;
import com.yywl.projectT.dao.GameDao;
import com.yywl.projectT.dmo.ApplicationDmo;

@RestController
@RequestMapping("game")
public class GameController {

	@Autowired
	GameBo gameBo;

	private static final Log log=LogFactory.getLog(GameController.class);
	/**
	 * 查看分类
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "list", method = RequestMethod.POST)
	public Callable<ResultModel> gameList(String secret) throws Exception {
		return () -> {
			if ("tomeet".equals(secret) ) {
				List<ApplicationDmo> apps=this.applicationDao.findByPlatformAndIsCurrent("gameType", true);
				String version=apps.isEmpty()?"1":apps.get(0).getVersion();
				return new ResultModel(true, version, gameBo.findRoots());
			}
			log.error("参数不正确");
			throw new Exception("参数不正确");
		};
	}

	@Autowired
	ApplicationDao applicationDao;

	@Autowired
	GameDao gameDao;

	/**
	 * 获取排行榜所展示的分类
	 */
	@PostMapping("findOrderGames")
	public Callable<ResultModel> findOrderGames(String secret) throws Exception {
		return () -> {
			if ("tomeet".equals(secret)) {
				List<ApplicationDmo> apps=this.applicationDao.findByPlatformAndIsCurrent("gameType", true);
				String version=apps.isEmpty()?"1":apps.get(0).getVersion();
				return new ResultModel(true, version, gameDao.findByIsScoring(true));
			}
			log.error("参数不正确");
			throw new Exception("参数不正确");
		};
	}
}
