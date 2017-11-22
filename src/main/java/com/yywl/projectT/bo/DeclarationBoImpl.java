package com.yywl.projectT.bo;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yywl.projectT.bean.ActivityDateBean;
import com.yywl.projectT.dao.BadgeDetailsDao;
import com.yywl.projectT.dao.DeclarationDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.DeclarationDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
public class DeclarationBoImpl implements DeclarationBo {

	@Autowired
	DeclarationDao declarationDao;

	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;

	@Autowired
	BadgeDetailsDao badgeDetailsDao;

	@Autowired
	BadgeBo badgeBo;

	private final static Log log = LogFactory.getLog(DeclarationBoImpl.class);

	@Autowired
	JdbcTemplate jdbc;
	
	@Override
	@Transactional(rollbackOn=Exception.class)
	public void remove(long userId, String token, long id) throws Exception {
		this.userBo.loginByToken(userId, token);
		DeclarationDmo dmo=this.declarationDao.findOne(id);
		if (dmo==null) {
			log.error(id);
			throw new Exception("id不存在");
		}
		if (dmo.getDeclarer().getId().longValue()==userId) {
			jdbc.update("delete from declaration_evaluation where declaration_id=?",id);
			this.declarationDao.delete(dmo);
		}else {
			throw new Exception("您没有删除权限");
		}
	}

	@Override
	public void declare(long userId, String token, String content, String city) throws Exception {
		if (StringUtils.isEmpty(content)) {
			log.error("喊话内容不能为空");
			throw new Exception("喊话内容不能为空");
		}
		UserDmo user = userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			log.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		Date now=new Date();
		if (now.before(ActivityDateBean.start())||now.after(ActivityDateBean.end())) {
			this.badgeBo.declare(user);
		}
		DeclarationDmo declarationDmo = new DeclarationDmo();
		declarationDmo.setCity(city);
		declarationDmo.setContent(content);
		declarationDmo.setCreateTime(new Date());
		declarationDmo.setDeclarer(user);
		this.declarationDao.save(declarationDmo);
	}

}
