package com.yywl.projectT.bean;

public class DistanceConverter {
	private final static double PI = 3.14159265358979323; // 圆周率
	private final static double R = 6371229; // 地球的半径

	/**
	 * 单位为米
	 * @param longitude1
	 * @param latitude1
	 * @param longitude2
	 * @param latitude2
	 * @return
	 */
	public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
		double x, y, distance;
		x = (longitude2 - longitude1) * PI * R * Math.cos(((latitude1 + latitude2) / 2) * PI / 180) / 180;
		y = (latitude2 - latitude1) * PI * R / 180;
		distance = Math.hypot(x, y);
		return distance;
	}
	
}
