package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.AugActivityPopularGirlDmo;

public interface AugActivityPopularGirlDao extends JpaRepository<AugActivityPopularGirlDmo, Long> {

	AugActivityPopularGirlDmo findByUser_Id(long userId);

}
