package com.yywl.projectT.bo;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yywl.projectT.dao.DeclarationDao;
import com.yywl.projectT.dao.DeclarationEvaluationDao;
import com.yywl.projectT.dmo.DeclarationDmo;
import com.yywl.projectT.dmo.DeclarationEvaluationDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
public class DeclarationEvaluationBoImpl implements DeclarationEvaluationBo {

	@PersistenceContext
	EntityManager manager;
	@Autowired
	DeclarationDao declarationDao;
	@Autowired
	DeclarationEvaluationDao declarationEvaluationDao;
	@Autowired
	UserBo userBo;

	private static final Log log=LogFactory.getLog(DeclarationBoImpl.class);
	
	@Override
	public DeclarationEvaluationDmo evaluate(long userId, String token, Long toUserId, long declaration, String content) throws Exception {
		if (StringUtils.isEmpty(content)) {
			log.error("评论内容不能为空");
			throw new Exception("评论内容不能为空");
		}
		DeclarationDmo declarationDmo = declarationDao.findOne(declaration);
		if (declarationDmo == null) {
			log.error("喊话不存在");
			throw new Exception("喊话不存在");
		}
		UserDmo sender = userBo.loginByToken(userId, token);
		if (!sender.getIsInit()) {
			log.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		DeclarationEvaluationDmo declarationEvaluationDmo = new DeclarationEvaluationDmo();
		if (toUserId!=null&&toUserId!=0) {
			UserDmo receiver = manager.find(UserDmo.class, toUserId);
			if (receiver == null) {
				log.error("评论者不存在");
				throw new Exception("评论者不存在");
			}
			declarationEvaluationDmo.setReceiver(receiver);
		}
		declarationEvaluationDmo.setContent(content);
		declarationEvaluationDmo.setSender(sender);
		declarationEvaluationDmo.setDeclaration(declarationDmo);
		declarationEvaluationDmo.setCreateTime(new Date());
		declarationEvaluationDao.save(declarationEvaluationDmo);
		return declarationEvaluationDmo;
	}

	@Override
	@Transactional(rollbackOn=Exception.class)
	public void remove(long userId, String token, long id) throws Exception {
		this.userBo.loginByToken(userId, token);
		DeclarationEvaluationDmo dmo=this.declarationEvaluationDao.findOne(id);
		if (dmo==null) {
			log.error(id);
			throw new Exception("id不存在");
		}
		if (dmo.getSender().getId().longValue()==userId) {
			this.declarationEvaluationDao.delete(dmo);
		}else {
			throw new Exception("您没有删除权限");
		}		
	}

}
