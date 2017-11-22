package com.yywl.projectT.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestEntityManager {

	@PersistenceContext
	EntityManager manager;

	@Test
	public void countUser() {
		TypedQuery<Long> query = manager.createQuery("select count(1) from UserDmo u", Long.class);
		System.out.println(query.getSingleResult());
	}

	@Test
	public void deleteCircleMsg() {
		int delete = manager.createQuery("delete from CircleMsgDmo cm left join fetch cm.circle c where c.id=1")
				.executeUpdate();
		System.out.println(delete);
	}


}
