package com.yywl.projectT.bo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.RoleDao;
import com.yywl.projectT.dmo.RoleDmo;
@Service
public class RoleBoImpl implements RoleBo{

	@Autowired
	RoleDao roleDao;
	
	@Override
	@Cacheable(value="role",key="#roleId")
	public RoleDmo findOne(Integer roleId) {
		return roleDao.findOne(roleId);
	}

}
