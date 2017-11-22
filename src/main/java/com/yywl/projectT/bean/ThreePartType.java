package com.yywl.projectT.bean;

public class ThreePartType {

	public final static String WEIBO = "WEIBO";
	public final static String WECHAT = "WECHAT";
	public final static String QQ = "QQ";

	public static boolean checkType(String type) {
		return QQ.equals(type) || WECHAT.equals(type) || WEIBO.equals(type);
	}

}
