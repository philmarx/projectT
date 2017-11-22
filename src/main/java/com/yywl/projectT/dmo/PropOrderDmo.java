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

/**
 * 购买道具的订单
 */
@Entity
@Table(name = "prop_order")
public class PropOrderDmo implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String outTradeNo;
	/**
	 * 0:小纸条,1:标签消除卡,2:改名卡,3:补签卡,4:vip一个月,5:vip三个月,6:vip十二个月
	 */
	private int propType;
	private int count;
	private Long userId;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	private String payType;

	public PropOrderDmo(Long id, String outTradeNo, int propType, int count, Long userId,
			Date createTime, String payType) {
		super();
		this.id = id;
		this.outTradeNo = outTradeNo;
		this.propType = propType;
		this.count = count;
		this.userId = userId;
		this.createTime = createTime;
		this.payType = payType;
	}

	public PropOrderDmo() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public int getPropType() {
		return propType;
	}

	public void setPropType(int propType) {
		this.propType = propType;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishTime;

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}
	
	
}
