package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.AdminDmo;

public interface AdminDao extends JpaRepository<AdminDmo, Long> {

	AdminDmo findByUsernameAndPassword(String username, String password);

}
