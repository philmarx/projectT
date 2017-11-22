package com.yywl.projectT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.bean.component.ScheduledBean;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestScheduled {
	@Autowired
	ScheduledBean bean;
	
	@Test
	public void clearPropOrder() {
		bean.clearPropOrder();
	}
	
	@Test
	public void clearRoom() {
		bean.clearRoom();
	}
	
	@Test
	public void clearSignCircle() {
		bean.clearSignCircle();
	}
	
	@Test
	public void evaluate() {
		bean.evaluate();
	}
	
	@Test
	public void joinChatRoom() {
		bean.joinChatRoom();
	}
	
	@Test
	public void push() {
		bean.push();
	}
	
	@Test
	public void roomBegin() {
		bean.roomBegin();
	}
	
	@Test
	public void saveScoreHistory() {
		bean.saveScoreHistory();
	}
	
	@Test
	public void setCircleManager() {
		bean.setCircleManager();
	}
	
	@Test
	public void roomEnd() {
		bean.roomEnd();
	}
}
