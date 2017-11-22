package com.yywl.projectT.bo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.yywl.projectT.bean.Formatter;
import com.yywl.projectT.bean.FriendPointUtil;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.NoteState;
import com.yywl.projectT.dao.FriendDao;
import com.yywl.projectT.dao.NoteDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.FriendDmo;
import com.yywl.projectT.dmo.NoteDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.UserDmo;

import io.rong.messages.TxtMessage;

@Service
public class NoteBoImpl implements NoteBo {

	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;

	@Autowired
	NoteDao noteDao;

	@Autowired
	RongCloudBean rongCloud;

	@Autowired
	PropBo propBo;

	@Autowired
	FriendDao friendDao;

	private static final Log log = LogFactory.getLog(NoteBoImpl.class);

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public long sendNote(long userId, String token, long receiverId, String content) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			log.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		UserDmo receiver = this.userDao.findOne(receiverId);
		if (receiver == null) {
			log.error("接收者不存在");
			throw new Exception("接收者不存在");
		}
		PropDmo prop = this.propBo.findByUser_Id(userId);
		if (prop.getNoteCount() < 1) {
			log.error("小纸条不足");
			throw new Exception("NoNote");
		}
		prop.setNoteCount(prop.getNoteCount() - 1);
		this.propBo.save(prop);
		NoteDmo note = new NoteDmo();
		note.setContent(content);
		note.setCreateTime(new Date());
		note.setState(0);
		note.setSender(user);
		note.setReceiver(receiver);
		noteDao.save(note);
		FriendDmo friend1 = this.friendDao.findByOwner_IdAndFriend_Id(userId, receiverId);
		friend1 = friend1 == null ? null
				: (friend1.getEvaluatePoint() == 0 || friend1.getEvaluatedPoint() == 0 ? null : friend1);
		FriendDmo friend2 = this.friendDao.findByOwner_IdAndFriend_Id(receiverId, userId);
		friend2 = friend2 == null ? null
				: (friend2.getEvaluatePoint() == 0 || friend2.getEvaluatedPoint() == 0 ? null : friend2);
		if ((friend1 != null) && (friend2 != null)) {
			friendPointUtil.addFriendPoint(friend1);
			friendPointUtil.addFriendPoint(friend2);
			this.friendDao.save(friend1);
			this.friendDao.save(friend2);
			final long id1=friend1.getOwner().getId();
			final long id2=friend2.getOwner().getId();
			new Thread(()->{
				try {
					rongCloud.sendSystemCmdMsgToOne(id1, Keys.RongCloud.CMD_MSG_REFRESH_FRIENDS);
					rongCloud.sendSystemCmdMsgToOne(id2, Keys.RongCloud.CMD_MSG_REFRESH_FRIENDS);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
		}
		Map<String, Object> map = new HashMap<>();
		map.put("avatarSignature", note.getSender().getAvatarSignature());
		map.put("nickname", note.getSender().getNickname());
		map.put("id", note.getId());
		map.put("senderId", note.getSender().getId());
		map.put("content", note.getContent());
		map.put("state", note.getState());
		map.put("type", "note");
		map.put("createTime", note.getCreateTime().getTime());
		String json = Formatter.gson.toJson(map);
		new Thread(()->{
			try {
				rongCloud.sendSystemCmdMsgToOne(receiverId, Keys.RongCloud.CMD_MSG_RECEIVE_NOTES, json);
				rongCloud.sendSystemMessage(new String[] { receiverId + "" }, new TxtMessage("您收到一张来自"+note.getSender().getNickname()+"小纸条，点击查看",json));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		return note.getId();
	}

	@Autowired
	FriendPointUtil friendPointUtil;

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public long replyNote(long userId, String token, long noteId, String content) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			log.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		NoteDmo noteOrigin = this.noteDao.findOne(noteId);
		if (noteOrigin.getState() == NoteState.已回复.ordinal()) {
			log.error("只能回复一次");
			throw new Exception("只能回复一次");
		}
		if (noteOrigin.getState() != NoteState.未读.ordinal()) {
			log.error("该小纸条不能回复");
			throw new Exception("该小纸条不能回复");
		}
		noteOrigin.setState(NoteState.已回复.ordinal());
		NoteDmo note = new NoteDmo(null, user, noteOrigin.getSender(), NoteState.不能回复未读.ordinal(), new Date(), content);
		this.noteDao.save(noteOrigin);
		this.noteDao.save(note);
		Map<String, Object> map = new HashMap<>();
		map.put("avatarSignature", note.getSender().getAvatarSignature());
		map.put("nickname", note.getSender().getNickname());
		map.put("id", note.getId());
		map.put("senderId", note.getSender().getId());
		map.put("content", note.getContent());
		map.put("state", note.getState());
		map.put("type", "note");
		map.put("createTime", note.getCreateTime().getTime());
		String json = Formatter.gson.toJson(map);
		new Thread(()->{
			try {
				rongCloud.sendSystemCmdMsgToOne(note.getReceiver().getId(), Keys.RongCloud.CMD_MSG_RECEIVE_NOTES, json);
				rongCloud.sendSystemMessage(new String[] { note.getReceiver().getId() + "" },
						new TxtMessage("您发送给"+note.getSender().getNickname()+"小纸条收到了回复，点击查看", json));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}).start();
		return note.getId();
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void readReply(long noteId) throws Exception {
		NoteDmo note = this.noteDao.findOne(noteId);
		if (note == null) {
			throw new Exception("小纸条不存在");
		}
		note.setState(NoteState.不能回复已读.ordinal());
		this.noteDao.save(note);
	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void store(long userId, String token, long noteId) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			log.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		NoteDmo note = noteDao.findOne(noteId);
		if (note == null) {
			log.error("小纸条不存在");
			throw new Exception("小纸条不存在");
		}
		if (note.getReceiver().getId() != user.getId()) {
			return;
		}
		if (note.getState() == NoteState.未读.ordinal()) {
			note.setState(NoteState.已读.ordinal());
			noteDao.save(note);
		}

	}

	@Override
	@Transactional(rollbackOn = Throwable.class)
	public void abandon(long userId, String token, long noteId) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		if (!user.getIsInit()) {
			log.error("请初始化个人信息");
			throw new Exception("请初始化个人信息");
		}
		NoteDmo note = noteDao.findOne(noteId);
		if (note.getReceiver().getId() != user.getId()) {
			return;
		}
		note.setState(NoteState.已删除.ordinal());
		noteDao.save(note);
	}

	@Override
	public List<NoteDmo> findNotesByReceiver_Id(long userId, String token, int page, int size) throws Exception {
		userBo.loginByToken(userId, token);
		Page<NoteDmo> notes = this.noteDao.findByReceiver_IdAndStateNot(userId, NoteState.已删除.ordinal(),
				new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC, "createTime"));
		return notes.getContent();
	}

	@Override
	public List<NoteDmo> findNotesBySenderId(long userId, String token, int page, int size) throws Exception {
		userBo.loginByToken(userId, token);
		Page<NoteDmo> notes = this.noteDao.findBySender_Id(userId,
				new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC, "createTime"));
		return notes.getContent();
	}

	@Override
	public NoteDmo findNote(long userId, String token, long noteId) throws Exception {
		userBo.loginByToken(userId, token);
		NoteDmo note = this.noteDao.findOne(noteId);
		if (note.getReceiver().getId().longValue() != userId&&note.getSender().getId().longValue()!=userId) {
			throw new Exception("您无权查看");
		}
		return note;
	}

}
