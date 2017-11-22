package com.yywl.projectT.bo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dmo.TransactionDetailsDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
@Transactional(rollbackOn=Throwable.class)
public class TransactionDetailsBoImpl implements TransactionDetailsBo {

	@Autowired
	UserBo userBo;

	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Override
	public List<TransactionDetailsDmo> findByUserId(long userId, String token, int page, int size) throws Exception {
		UserDmo user = this.userBo.loginByToken(userId, token);
		Page<TransactionDetailsDmo> list = transactionDetailsDao.findByUser_Id(user.getId(),
				new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC, "createTime"));
		return list.getContent();
	}

}
