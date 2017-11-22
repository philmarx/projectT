package com.yywl.projectT;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bo.RoomBo;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.UserDmo;

@RunWith(SpringRunner.class)
@SpringBootTest
@Rollback(false)
public class TestThread {
	@Autowired
	RoomBo roomBo;
	@Autowired
	UserDao userDao;

	private final static Log log = LogFactory.getLog(TestThread.class);

	@Test
	public void quit() {
		try {
			roomBo.quit(10000000000L,
					"c+ksSZQxXi8vJ3UitRFcADMcmI9C4tVj1kvfRptGkzepkW8U5j7Ldqx8KYRZ/R7izk205F27xsw+WB/M+IGBWPPAm8LdLHvu",
					1000000000232L);
			roomBo.quit(10000000004L,
					"uv3ExwpFy8lszOHbZqFAxTMcmI9C4tVj1kvfRptGkzepkW8U5j7Ldm7Sys5dDEZdzk205F27xsw+WB/M+IGBWGjEJ0QR+VDP",
					1000000000232L);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	@Autowired
	RoomMemberDao roomMemberDao;

	@Test
	public void join() throws InterruptedException {
		UserDmo user1 = userDao.findOne(10000000000L);
		Long roomId = 1000000000232L;
		System.out.println(roomId);
		Thread t=new Thread(()->{
			user1.setNickname("张三李四");
			this.userDao.save(user1);
		});
		t.start();
		
	}

	
	
	@Test
	public void test() {
		UserDmo user1 = userDao.findOne(10000000000L);
		UserDmo user2 = userDao.findOne(10000000004L);
		Long roomId = 1000000000232L;
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 1000; i++) {
					try {
						roomBo.join(user1, roomId, Keys.ROOM_KEY);
						roomBo.leave(10000000000L,
								"c+ksSZQxXi8vJ3UitRFcADMcmI9C4tVj1kvfRptGkzepkW8U5j7Ldqx8KYRZ/R7izk205F27xsw+WB/M+IGBWPPAm8LdLHvu",
								roomId);
					} catch (Exception e) {
						log.error("thread1-" + e.getMessage());
					}
				}
			}
		}.start();
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 1000; i++) {
					try {
						roomBo.join(user2, roomId, Keys.ROOM_KEY);
						roomBo.leave(100000000004L,
								"uv3ExwpFy8lszOHbZqFAxTMcmI9C4tVj1kvfRptGkzepkW8U5j7Ldm7Sys5dDEZdzk205F27xsw+WB/M+IGBWGjEJ0QR+VDP",
								roomId);
					} catch (Exception e) {
						log.error("thread2-" + e.getMessage());
					}
				}
			}
		}.start();

	}

}
