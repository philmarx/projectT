package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.PayOrderDmo;

public interface PayOrderDao extends JpaRepository<PayOrderDmo, Long> {

	boolean existsByOutTradeNo(String out_trade_no);

	Page<PayOrderDmo> findByOutTradeNoLike(String out_trade_no, Pageable pageable);

	List<PayOrderDmo> findByOutTradeNoLike(String string);

	Page<PayOrderDmo> findByOutTradeNoLikeAndTotalAmountNot(String out_trade_no, int totalAmount, Pageable pageable);

}
