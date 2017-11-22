package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.ErrorMessageDmo;

public interface ErrorMessageDao extends JpaRepository<ErrorMessageDmo, Long>{

}
