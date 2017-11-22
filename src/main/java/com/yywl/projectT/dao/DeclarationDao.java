package com.yywl.projectT.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.DeclarationDmo;

public interface DeclarationDao extends JpaRepository<DeclarationDmo, Long> {

	Page<DeclarationDmo> findByCityLike(String city, Pageable pageable);

	Page<DeclarationDmo> findByDeclarer_Id(long userId, Pageable pageable);

}
