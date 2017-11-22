package com.yywl.projectT.bo;

import org.junit.Test;

public class TestDistance {
	@Test
	public void distance() {
		//经度
		int longitude=1;
		//纬度
		int latitude=3;
		
		int longitude2=2;
		int latitude2=-2;
		
		double t= Math.pow((latitude-latitude2), 2)+Math.pow((longitude-longitude2), 2);
		double distance=Math.pow(t, 0.5);
		System.out.println(distance);
	}
}
