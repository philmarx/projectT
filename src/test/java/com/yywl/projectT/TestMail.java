package com.yywl.projectT;

import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Test;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.yywl.projectT.bean.Keys;

public class TestMail {

	@Test
	public void send() throws Exception {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.auth", "true");
		Session session = Session.getInstance(props);
		session.setDebug(true);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress("mjy@hzease.com"));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress("17702525841@163.com"));
		message.setSubject("主题2", "utf-8");
		message.setText("内容2", "utf-8");
		message.saveChanges();
		Transport transport = session.getTransport();
		transport.connect("smtp.hzease.com", Keys.JavaMail.USERNAME, Keys.JavaMail.PASSWORD);
		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
		transport.close();
	}

	@Test
	public void receive() throws MessagingException, IOException {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.auth", "true");
		Session session = Session.getInstance(props);
		session.setDebug(false);
		IMAPStore store = (IMAPStore) session.getStore("imap");
		store.connect("imap.hzease.com", Keys.JavaMail.USERNAME, Keys.JavaMail.PASSWORD);
		IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		int start = folder.getMessageCount() - folder.getUnreadMessageCount() + 1;
		int end = folder.getMessageCount();
		Message[] messages = folder.getMessages(start, end);
		for (Message message : messages) {
			System.out.println("subject:"+message.getSubject());
			Object objContent = message.getContent();
			if (objContent.getClass() != MimeMultipart.class) {
				continue;
			}
			MimeMultipart mimeMultipart = (MimeMultipart) objContent;
			BodyPart part = mimeMultipart.getBodyPart(0);
			String str = (String) part.getContent();
			str = str.trim();
			if (!str.contains("Ready for Sale")) {
				continue;
			}
			if (str.contains("App Version Number: ")) {
				str = str.split("App Version Number: ")[1];
				if (str.contains("\n")) {
					str = str.split("\n")[0].trim();
				}
				if (str.contains(" ")) {
					str = str.split(" ")[0].trim();
				}
			}
			System.out.println(str);
		}
		folder.close(false);
		store.close();
	}
}
