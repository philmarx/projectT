package com.yywl.projectT.bean.component;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yywl.projectT.bean.Keys;

import io.rong.RongCloud;
import io.rong.messages.BaseMessage;
import io.rong.messages.CmdMsgMessage;
import io.rong.messages.InfoNtfMessage;
import io.rong.models.ChatRoomInfo;
import io.rong.models.CodeSuccessReslut;
import io.rong.models.TokenReslut;

@Component
public class RongCloudBean {

	public final RongCloud rongCloud = RongCloud.getInstance(Keys.RongCloud.APP_KEY, Keys.RongCloud.APP_SECRET);

	/**
	 * 根据用户ID获取token
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */

	public String getToken(String id, String nickname) throws Exception {
		TokenReslut userGetTokenResult = rongCloud.user.getToken(id + "", nickname,
				"http://oss.yueyuan.pro/user/" + id + "/avatar");
		return userGetTokenResult.getToken();
	}

	/**
	 * 创建聊天室
	 * 
	 * @throws Exception
	 */
	public void createChatRoom(String roomId, String roomName) throws Exception {
		ChatRoomInfo[] chatRoomInfos = { new ChatRoomInfo(roomId, roomName) };
		CodeSuccessReslut result = rongCloud.chatroom.create(chatRoomInfos);
		if (result.getCode() != 200) {
			throw new Exception(result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 创建群组
	 * 
	 * @throws Exception
	 */
	public void createGroup(String userId, String groupId, String groupName) throws Exception {
		CodeSuccessReslut result = rongCloud.group.create(new String[] { userId }, groupId, groupName);
		if (result.getCode() != 200) {
			throw new Exception(result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 加入群组
	 * 
	 * @throws Exception
	 */
	public void joinGroup(String userId, String groupId, String groupName) throws Exception {
		CodeSuccessReslut result = rongCloud.group.join(new String[] { userId }, groupId, groupName);
		if (result.getCode() != 200) {
			throw new Exception(result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 销毁群组
	 * 
	 * @throws Exception
	 */
	public void destoryGroup(String userId, String groupId) throws Exception {
		CodeSuccessReslut result = rongCloud.group.dismiss(userId, groupId);
		if (result.getCode() != 200) {
			throw new Exception(result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 离开群组
	 * 
	 * @throws Exception
	 */
	public void quitGroup(String userId, String groupId) throws Exception {
		CodeSuccessReslut result = rongCloud.group.quit(new String[] { userId }, groupId);
		if (result.getCode() != 200) {
			throw new Exception(result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 刷新群组
	 * 
	 * @throws Exception
	 */
	public void refreshGroup(String groupId, String groupName) throws Exception {
		CodeSuccessReslut result = rongCloud.group.refresh(groupId, groupName);
		if (result.getCode() != 200) {
			throw new Exception(result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 销毁聊天室
	 * 
	 * @throws Exception
	 */

	public void destoryChatRoom(String roomId) throws Exception {
		CodeSuccessReslut result = rongCloud.chatroom.destroy(new String[] { roomId, "cmd" + roomId });
		if (result.getCode() != 200) {
			throw new Exception(result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 发送聊天室消息
	 * 
	 * @throws Exception
	 */
	public void sendMessageToChatRoom(String userId, long roomId, BaseMessage msg) throws Exception {
		String roomIdString = roomId + "";
		if (msg.getClass() == CmdMsgMessage.class || msg.getClass() == InfoNtfMessage.class) {
			roomIdString = "cmd" + roomIdString;
		}
		CodeSuccessReslut result = rongCloud.message.publishChatroom(userId, new String[] { roomIdString }, msg);
		if (result.getCode() != 200) {
			throw new Exception("发送消息失败");
		}
	}

	/**
	 * 刷新融云的昵称
	 * 
	 * @param id
	 * @param nickname
	 * @throws Exception
	 */
	public void refresh(Long id, String nickname) throws Exception {
		CodeSuccessReslut result = rongCloud.user.refresh(id + "", nickname, null);
		if (result.getCode() != 200) {
			throw new Exception("刷新用户失败，code=" + result.getCode() + ",msg=" + result.getErrorMessage());
		}
	}

	/**
	 * 私聊
	 * 
	 * @param userId
	 * @param id
	 * @param txtMessage
	 * @throws Exception
	 */
	public void sendMessageToFriend(long userId, long receiverId, BaseMessage msg) throws Exception {
		CodeSuccessReslut result = this.rongCloud.message.publishPrivate(userId + "", new String[] { receiverId + "" },
				msg, null, null, null, 1, 1, 1, 0);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 私聊
	 * 
	 * @param userId
	 * @param id
	 * @param txtMessage
	 * @throws Exception
	 */
	public void sendMessageToFriend(String userId, long receiverId, BaseMessage msg) throws Exception {
		CodeSuccessReslut result = this.rongCloud.message.publishPrivate(userId, new String[] { receiverId + "" }, msg,
				null, null, null, 1, 1, 1, 0);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 发送单条系统消息
	 * 
	 * @param toUserId
	 * @param name
	 * @throws Exception
	 */
	public void sendSystemCmdMsgToOne(long toUserId, String name) throws Exception {
		CodeSuccessReslut result = this.rongCloud.message.PublishSystem(Keys.RONGCLOUD_SYSTEM_ID,
				new String[] { "" + toUserId }, new CmdMsgMessage(name, ""), null, null, 1, 1);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}

	public void sendSystemCmdMsgToOne(long toUserId, String name, String data) throws Exception {
		CodeSuccessReslut result = this.rongCloud.message.PublishSystem(Keys.RONGCLOUD_SYSTEM_ID,
				new String[] { "" + toUserId }, new CmdMsgMessage(name, data), null, null, 1, 1);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}

	/**
	 * 发送单条系统消息
	 * 
	 * @param toUserId
	 * @param name
	 * @throws Exception
	 */
	public void sendSystemTextMsgToOne(long toUserId, BaseMessage message) throws Exception {
		CodeSuccessReslut result = this.rongCloud.message.PublishSystem(Keys.RONGCLOUD_SYSTEM_ID,
				new String[] { "" + toUserId }, message, null, null, 1, 1);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}

	public void sendSystemMessage(List<Long> memberIds, BaseMessage message) throws Exception {
		if (null == memberIds || memberIds.isEmpty()) {
			return;
		}
		List<String> toUserIds = new LinkedList<>();
		for (Long id : memberIds) {
			toUserIds.add(id + "");
		}
		String[] arr = (String[]) toUserIds.toArray(new String[toUserIds.size()]);
		CodeSuccessReslut result = this.rongCloud.message.PublishSystem(Keys.RONGCLOUD_SYSTEM_ID, arr, message, null,
				null, 1, 1);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}

	public void sendSystemMessage(Long memberId, BaseMessage message) throws Exception {
		CodeSuccessReslut result = this.rongCloud.message.PublishSystem(Keys.RONGCLOUD_SYSTEM_ID,
				new String[] { memberId + "" }, message, null, null, 1, 1);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}

	public void sendSystemMessage(String[] memberIds, BaseMessage message) throws Exception {
		if (null == memberIds || memberIds.length == 0) {
			return;
		}
		CodeSuccessReslut result = this.rongCloud.message.PublishSystem(Keys.RONGCLOUD_SYSTEM_ID, memberIds, message,
				null, null, 1, 1);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}

	}

	public void sendGroup(String senderId, String groupId, BaseMessage message) throws Exception {
		CodeSuccessReslut result = this.rongCloud.message.publishGroup(senderId, new String[] { groupId }, message,
				null, null, 1, 1, 1);
		if (result.getCode() != 200) {
			throw new Exception("" + result.getCode() + ":" + result.getErrorMessage());
		}
	}
}
