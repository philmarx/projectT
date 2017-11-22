package com.yywl.projectT.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.RoomDmo;

public interface RoomDao extends JpaRepository<RoomDmo, Long> {

	Page<RoomDmo> findByManager_Id(Long userId, Pageable pageable);

	Page<RoomDmo> findByGame_Id(Integer gameId, Pageable pageable);

	List<RoomDmo> findByBeginTime(Date date);

	List<RoomDmo> findByBeginTimeAndState(Date date, int state);

	List<RoomDmo> findByEndTimeAndState(Date date, int ordinal);

	List<RoomDmo> findByBelongCircle_IdOrderByBeginTimeDesc(long circleId);

	Page<RoomDmo> findByBelongCircle_Id(long circleId, Pageable pageable);

	List<RoomDmo> findByBeginTimeBetweenAndState(Date currentTime, Date delayTime, int state);

	List<RoomDmo> findByEndTimeBetweenAndState(Date currentTime, Date delayTime, int state);

	long countByManager_IdAndBeginTimeBetween(long userId, Date date1, Date date2);

	Page<RoomDmo> findByBelongCircle_IdAndState(long circleId, int i,Pageable pageable);

	long countByBelongCircle_Id(Long id);

	List<RoomDmo> findByEvaluateTimeBetweenAndState(Date currentTime, Date delayTime, int state);

	List<RoomDmo> findByEndTimeBeforeAndState(Date delayTime, int ordinal);

	List<RoomDmo> findByEvaluateTimeBeforeAndState(Date delayTime, int ordinal);

	List<RoomDmo> findByBeginTimeBeforeAndState(Date delayTime, int ordinal);

	List<RoomDmo> findByState(int state, Sort sort);

	List<RoomDmo> findByBeginTimeBeforeAndStateLessThan(Date delayTime, int ordinal);

	boolean existsByManager_Id(Long id);

	boolean existsByManager_IdAndStateLessThan(Long id, int ordinal);

	long countByManager_IdAndBeginTimeBetweenAndGame_IdNot(long userId, Date date1, Date date2, int gameId);

	List<RoomDmo> findByBeginTimeBeforeAndStateAndGame_IdNot(Date delayTime, int ordinal, int i);

	List<RoomDmo> findByBeginTimeBeforeAndStateLessThanAndGame_IdNot(Date delayTime, int ordinal, int i);

	List<RoomDmo> findByEndTimeBeforeAndStateAndGame_IdNot(Date delayTime, int ordinal, int i);

	List<RoomDmo> findByEvaluateTimeBeforeAndStateAndGame_IdNot(Date delayTime, int ordinal, int i);

	Page<RoomDmo> findByManager_IdAndStateNot(Long id, int ordinal, Pageable pageRequest);

	List<RoomDmo> findByEndTimeBeforeAndStateNotAndGame_Id(Date date, int ordinal, int i);

	Page<RoomDmo> findByManager_IdAndStateNotAndGame_IdNot(Long id, int ordinal, int i, Pageable pageRequest);

	Page<RoomDmo> findByGame_IdAndStateNot(int gameId, int state, Pageable pageRequest);

}
