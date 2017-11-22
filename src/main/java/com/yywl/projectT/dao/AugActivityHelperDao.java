package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.AugActivityHelperDmo;

public interface AugActivityHelperDao extends JpaRepository<AugActivityHelperDmo, Long> {

	long countByUser_Id(Long id);

	/**
	 * 查看被userId助力的对象
	 * @param userId
	 * @return
	 */
	List<AugActivityHelperDmo> findByHelper_Id(long userId);

	/**
	 * @param userId
	 * @param token
	 * @return
	 */
	boolean existsByUser_IdAndHelper_Id(long userId, long helperId);

	long countByHelper_Id(long userId);

}
