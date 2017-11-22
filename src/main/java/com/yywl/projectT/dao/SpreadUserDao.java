package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.SpreadUserDmo;

public interface SpreadUserDao extends JpaRepository<SpreadUserDmo, Long> {

	SpreadUserDmo findByUserId(Long recommenderId);

	int countByUserId(Long id);

	boolean existsByUserId(long recommenderId);

}
