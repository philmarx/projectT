package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.SmsCodeDmo;

public interface SmsCodeDao extends JpaRepository<SmsCodeDmo, Long> {
	SmsCodeDmo findByPhone(String phone);
}
