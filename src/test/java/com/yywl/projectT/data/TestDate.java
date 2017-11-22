package com.yywl.projectT.data;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Rollback(false)
@Transactional(rollbackOn = Throwable.class)
public class TestDate {
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	public void callProcedure() {
		try {
			Connection conn= this.jdbcTemplate.getDataSource().getConnection();
			CallableStatement cs=conn.prepareCall("{call proc_demo1(?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.execute();
			Integer count=cs.getInt(1);
			System.out.println(count);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
