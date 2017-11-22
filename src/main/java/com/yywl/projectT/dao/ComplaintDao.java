package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.ComplaintDmo;

public interface ComplaintDao extends JpaRepository<ComplaintDmo, Long> {

	List<ComplaintDmo> findByRoom_Id(Long id);

}
