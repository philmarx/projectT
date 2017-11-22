package com.yywl.projectT.bo;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.ActivityDao;
import com.yywl.projectT.dao.PropDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.SeptActivityHelpDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.ActivityDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.SeptActivityHelpDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
public class SeptActivityHelpBo {
	@Autowired
	SeptActivityHelpDao septActivityHelpDao;
	@Autowired
	UserDao userDao;
	@Autowired
	RoomDao roomDao;
	@Autowired
	PropDao propDao;
	@Autowired
	PropBo propBo;
	@Autowired
	UserBo userBo;
	@Autowired
	ActivityDao activityDao;

	@Transactional(rollbackOn = Exception.class)
	public void help(long userId, String token,long helperId, String movieName, String ipAddress) throws Exception {
		ActivityDmo activityDmo=this.activityDao.findOne(3L);
		Date now=new Date();
		if (now.before(activityDmo.getBeginTime())||now.after(activityDmo.getEndTime())) {
			throw new Exception("活动还未开始或已结束");
		}
		if (userId==helperId) {
			throw new Exception("自己不能给自己助力");
		}
		if (septActivityHelpDao.existsByUser_IdAndHelper_IdAndMovieName(userId,helperId,movieName)) {
			throw new Exception("已经助力过了");
		}
		UserDmo user=this.userBo.loginByToken(userId, token);
		UserDmo helper=this.userDao.findOne(helperId);
		if (null==helper) {
			throw new Exception("助力人不存在");
		}
		SeptActivityHelpDmo dmo=new SeptActivityHelpDmo(null, user, helper, ipAddress, movieName);
		this.septActivityHelpDao.save(dmo);
		int count=this.septActivityHelpDao.countByHelper_IdAndMovieName(helperId,movieName);
		PropDmo prop=this.propBo.findByUser_Id(helperId);
		if (count!=0&&count%30!=0) {
			return;
		}
		prop.setRemainMovieTicket(prop.getRemainMovieTicket()+1);
		this.propDao.save(prop);
	}
}
