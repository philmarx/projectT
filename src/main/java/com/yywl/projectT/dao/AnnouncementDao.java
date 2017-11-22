package com.yywl.projectT.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.AnnouncementDmo;

public interface AnnouncementDao extends JpaRepository<AnnouncementDmo, Integer> {

	List<AnnouncementDmo> findByIsEffective(boolean isEffective);

	List<AnnouncementDmo> findByIsEffectiveAndExpiryDateGreaterThanEqual(boolean b, Date date);

}
