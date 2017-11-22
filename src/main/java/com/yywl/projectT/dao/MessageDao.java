package com.yywl.projectT.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.MessageDmo;

public interface MessageDao extends JpaRepository<MessageDmo, Long> {

	List<MessageDmo> findBySender_IdAndReceiver_IdAndCreateTimeBetween(long sender, long receiver, Date startTime,
			Date endTime);

	long countBySender_IdAndReceiver_IdAndCreateTimeBetween(long sender, long receiver, Date startTime, Date endTime);

}
