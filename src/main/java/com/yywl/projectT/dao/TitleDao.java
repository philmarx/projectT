package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.TitleDmo;

public interface TitleDao extends JpaRepository<TitleDmo, Long> {

}
