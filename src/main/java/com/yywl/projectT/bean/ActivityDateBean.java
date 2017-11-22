package com.yywl.projectT.bean;

import java.util.Calendar;
import java.util.Date;

public class ActivityDateBean {
	//活动结束时间，包括人气女神，达人榜
	public static Date end(){
		Calendar calendar=Calendar.getInstance();
		calendar.set(2017, 9, 31, 23, 59, 59);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	//抽奖结束时间
	public static Date luckDrawEnd() {
		Calendar calendar=Calendar.getInstance();
		calendar.set(2017, 8, 10, 23, 59, 59);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	//抽奖开始时间
	public static Date luckDrawStart() {
		Calendar calendar=Calendar.getInstance();
		calendar.set(2017, 7, 11, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	//活动开始时间，包括人气女神，达人榜
	public static Date start(){
		Calendar calendar=Calendar.getInstance();
		calendar.set(2017, 7, 13, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
