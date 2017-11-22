package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.BadgeDetailsDmo;

public interface BadgeDetailsDao extends JpaRepository<BadgeDetailsDmo, Long> {

	boolean existsByOutTradeNo(String outTradeNo);

	Page<BadgeDetailsDmo> findByUser_Id(long userId, Pageable pageRequest);

	List<BadgeDetailsDmo> findByUser_Id(Long userId);

}
