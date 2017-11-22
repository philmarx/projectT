package com.yywl.projectT.vo;

public class TempEvaluationVo {

	/**
	 * 被评价者
	 */
	private long userId;

	private double avgPoint;
	
	public double getAvgPoint() {
		return avgPoint;
	}

	public void setAvgPoint(double avgPoint) {
		this.avgPoint = avgPoint;
	}


	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public TempEvaluationVo() {
		super();
	}

	
	
}
