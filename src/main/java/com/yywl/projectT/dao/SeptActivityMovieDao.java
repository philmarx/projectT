package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.SeptActivityMovieDmo;

public interface SeptActivityMovieDao extends JpaRepository<SeptActivityMovieDmo, Integer>{

	List<SeptActivityMovieDmo> findByIsEffective(boolean b);

	List<SeptActivityMovieDmo> findByIdIn(List<Integer> votes);

}
