package com.yywl.projectT.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.WithdrawalsDmo;

public interface WithdrawalsDao extends JpaRepository<WithdrawalsDmo, Long> {

	List<WithdrawalsDmo> findByUser_IdAndStateOrderByCreateTimeDesc(long userId, int ordinal);

	long countByUser_IdAndImeiAndCreateTimeBetween(long userId, String imei, Date startTime, Date endTime);

	List<WithdrawalsDmo> findByUser_IdAndStateInOrderByCreateTimeDesc(long userId, Integer[] integers);

}
