package com.yywl.projectT.bo;

import java.util.List;

import com.yywl.projectT.dmo.NoteDmo;

public interface NoteBo {

	long sendNote(long userId, String token, long receiverId, String content) throws Exception;

	void store(long userId, String token, long noteId) throws Exception;

	void abandon(long userId, String token, long noteId) throws Exception;

	List<NoteDmo> findNotesByReceiver_Id(long userId, String token, int page, int size) throws Exception;

	NoteDmo findNote(long userId, String token, long noteId) throws Exception;

	long replyNote(long userId, String token, long noteId, String content) throws Exception;

	List<NoteDmo> findNotesBySenderId(long userId, String token, int page, int size) throws Exception;

	void readReply(long noteId) throws Exception;

}
