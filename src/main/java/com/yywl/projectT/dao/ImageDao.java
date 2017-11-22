package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.ImageDmo;

public interface ImageDao extends JpaRepository<ImageDmo, Long> {

	ImageDmo findByUserId(long userId);

}
