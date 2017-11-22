package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.ActivityDmo;

public interface ActivityDao extends JpaRepository<ActivityDmo, Long>{

	List<ActivityDmo> findByEnable(boolean enable, Sort sort);

}
