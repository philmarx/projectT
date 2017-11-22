package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.AugActivityLuckDrawDmo;

public interface AugActivityLuckDrawDao extends JpaRepository<AugActivityLuckDrawDmo, Long> {

	long countByUser_Id(Long id);

	List<AugActivityLuckDrawDmo> findByUser_Id(long userId, Sort sort);

	List<AugActivityLuckDrawDmo> findByUser_Id(Long id);

}
