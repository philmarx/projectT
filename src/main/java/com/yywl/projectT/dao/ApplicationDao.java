package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.ApplicationDmo;

public interface ApplicationDao extends JpaRepository<ApplicationDmo, Integer> {
	List<ApplicationDmo> findApplicationDmoByIsCurrent(Boolean isCurrent);

	List<ApplicationDmo> findByPlatformAndIsCurrent(String platform, boolean isCurrent);

	ApplicationDmo findByPlatform(String string);
}
