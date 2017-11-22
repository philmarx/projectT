package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.CircleDmo;

public interface CircleDao extends JpaRepository<CircleDmo, Long> {

	Page<CircleDmo> findByNameLike(String string, Pageable pageRequest);

	boolean existsByName(String name);

	List<CircleDmo> findByManager_Id(Long userId);

	long countByManager_Id(long userId);

	boolean existsByManager_Id(Long id);

}
