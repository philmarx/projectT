package com.yywl.projectT.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.GameScoreHistoryDmo;

public interface GameScoreHistoryDao extends JpaRepository<GameScoreHistoryDmo, Long> {

	void deleteByCreateDateBefore(Date date);

	boolean existsByCreateDate(Date date);

	long countByCreateDate(Date date);

	GameScoreHistoryDmo findByUser_IdAndGame_IdAndCreateDate(Long userId, Integer gameId, Date thisSunday);

	Page<GameScoreHistoryDmo> findByGame_Id(int gameId, Pageable pageRequest);

	List<GameScoreHistoryDmo> findByUser_IdAndGame_IsShow(long userId, boolean isShow);

	GameScoreHistoryDmo findByUser_IdAndGame_Id(long userId, int gameId);

	List<GameScoreHistoryDmo> findByUser_IdAndGame_IsShowAndCreateDate(long userId, boolean b, Date thisMonday);

	Page<GameScoreHistoryDmo> findByGame_IdAndCreateDate(int gameId, Date thisMonday, Pageable pageRequest);

}
