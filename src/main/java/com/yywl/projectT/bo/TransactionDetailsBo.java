package com.yywl.projectT.bo;

import java.util.List;

import com.yywl.projectT.dmo.TransactionDetailsDmo;

public interface TransactionDetailsBo {

	List<TransactionDetailsDmo> findByUserId(long userId, String token, int page, int size) throws Exception;

}
