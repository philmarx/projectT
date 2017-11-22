package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.RoomEvaluationDmo;

public interface RoomEvaluationDao extends JpaRepository<RoomEvaluationDmo, Long> {

	RoomEvaluationDmo findByRoomIdAndOwnerIdAndOtherId(long roomId, Long ownerId, long friendId);

	List<RoomEvaluationDmo> findByRoomIdOrderByOwnerId(Long id);

	boolean existsByOwnerIdAndRoomId(Long userId, Long roomId);

	long countByOwnerIdAndOtherIdIn(Long id, List<Long> memberIds);

	boolean existsByOwnerIdAndOtherId(Long ownerId, Long otherId);

}
