package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.UserCircleDmo;

public interface UserCircleDao extends JpaRepository<UserCircleDmo, Long> {

	UserCircleDmo findByUser_IdAndCircle_Id(Long user, Long circle);

	Page<UserCircleDmo> findByUser_Id(Long id, Pageable pageable);

	List<UserCircleDmo> findByCircle_Id(long circleId);

	boolean existsByUser_IdAndCircle_Id(long userId, long circleId);

	long countByCircle_Id(Long id);

	List<UserCircleDmo> findByCircle_IdOrderByExperienceDesc(long circleId);

	List<UserCircleDmo> findByUser_Id(long userId);

	Page<UserCircleDmo> findByUser_IdNotAndCircle_Id(long userId, Long circleId, Pageable pageRequest);

	long countByUser_Id(long userId);

}
