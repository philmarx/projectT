package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.GameDmo;

public interface GameDao extends JpaRepository<GameDmo, Integer> {

	List<GameDmo> findByParentId(Integer parentId);

	List<GameDmo> findByParentIdIn(int[] parentIds);

	List<GameDmo> findByIsScoring(boolean isScoring);

}
