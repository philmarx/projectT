package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.RoleDmo;

public interface RoleDao extends JpaRepository<RoleDmo, Integer> {

}
