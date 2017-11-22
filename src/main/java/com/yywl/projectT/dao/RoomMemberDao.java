package com.yywl.projectT.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.RoomMemberDmo;

public interface RoomMemberDao extends JpaRepository<RoomMemberDmo, Long> {
	Page<RoomMemberDmo> findByMember_Id(Long memberId, Pageable pageable);

	List<RoomMemberDmo> findByRoom_Id(Long roomId);

	RoomMemberDmo findByMember_IdAndRoom_Id(Long memberId, Long roomId);
	
	boolean existsByRoom_IdAndResult(long roomId, int result);

	long countByRoom_Id(Long roomId);

	boolean existsByGame_IdAndMember_Id(int gameId, Long memberId);

	List<RoomMemberDmo> findByRoom_IdAndResult(long roomId, int result);

	List<RoomMemberDmo> findByRoom_IdAndMember_IdNot(Long roomId, Long userId);

	boolean existsByRoom_IdAndMember_Id(Long roomId, Long memberId);

	RoomMemberDmo findByRoom_IdAndMember_Id(Long roomId, Long userId);

	List<RoomMemberDmo> findByRoom_IdAndMember_IdNotAndReady(long roomId, Long userId, boolean b);

	List<RoomMemberDmo> findByRoom_IdAndReady(long roomId, boolean b);

	long countByMember_IdAndRoom_Id(Long id, long roomId);

	List<RoomMemberDmo> findByRoom_IdNotAndMember_IdAndReady(long roomId, long userId, boolean ready);

	List<RoomMemberDmo> findByRoom_IdAndMember_IdNotAndReadyAndMember_AmountGreaterThanEqual(Long roomId,
			Long userId, boolean ready, int roomMoney);


	List<RoomMemberDmo> findByMember_Id(long l);

	List<RoomMemberDmo> findByRoom_IdAndPointNot(long roomId, int point);

	List<RoomMemberDmo> findByRoom_BeginTimeBetweenAndRoom_State(Date start, Date end, int state);

	long countByRoom_IdAndReady(long roomId, boolean b);

	List<RoomMemberDmo> findByMember_IdAndReady(long userId, boolean b);

	boolean existsByRoom_IdAndIsEvaluated(Long id, boolean b);

	List<RoomMemberDmo> findByRoom_IdOrderByPointDesc(long roomId);

	List<RoomMemberDmo> findByMember_IdNot(Long id);

	List<RoomMemberDmo> findByMember_IdAndIsLockMoney(long userId, boolean b);

	long countByMember_IdAndRoom_StateIn(long userId, Integer[] is);


	long countByRoom_BeginTimeBetweenAndMember_IdAndAndRoom_StateIn(Date startTime, Date endTime, long userId,
			Integer[] states);

	List<RoomMemberDmo> findByMember_IdAndIsLockMoneyOrderByRoom_EndTimeDesc(long userId, boolean isLockMoney);

	List<RoomMemberDmo> findByRoom_IdAndMember_Gender(Long id, boolean gender);

	long countByMember_IdAndRoom_StateInAndIsSigned(long userId, Integer[] integers, boolean b);

	List<RoomMemberDmo> findByRoom_BeginTimeBetweenAndRoom_StateAndIsSigned(Date start, Date end, int ordinal,
			boolean isSigned);

	List<RoomMemberDmo> findByRoom_BeginTimeBetweenAndRoom_StateAndIsSigned(Date start, Date end, int ordinal,
			boolean isSigned, Sort sort);

	boolean existsByMember_Id(Long id);

	int countByRoom_IdAndIsSigned(Long id, boolean signed);

	long countByRoom_BeginTimeBetweenAndMember_IdAndAndRoom_StateInAndGame_IdNot(Date startTime, Date endTime,
			long userId, Integer[] integers, int gameId);

	long countByMember_IdAndRoom_StateInAndGame_IdNot(long userId, Integer[] integers, int gameId);

	List<RoomMemberDmo> findByMember_IdAndReadyAndRoom_Game_IdNot(long userId, boolean b, int i);


}
