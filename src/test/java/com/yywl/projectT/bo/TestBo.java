package com.yywl.projectT.bo;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.ProjectTApplication;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.dao.GameScoreHistoryDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.GameDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.RoomMemberDmo;
import com.yywl.projectT.dmo.UserDmo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectTApplication.class)
@Rollback(false)
public class TestBo {

	private final static Log log = LogFactory.getLog(TestBo.class);

	@Autowired
	GameScoreBo gameScoreBo;

	@Autowired
	RoomDao roomDao;

	@Autowired
	GameScoreHistoryDao gameScoreHistoryDao;

	@Autowired
	FriendBo friendBo;

	@Autowired
	GameBo gameBo;

	@Autowired
	RoomBo roomBo;

	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;
	@Autowired
	MoneyTransactionBo moneyTransactionBo;
	@Autowired
	RoomMemberDao roomMemberDao;

	@Test
	public void sendLocation() {
		try {
			this.roomBo.sendLocationV2(10000000004L,
					"783aKOODcvgvJ3UitRFcADMcmI9C4tVj1kvfRptGkzdk5UozJRZ4xm+yqvX1WbTvz0OylDDtK+0+WB/M+IGBWGjEJ0QR+VDP",
					-1L, 0, 0, "默认", "12345", "192.168.1.195");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void countByJoinCount() {
		long count = this.roomMemberDao.countByMember_IdAndRoom_StateIn(10000000004L,
				new Integer[] { ActivityStates.新建.ordinal(), ActivityStates.准备中.ordinal() });
		System.out.println(count);
	}

	@Test
	@Transactional(rollbackOn = Throwable.class)
	public void testValidateMoney() {
		UserDmo user = this.userDao.findOne(10000000004L);
		try {
			this.moneyTransactionBo.validateMoney(user);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		user.setAmount(16);
		this.userDao.save(user);
	}

	@Test
	public void quit() {
		try {
			this.roomBo.quit(10000000003L,
					"VlbdyK8E4Oo2lqczMMYqAzMcmI9C4tVj1kvfRptGkzfMmMSP7OxBN8uPtySJBSwKaCcReFZiAus+WB/M+IGBWGfo4f0QllKN",
					1000000000052L);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void deleteRoomMember() {
		RoomMemberDmo roomMemberDmo = roomMemberDao.findByMember_IdAndRoom_Id(10000000003L, 1000000000052L);
		try {
			this.moneyTransactionBo.roomMemberQuit(roomMemberDmo);
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void belongCircleIsNull() {
		RoomDmo room = this.roomDao.findOne(1000000000003L);
		System.out.println(room.getName());
		System.out.println(room.getBelongCircle());
		if (room.getBelongCircle() != null) {
			System.out.println(room.getBelongCircle().getId());
		}
	}

	@Test
	public void importsGameScoreHistory() {
		this.gameScoreBo.clear();
		this.gameScoreBo.imports();
	}

	@Test
	public void testFindGame() {
		GameDmo gameDmo = gameBo.findTree(0);
		System.out.println(gameDmo.getName());
		for (GameDmo game : gameDmo.getChildren()) {
			System.out.println(game.getName() + ":");
			for (GameDmo g : game.getChildren()) {
				System.out.println(g.getName());
			}
			System.out.println();
		}
		System.out.println();
	}
}
