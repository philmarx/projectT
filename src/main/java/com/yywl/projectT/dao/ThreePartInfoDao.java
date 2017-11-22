package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.ThreePartInfoDmo;

public interface ThreePartInfoDao extends JpaRepository<ThreePartInfoDmo, Long> {

	List<ThreePartInfoDmo> findByUserIdAndType(long userId, String type);

}
