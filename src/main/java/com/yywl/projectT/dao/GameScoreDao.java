package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.GameScoreDmo;

public interface GameScoreDao extends JpaRepository<GameScoreDmo, Long> {

	boolean existsByUser_IdAndGame_Id(Long userId, Integer gameId);

	GameScoreDmo findByUser_IdAndGame_Id(Long memberId, Integer gameId);

	List<GameScoreDmo> findByUser_Id(long userId);

	Page<GameScoreDmo> findByGame_Id(int gameId, Pageable pageRequest);

	List<GameScoreDmo> findByUser_IdAndGame_IsShow(long userId, boolean show);

	List<GameScoreDmo> findByGame_Id(Integer id, Sort sort);

}
