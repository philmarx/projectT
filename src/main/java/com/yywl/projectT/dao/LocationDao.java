package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.LocationDmo;

public interface LocationDao extends JpaRepository<LocationDmo, Long> {

	List<LocationDmo> findByRoom_Id(Long id);

	List<LocationDmo> findByUser_Id(Long id);

}
