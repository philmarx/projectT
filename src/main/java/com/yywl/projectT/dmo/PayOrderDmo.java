package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="pay_order")
public class PayOrderDmo implements Serializable{

	private static final long serialVersionUID = 8976232455950022923L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date notifyTime;
	
	/**
	 * 已退款金额
	 */
	private int refundAmount;

	private String tradeNo;
	
	private String outTradeNo;

	/**
	 * 方式，可以是微信，支付宝
	 * 
	 */
	private String type;

	/**
	 * 充值总金额
	 */
	private int totalAmount;

	public PayOrderDmo() {
		super();
	}
	
	public Long getId() {
		return id;
	}

	public Date getNotifyTime() {
		return notifyTime;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}
	
	public int getRefundAmount() {
		return refundAmount;
	}

	public int getTotalAmount() {
		return totalAmount;
	}

	public String getTradeNo() {
		return tradeNo;
	}


	public String getType() {
		return type;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public void setNotifyTime(Date notifyTime) {
		this.notifyTime = notifyTime;
	}


	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}


	public void setRefundAmount(int refundAmount) {
		this.refundAmount = refundAmount;
	}


	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}



	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
