package com.yywl.projectT.bo;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.GameScoreDao;
import com.yywl.projectT.dao.GameScoreHistoryDao;
import com.yywl.projectT.dmo.GameDmo;
import com.yywl.projectT.dmo.GameScoreDmo;
import com.yywl.projectT.dmo.GameScoreHistoryDmo;

@Service
public class GameScoreBo {

	@Autowired
	GameScoreHistoryDao gameScoreHistoryDao;

	@Autowired
	GameScoreDao gameScoreDao;

	@Transactional(rollbackOn = Throwable.class)
	public void clear() {
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR)-49);
		this.gameScoreHistoryDao.deleteByCreateDateBefore(cal.getTime());
	}

	@Autowired
	GameBo gameBo;
	
	
	@Transactional(rollbackOn = Throwable.class)
	public void imports() {
		List<GameDmo> games=this.gameBo.findRoots();
		if (null==games||games.isEmpty()) {
			return;
		}
		List<GameScoreHistoryDmo> historys = new LinkedList<>();
		for (GameDmo parent : games) {
			if (parent.getChildren()==null||parent.getChildren().isEmpty()) {
				continue;
			}
			for (GameDmo gameDmo : parent.getChildren()) {
				if ((!gameDmo.isShow())||(!gameDmo.isScoring())) {
					continue;
				}
				List<GameScoreDmo> list=this.gameScoreDao.findByGame_Id(gameDmo.getId(), new Sort(Direction.DESC, "score"));
				for (int i=0;i<list.size();i++) {
					GameScoreDmo o=list.get(i);
					GameScoreHistoryDmo dmo=new GameScoreHistoryDmo(null, o.getUser(), o.getGame(), o.getScore(), new Date(), i+1,o.getCount());
					historys.add(dmo);
				}
			}
		}
		this.gameScoreHistoryDao.save(historys);
	}

}
