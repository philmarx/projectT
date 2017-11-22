package com.yywl.projectT.bo;

import com.yywl.projectT.dmo.DeclarationEvaluationDmo;

public interface DeclarationEvaluationBo {

	DeclarationEvaluationDmo evaluate(long userId, String token, Long toUserId,long declaration, String content) throws Exception;

	void remove(long userId, String token, long declaration) throws Exception;

}
