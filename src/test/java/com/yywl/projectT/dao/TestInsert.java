package com.yywl.projectT.dao;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.ProjectTApplication;
import com.yywl.projectT.dmo.SpreadUserDmo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectTApplication.class)
@Rollback(value = false)
public class TestInsert {

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	SpreadUserDao spreadUserDao;
	
	@Test
	@Transactional(rollbackOn = Throwable.class)
	public void insert() {
		SpreadUserDmo s=this.spreadUserDao.findAll().get(0);
		for (long id = 1000L; id < 2000L; id++) {
			String sql="INSERT INTO spread_user (user_id, longitude1, latitude1, longitude2, latitude2) VALUES ( ?, ?,?, ?, ?)";
			jdbc.update(sql,id,s.getLongitude1(),s.getLongitude2(),s.getLatitude1(),s.getLatitude2());
		}
	}

}
