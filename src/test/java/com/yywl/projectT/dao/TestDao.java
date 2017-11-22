package com.yywl.projectT.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.ProjectTApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectTApplication.class)
@Rollback(value = false)
public class TestDao {

	@Autowired
	JdbcDao jdbcDao;
	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	UserDao userDao;

	@Autowired
	PayOrderDao payOrderDao;

	@Test
	public void test() throws Exception {
		List<Map<String, Object>> list = jdbcDao.findMyEvaluation(10000000000L, 0, 10);
		for (Map<String, Object> map : list) {
			String str = "";
			for (Entry<String, Object> e : map.entrySet()) {
				str += "，" + e.getKey() + "：" + e.getValue() + "";
			}
			str=str.replaceFirst("，", "");
			System.out.println(str);
		}
	}
}
