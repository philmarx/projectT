package com.yywl.projectT.bean;

public class WeixinSiginatureBean {

	private String accessToken;

	private long expire;

	private String ticket;

	private String timestamp;

	private String noncestr;

	public static ThreadLocal<WeixinSiginatureBean> local = new ThreadLocal<>();

	public WeixinSiginatureBean(String accessToken, long expire, String ticket, String timestamp, String noncestr) {
		super();
		this.accessToken = accessToken;
		this.expire = expire;
		this.ticket = ticket;
		this.timestamp = timestamp;
		this.noncestr = noncestr;
	}

	public WeixinSiginatureBean() {
		super();
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getNoncestr() {
		return noncestr;
	}

	public void setNoncestr(String noncestr) {
		this.noncestr = noncestr;
	}
	
}
