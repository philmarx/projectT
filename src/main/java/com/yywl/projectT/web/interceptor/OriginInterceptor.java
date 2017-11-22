package com.yywl.projectT.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class OriginInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//第二个参数为允许访问的域名
		response.setHeader("Access-Control-Allow-Origin", "https://hzease.com");
		return super.preHandle(request, response, handler);
	}
}

