package com.yywl.projectT;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.yywl.projectT.bean.Keys;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class ProjectTApplication extends SpringBootServletInitializer implements EmbeddedServletContainerCustomizer {
	/**
	 * 在tomcat运行需要继承SpringBootServletInitializer实现configure方法
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ProjectTApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ProjectTApplication.class, args);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		container.setPort(Keys.SERVER_PORT);
		container.setSessionTimeout(10, TimeUnit.MINUTES);
	}
}
