package com.yywl.projectT.web.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bo.NoteBo;
import com.yywl.projectT.bo.UserBo;
import com.yywl.projectT.dmo.NoteDmo;
import com.yywl.projectT.dmo.UserDmo;

@RestController
@RequestMapping("message")
public class MessageController {
	private final static Log log=LogFactory.getLog(MessageController.class);

	@Autowired
	NoteBo noteBo;

	@PostMapping("abandonNote")
	public Callable<ResultModel> abandonNote(long userId, String token, long noteId) {
		return () -> {
			noteBo.abandon(userId, token, noteId);
			return new ResultModel();
		};
	}
	
	@PostMapping("findNote")
	public Callable<ResultModel> findNote(long userId,String token,long noteId){
		return ()->{
			NoteDmo noteDmo=noteBo.findNote(userId,token,noteId);
			Map<String, Object> map=new HashMap<>();
			map.put("id", noteDmo.getId());
			map.put("senderId", noteDmo.getSender().getId());
			map.put("nickname", noteDmo.getSender().getNickname());
			map.put("avatarSignature", noteDmo.getSender().getAvatarSignature());
			map.put("content", noteDmo.getContent());
			map.put("state", noteDmo.getState());
			map.put("createTime", noteDmo.getCreateTime().getTime());
			return new ResultModel(true, "", map);
		};
	}

	@PostMapping("findNotesByReceiverId")
	public Callable<ResultModel> findNotesByReceiverId(long userId, String token, int page, int size) {
		return () -> {
			List<Map<String, Object>> vos = new LinkedList<>();
			List<NoteDmo> notes = noteBo.findNotesByReceiver_Id(userId, token, page, size);
			for (NoteDmo note : notes) {
				Map<String, Object> map = new HashMap<>();
				map.put("avatarSignature", note.getSender().getAvatarSignature());
				map.put("nickname", note.getSender().getNickname());
				map.put("id", note.getId());
				map.put("senderId", note.getSender().getId());
				map.put("content", note.getContent());
				map.put("state", note.getState());
				map.put("createTime", note.getCreateTime().getTime());
				vos.add(map);
			}
			return new ResultModel(true, "", vos);
		};
	}
	
	@PostMapping("findNotesBySenderId")
	public Callable<ResultModel> findNotesBySenderId(long userId, String token, int page, int size) {
		return () -> {
			List<Map<String, Object>> vos = new LinkedList<>();
			List<NoteDmo> notes = noteBo.findNotesBySenderId(userId, token, page, size);
			for (NoteDmo note : notes) {
				Map<String, Object> map = new HashMap<>();
				map.put("avatarSignature", note.getReceiver().getAvatarSignature());
				map.put("nickname", note.getReceiver().getNickname());
				map.put("id", note.getId());
				map.put("receiverId", note.getReceiver().getId());
				map.put("content", note.getContent());
				map.put("state", note.getState());
				map.put("createTime", note.getCreateTime().getTime());
				vos.add(map);
			}
			return new ResultModel(true, "", vos);
		};
	}
	
	@PostMapping("replyNote")
	public Callable<ResultModel> replyNote(long userId ,String token,long noteId,String content){
		return ()->{
			if (StringUtils.isEmpty(content)) {
				log.error("小纸条内容不能为空");
				throw new Exception("小纸条内容不能为空");
			}
			long id= this.noteBo.replyNote(userId,token,noteId,content);
			Map<String,Object> map=new HashMap<>();
			map.put("note", id);
			return new ResultModel(true,"",map);
		};
	}
	@PostMapping("readReplyNote")
	public Callable<ResultModel> readReplyNote(long userId,String token,long noteId){
		return ()->{
			UserDmo user= this.userBo.loginByToken(userId, token);
			if (!user.getIsInit()) {
				log.error("请初始化个人信息");
				throw new Exception("请初始化个人信息");
			}
			this.noteBo.readReply(noteId);
			return new ResultModel(true);
		};
	}

	@Autowired
	UserBo userBo;
	
	@PostMapping("sendNote")
	public Callable<ResultModel> sendNote(long userId, String token, long receiverId, String content) {
		return () -> {
			if (StringUtils.isEmpty(content)) {
				log.error("小纸条内容不能为空");
				throw new Exception("小纸条内容不能为空");
			}
			long id= noteBo.sendNote(userId, token, receiverId, content);
			Map<String,Object> map=new HashMap<>();
			map.put("note", id);
			return new ResultModel(true,"",map);
		};
	}
	
	@PostMapping("storeNote")
	public Callable<ResultModel> storeNote(long userId, String token, long noteId) {
		return () -> {
			noteBo.store(userId, token, noteId);
			return new ResultModel();
		};
	}
}
