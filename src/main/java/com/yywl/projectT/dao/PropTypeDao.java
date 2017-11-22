package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.PropTypeDmo;

public interface PropTypeDao extends JpaRepository<PropTypeDmo, Integer> {

	PropTypeDmo findByUniqueId(int id);

}
