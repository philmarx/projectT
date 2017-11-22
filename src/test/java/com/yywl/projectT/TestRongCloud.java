package com.yywl.projectT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.dao.ActivityDao;

import io.rong.RongCloud;
import io.rong.messages.BaseMessage;
import io.rong.messages.CmdMsgMessage;
import io.rong.messages.InfoNtfMessage;
import io.rong.messages.TxtMessage;
import io.rong.methods.Message;
import io.rong.models.ChatRoom;
import io.rong.models.ChatRoomUser;
import io.rong.models.ChatroomQueryReslut;
import io.rong.models.ChatroomUserQueryReslut;
import io.rong.models.CodeSuccessReslut;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectTApplication.class)
public class TestRongCloud {
	@Autowired
	RongCloudBean rongCloud;

	@Test
	public void sendCmdMessage() {
		try {
			rongCloud.sendMessageToChatRoom(Keys.RONGCLOUD_SYSTEM_ID, 1000000000899L,
					new CmdMsgMessage("outMan", "" + 123456));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void destoryChatRoom() {
		try {
			rongCloud.destoryChatRoom("cmd111111");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void createGroup() {
		try {
			rongCloud.createGroup("10000000000", "tomeet", "tomeet");
			rongCloud.joinGroup("10000000004", "tomeet", "tomeet");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void sendGroup() {
		try {
			rongCloud.sendGroup("10000000004", "tomeet", new TxtMessage("hello", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void destoryGroup() {
		try {
			rongCloud.destoryGroup("10000000000", "tomeet");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getToken() {
		try {
			System.out.println("getToken:  " + rongCloud.getToken(2 + "", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void sendMessage() throws Exception {
		RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);
		String[] messagePublishPrivateToUserId = { "10000000004", "userid4", "userId5" };
		BaseMessage msg = new TxtMessage("hello", null);
		CodeSuccessReslut messagePublishPrivateResult = rongCloud.message.publishPrivate("2",
				messagePublishPrivateToUserId, msg, "thisisapush", "{\"pushData\":\"hello\"}", "4", 0, 0, 0, 0);
		System.out.println("publishPrivate:  " + messagePublishPrivateResult.getCode());
	}

	/**
	 * 向房间发送消息
	 * 
	 * @throws Exception
	 */
	@Test
	public void sendRoom() throws Exception {
		RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);
		BaseMessage message = new InfoNtfMessage("发送消息测试", null);
		CodeSuccessReslut result = rongCloud.message.publishChatroom("10000000021", new String[] { "56" }, message);
		System.out.println(result.getCode());
	}

	/**
	 * 销毁房间
	 */
	@Test
	public void destoryRoom() {
		RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);
		try {
			CodeSuccessReslut result = rongCloud.chatroom.destroy(new String[] { "11" });
			System.out.println(result.getCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询房间
	 * 
	 * @throws Exception
	 */
	@Test
	public void queryRoom() throws Exception {
		RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);
		ChatroomQueryReslut result = rongCloud.chatroom.query(new String[] { "11" });
		List<ChatRoom> chatRooms = result.getChatRooms();
		for (ChatRoom chatRoom : chatRooms) {
			System.out.println(chatRoom.getChrmId() + ":" + chatRoom.getName());
		}
	}

	/**
	 * 查看房间里的人数
	 * 
	 * @throws Exception
	 */
	@Test
	public void queryRoomUsers() throws Exception {
		RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);
		ChatroomUserQueryReslut result = rongCloud.chatroom.queryUser("11", "100", "1");
		List<ChatRoomUser> chatRoomUsers = result.getUsers();
		for (ChatRoomUser user : chatRoomUsers) {
			System.out.println(user.getId() + ":" + user.getTime());
		}
	}

	@Test
	public void pushData() throws Exception {
		RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);
		rongCloud.message.PublishSystem(Keys.RONGCLOUD_SYSTEM_ID + "", new String[] { "10000000023" },
				new InfoNtfMessage("msg", "{\"type\":\"type\",\"id\":\"" + "10000000023" + "\"}"), "", "", 1, 1);
	}

	@Test
	public void refresh() {
		RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);
		try {
			rongCloud.user.refresh("12345678", null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void sendCmdMsgMessage() throws Exception {
		Message msg = rongCloud.rongCloud.message;
		msg.publishPrivate(Keys.RONGCLOUD_SYSTEM_ID + "", new String[] { "10000000001", "10000000007" },
				new CmdMsgMessage("flushFriends", null), null, null, null, 0, 0, 0, 0);
		msg.publishPrivate("10000000030", new String[] { "10000000001" }, new TxtMessage("您已添加test5成为好友", null), null,
				null, null, 0, 0, 0, 0);
	}
	@Autowired
	ActivityDao activityDao;
	@Test
	public void sendBounty() {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("type", "getBounty");
		map.put("url", activityDao.findOne(7L).getUrl());
		new Thread(()->{
			try {
				rongCloud.sendSystemMessage(new String[] {10000000003L+"",10000000001L+""},
						new TxtMessage("您已获得0元奖金，请至活动页面领取(圈子页面顶端)", Formatter.gson.toJson(map)));
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}).start();
	}
}
