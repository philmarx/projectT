package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.SuggestionDmo;

public interface SuggestionDao extends JpaRepository<SuggestionDmo, Long> {

}
