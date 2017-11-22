package com.yywl.projectT.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.DeclarationEvaluationDmo;

public interface DeclarationEvaluationDao extends JpaRepository<DeclarationEvaluationDmo, Long>{

	Page<DeclarationEvaluationDmo> findByReceiver_Id(long userId, Pageable pageable);

}
