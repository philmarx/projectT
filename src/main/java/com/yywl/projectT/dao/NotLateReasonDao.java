package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.NotLateReasonDmo;

public interface NotLateReasonDao extends JpaRepository<NotLateReasonDmo, Long> {

	NotLateReasonDmo findByUser_IdAndRoom_Id(long userId, long roomId);

}
