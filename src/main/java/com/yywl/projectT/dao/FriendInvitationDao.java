package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.FriendInvitationDmo;

public interface FriendInvitationDao extends JpaRepository<FriendInvitationDmo, Long> {

	Page<FriendInvitationDmo> findByFriend_Id(long userId, Pageable pageRequest);

	List<FriendInvitationDmo> findByFriend_IdOrderByCreateTimeDesc(long userId);

	FriendInvitationDmo findByOwner_IdAndFriend_Id(long userId, long friendId);

	boolean existsByOwner_IdAndFriend_Id(long senderId, long receiverId);

}
