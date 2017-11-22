package com.yywl.projectT.web.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bo.DeclarationBo;
import com.yywl.projectT.bo.DeclarationEvaluationBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dao.DeclarationDao;
import com.yywl.projectT.dao.DeclarationEvaluationDao;
import com.yywl.projectT.dao.ImageDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dmo.DeclarationDmo;
import com.yywl.projectT.dmo.DeclarationEvaluationDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.DeclarationEvaluationVo;
import com.yywl.projectT.vo.DeclarationUserVo;
import com.yywl.projectT.vo.DeclarationVo;

@RequestMapping("declaration")
@RestController
public class DeclarationController {
	@PostMapping("declare")
	public Callable<ResultModel> declare(long userId, String token, String content, String city) {
		return () -> {
			declarationBo.declare(userId, token, content, city);
			return new ResultModel();
		};
	}

	@Autowired
	DeclarationBo declarationBo;

	@Autowired
	DeclarationEvaluationBo declarationEvaluationBo;

	@PostMapping("evaluate")
	public Callable<ResultModel> evaluate(long userId, String token, long declaration, Long toUserId, String content) {
		return () -> {
			DeclarationEvaluationDmo dmo = declarationEvaluationBo.evaluate(userId, token, toUserId, declaration,
					content);
			Map<String, Object> map = new HashMap<>();
			map.put("id", dmo.getId());
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("remove")
	public Callable<ResultModel> remove(long userId, String token, long id, int type) {
		return () -> {
			if (type == 1) {
				this.declarationBo.remove(userId, token, id);
			} else if (type == 2) {
				this.declarationEvaluationBo.remove(userId, token, id);
			} else {
				throw new Exception("类型不正确");
			}
			return new ResultModel();
		};
	}

	@Autowired
	ImageDao imageDao;

	@Autowired
	UserBo userBo;

	@Autowired
	DeclarationEvaluationDao declarationEvaluationDao;

	@Autowired
	JdbcDao jdbcDao;
	
	@PostMapping("findMyEvaluation")
	public Callable<ResultModel> findMyEvaluation(long userId, String token, int page, int size) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			List<Map<String,Object>> list=this.jdbcDao.findMyEvaluation(userId,ValidatorBean.page(page),ValidatorBean.size(size));
			return new ResultModel(true, "", list);
		};
	}

	@PostMapping("findMyDeclaration")
	public Callable<ResultModel> findMyDeclaration(long userId, String token, int page, int size) {
		return () -> {
			this.userBo.loginByToken(userId, token);
			Pageable pageable = new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC,
					"createTime");
			Page<DeclarationDmo> plist = this.declarationDao.findByDeclarer_Id(userId, pageable);
			List<DeclarationVo> vos = new LinkedList<>();
			List<DeclarationDmo> list = plist.getContent();
			for (DeclarationDmo dmo : list) {
				DeclarationVo vo = new DeclarationVo(dmo.getId(), dmo.getContent(),
						new LinkedList<DeclarationEvaluationVo>(), dmo.getCreateTime());
				UserDmo declarer = dmo.getDeclarer();
				if (declarer != null) {
					vo.setAvatarSignature(declarer.getAvatarSignature());
					vo.setDeclareNickname(declarer.getNickname());
					vo.setDeclareId(declarer.getId());
				}
				for (DeclarationEvaluationDmo edmo : dmo.getEvaluations()) {
					DeclarationUserVo sender = null, reciever = null;
					if (edmo.getSender() != null) {
						sender = new DeclarationUserVo(edmo.getSender().getId(), edmo.getSender().getNickname());
					}
					if (edmo.getReceiver() != null) {
						reciever = new DeclarationUserVo(edmo.getReceiver().getId(), edmo.getReceiver().getNickname());
					}
					DeclarationEvaluationVo dev = new DeclarationEvaluationVo(edmo.getId(), sender, reciever,
							edmo.getContent());
					vo.getEvaluations().add(dev);
				}
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findDeclaration")
	public Callable<ResultModel> findDeclaration(String city, int page, int size) {
		return () -> {
			Sort.Order order = new Sort.Order(Direction.DESC, "createTime");
			Sort sort = new Sort(order);
			Page<DeclarationDmo> plist = this.declarationDao.findByCityLike(city + "%",
					new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), sort));
			List<DeclarationDmo> list = plist.getContent();
			List<DeclarationVo> vos = new LinkedList<>();
			for (DeclarationDmo dmo : list) {
				DeclarationVo vo = new DeclarationVo(dmo.getId(), dmo.getContent(),
						new LinkedList<DeclarationEvaluationVo>(), dmo.getCreateTime());
				UserDmo declarer = dmo.getDeclarer();
				if (declarer != null) {
					vo.setAvatarSignature(declarer.getAvatarSignature());
					vo.setDeclareNickname(declarer.getNickname());
					vo.setDeclareId(declarer.getId());
				}
				for (DeclarationEvaluationDmo edmo : dmo.getEvaluations()) {
					DeclarationUserVo sender = null, reciever = null;
					if (edmo.getSender() != null) {
						sender = new DeclarationUserVo(edmo.getSender().getId(), edmo.getSender().getNickname());
					}
					if (edmo.getReceiver() != null) {
						reciever = new DeclarationUserVo(edmo.getReceiver().getId(), edmo.getReceiver().getNickname());
					}
					DeclarationEvaluationVo dev = new DeclarationEvaluationVo(edmo.getId(), sender, reciever,
							edmo.getContent());
					vo.getEvaluations().add(dev);
				}
				vos.add(vo);
			}
			return new ResultModel(true, "", vos);
		};
	}

	@PostMapping("findOne")
	public Callable<ResultModel> findOne(long id) {
		return () -> {
			DeclarationDmo dmo = this.declarationDao.findOne(id);
			if (dmo == null) {
				return new ResultModel();
			}
			DeclarationVo vo = new DeclarationVo(dmo.getId(), dmo.getContent(),
					new LinkedList<DeclarationEvaluationVo>(), dmo.getCreateTime());
			UserDmo declarer = dmo.getDeclarer();
			if (declarer != null) {
				vo.setAvatarSignature(declarer.getAvatarSignature());
				vo.setDeclareNickname(declarer.getNickname());
				vo.setDeclareId(declarer.getId());
			}
			for (DeclarationEvaluationDmo edmo : dmo.getEvaluations()) {
				DeclarationUserVo sender = null, reciever = null;
				if (edmo.getSender() != null) {
					sender = new DeclarationUserVo(edmo.getSender().getId(), edmo.getSender().getNickname());
				}
				if (edmo.getReceiver() != null) {
					reciever = new DeclarationUserVo(edmo.getReceiver().getId(), edmo.getReceiver().getNickname());
				}
				DeclarationEvaluationVo dev = new DeclarationEvaluationVo(edmo.getId(), sender, reciever,
						edmo.getContent());
				vo.getEvaluations().add(dev);
			}
			List<DeclarationVo> vos = new LinkedList<>();
			vos.add(vo);
			return new ResultModel(true, "", vos);
		};
	}

	@Autowired
	DeclarationDao declarationDao;

}
