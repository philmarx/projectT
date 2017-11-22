package com.yywl.projectT.bo;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class SmsCodeBoImpl implements SmsCodeBo {


	@Override
	public String getSmsCode() {
		String code = 100000+(new Random(System.currentTimeMillis()).nextInt(900000)) + "";
		return code;
	}

}
