package com.aliyun.sts.sample;

import org.junit.Test;

import com.aliyun.oss.common.auth.Credentials;
import com.aliyun.oss.common.auth.DefaultCredentials;
import com.aliyun.oss.common.comm.RequestMessage;
import com.aliyun.oss.internal.OSSRequestSigner;

public class TestHmacSha1 {
	@Test
	public void main() {
		Credentials creds = new DefaultCredentials("STS.FXi9ue8wca8YQB8UDfzKM4jqw",
				"2NTqhabxfzyZCTYF6TkUdymtwLQD7TU77rt7EoE9eGev",
				"CAIS0AN1q6Ft5B2yfSjIp5vccs/R1ahC1vqyU2SJsUQzdsRh26/alTz2IHlIfHltCOAWsv8+lWBT5/celrh+W4NIX0rNaY5t9ZlN9wqkbtIaNmhRLv9W5qe+EE2/VjQdtq27OpflLr70fvOqdCqL9Etayqf7cjOPRkGsNYbz57dsctUQWHvfD19BH8wECQZ+j8UYOHDNT9zPVCTnmW3NFkFllxNhgGdkk8SFz9ab9wDVgS+0qKMcrJ+jJYO/PYs+Zc0nC4nlg7QtJ/Gain4LtUpS7qB0gadY5XW1RLj/bnBV5xKZSbu2lvRkMA5+YIUjBqdAt4KSvPZku+vV5c2VrhFWJrNtTjj4ToKty9emYOSyLYQVeLL2J3nq2NKCPYWPlWFCW38AMx5QcNcMM2J5DQdWKgvXMai64lvHEF3BE6GOy/MxyoEnjQexp4LSeQjQG+7Iin1CasRiPVsyMBwb1nD9NbIdfhYLIwM3V+bFD6cLNUoG8f244lWJB3Y7lCEM4ceTPa2G5votDqzkRY9D3IYnY5BLjnAnVVyfScj10xtMJDE0EeYKi/i8acTuueOfs+yXYP/bDPccoUldcj3XoCeLRGlZMSrr/NonZEoUEHFab0Q1mRqAAXKqABlRGOp1t+fyEuJBYBU0IgGxP6QEPLBotcQWjctAWX+VXv47ivD9l6liFbdK7+6qBZTN7P8YtyjEFDxlV++I4xdfm8sCK7knVJnLyvJDjkLJeuOLC54l1O9qcdu6+PPPWoBCbGgVJcVCAbJHH10FxRsGiJ26bY6Eu8MqVE9u");
		OSSRequestSigner singer = new OSSRequestSigner("GET", "/projectt/user/", creds);
		RequestMessage request = new RequestMessage();
		singer.sign(request);
		System.out.println(request.getHeaders().toString());
	}
}
