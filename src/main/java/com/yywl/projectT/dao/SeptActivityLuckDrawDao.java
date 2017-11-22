package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.SeptActivityLuckDrawDmo;

public interface SeptActivityLuckDrawDao extends JpaRepository<SeptActivityLuckDrawDmo, Long> {

	Long countByUser_Id(long userId);

	List<SeptActivityLuckDrawDmo> findByUser_Id(long userId, Sort sort);

}
