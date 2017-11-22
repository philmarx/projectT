package com.yywl.projectT.bo;

public interface DeclarationBo {

	void declare(long userId, String token, String content,String city) throws Exception;

	void remove(long userId, String token, long id) throws Exception;

}
