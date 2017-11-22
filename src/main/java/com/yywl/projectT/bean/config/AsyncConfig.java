package com.yywl.projectT.bean.config;

import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
	private ThreadPoolTaskExecutor taskExecutor;

	@PostConstruct
	public void postConstruct() {
		this.taskExecutor = new ThreadPoolTaskExecutor();
		this.taskExecutor.setCorePoolSize(50);// 线程池维护线程的最小数量.
		this.taskExecutor.setMaxPoolSize(500);// 线程池维护线程的最大数量.
		this.taskExecutor.setQueueCapacity(100);// 线程池所使用的缓冲队列
		this.taskExecutor.initialize();
	}

	@Override
	public Executor getAsyncExecutor() {
		return this.taskExecutor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return null;
	}
	
	@PreDestroy
	public void preDestory(){
		this.taskExecutor.shutdown();
		this.taskExecutor=null;
	}
}
