package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.SeptActivityHelpDmo;

public interface SeptActivityHelpDao extends JpaRepository<SeptActivityHelpDmo, Long>{

	boolean existsByUser_IdAndHelper_IdAndMovieName(long userId, long helperId, String movieName);

	int countByHelper_Id(long helperId);

	List<SeptActivityHelpDmo> findByHelper_IdAndMovieName(long userId, String movieName);

	int countByHelper_IdAndMovieName(long helperId, String movieName);

}
