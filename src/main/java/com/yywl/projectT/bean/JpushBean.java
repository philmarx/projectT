package com.yywl.projectT.bean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

public class JpushBean {

	private final static Log log = LogFactory.getLog(JpushBean.class);

	private static ClientConfig clientConfig = ClientConfig.getInstance();
	
	private static JPushClient jpushClient = new JPushClient(Keys.JPhsh.MARKET_SECRET, Keys.JPhsh.APP_KEY, null, clientConfig);
	
	public static void push(String alert,String id) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all())
				.setAudience(Audience.alias(id)).setNotification(Notification.alert(alert)).build();
		try {
			PushResult result = jpushClient.sendPush(payload);
			if (result.getResponseCode()!=200) {
				log.error(id+":推送失败");
			}
		} catch (APIConnectionException e) {
			log.error("Connection error. Should retry later. ", e);
			log.error("Sendno: " + payload.getSendno());
		} catch (APIRequestException e) {
			log.error("Error response from JPush server. Should review and fix it. ", e);
			log.info("HTTP Status: " + e.getStatus());
			log.info("Error Code: " + e.getErrorCode());
			log.info("Error Message: " + e.getErrorMessage());
			log.info("Msg ID: " + e.getMsgId());
			log.error("Sendno: " + payload.getSendno());
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
	}
	
	
	public static void push(String alert,String message,String id) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all())
				.setAudience(Audience.alias(id))
				.setNotification(Notification.alert(alert)).setMessage(Message.content(message)).build();
		try {
			PushResult result = jpushClient.sendPush(payload);
			if (result.getResponseCode()!=200) {
				throw new Exception("推送失败");
			}
		} catch (APIConnectionException e) {
			log.error("Connection error. Should retry later. ", e);
			log.error("Sendno: " + payload.getSendno());
		} catch (APIRequestException e) {
			log.error("Error response from JPush server. Should review and fix it. ", e);
			log.info("HTTP Status: " + e.getStatus());
			log.info("Error Code: " + e.getErrorCode());
			log.info("Error Message: " + e.getErrorMessage());
			log.info("Msg ID: " + e.getMsgId());
			log.error("Sendno: " + payload.getSendno());
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
	}
	
}
