package com.yywl.projectT.bo;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.AugActivityPopularGirlDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.AugActivityPopularGirlDmo;

@Service
@Transactional(rollbackOn=Throwable.class)
public class AugActivityPopularGirlBo {

	@Autowired
	AugActivityPopularGirlDao popularGirlDao;
	
	@Autowired
	UserDao userDao;
	
	private static final Log log=LogFactory.getLog(AugActivityPopularGirlBo.class);
	
	public AugActivityPopularGirlDmo findPopularGirlByUserId(long userId) throws Exception{
		AugActivityPopularGirlDmo girl=this.popularGirlDao.findByUser_Id(userId);
		if (girl==null) {
			girl=new AugActivityPopularGirlDmo(null, userDao.findOne(userId),0);
			if (girl.getUser()==null) {
				log.error("用户不存在");
				throw new Exception("用户不存在");
			}
			Boolean gender=girl.getUser().getGender();
			if (gender==null||gender) {
				log.error("性别不正确");
				throw new Exception("性别不正确");
			}
			this.popularGirlDao.save(girl);
		}
		return girl;
	}
}
