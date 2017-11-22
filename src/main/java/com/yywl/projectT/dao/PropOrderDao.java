package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.PropOrderDmo;

public interface PropOrderDao extends JpaRepository<PropOrderDmo, Long> {

	PropOrderDmo findByOutTradeNo(String outTradeNo);

}
