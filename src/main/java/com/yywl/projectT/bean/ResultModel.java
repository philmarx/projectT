package com.yywl.projectT.bean;

public class ResultModel {
	private boolean success=true;
	private String msg="";
	private Object data;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public ResultModel() {
		super();
		this.success = true;
	}

	public ResultModel(boolean success) {
		super();
		this.success = success;
	}

	public ResultModel(boolean success, String msg, Object data) {
		super();
		this.success = success;
		this.msg = msg;
		this.data = data;
	}

}
