package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.PropDmo;

public interface PropDao extends JpaRepository<PropDmo, Long> {

	PropDmo findByUser_Id(long userId);

}
