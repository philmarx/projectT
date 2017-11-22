package com.yywl.projectT.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlWeixinResultModel {

	@XmlElement(name = "return_code")
	private String returnCode;

	@XmlElement(name = "return_msg")
	private String returnMsg;

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	public XmlWeixinResultModel() {
		super();
	}

	public XmlWeixinResultModel(String returnCode, String returnMsg) {
		super();
		this.returnCode = returnCode;
		this.returnMsg = returnMsg;
	}

	public final static String CODE_SUCCESS = "SUCCESS";
	
	public final static String CODE_FAIL = "FAIL";
	
	
}
