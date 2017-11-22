package com.yywl.projectT.bo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.AdminDao;

@Service
public class AdminBo {
	@Autowired
	AdminDao adminDao;
	
	
}
