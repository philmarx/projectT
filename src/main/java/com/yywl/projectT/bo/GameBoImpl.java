package com.yywl.projectT.bo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.GameDao;
import com.yywl.projectT.dmo.GameDmo;

@Service
@Transactional(rollbackOn=Throwable.class)
public class GameBoImpl implements GameBo {

	@Autowired
	GameDao gameDao;

	@Override
	public GameDmo findTree(Integer id) {
		GameDmo gameDmo = this.gameDao.findOne(id);
		this.findChildren(gameDmo);
		return gameDmo;
	}

	private void findChildren(GameDmo gameDmo) {
		List<GameDmo> children = gameDao.findByParentId(gameDmo.getId());
		if (children == null || children.isEmpty()) {
			return;
		}
		gameDmo.setChildren(children);
		for (GameDmo gameDmo2 : children) {
			findChildren(gameDmo2);
		}
	}

	@Override
	@Cacheable(value="games")
	public List<GameDmo> findRoots() {
		return findTree(0).getChildren();
	}

	@Override
	public GameDmo findParent(Integer parentId) {
		return gameDao.findOne(parentId);
	}

}
