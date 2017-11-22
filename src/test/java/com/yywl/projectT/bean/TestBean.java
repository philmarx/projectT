package com.yywl.projectT.bean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.vo.UserVo;

import io.rong.messages.InfoNtfMessage;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestBean {

	@Test
	public void write() throws FileNotFoundException, IOException{
		/*Resource res=new ClassPathResource("gateway.log");*/
		File file=new File("gateway.log");
		if (!file.exists()) {
			file.createNewFile();
		}
		System.out.println(file.getAbsolutePath());
		BufferedWriter writer=new BufferedWriter(new FileWriter(file));
		writer.newLine();
		writer.append(Formatter.dateTimeFormatter.format(new Date()));
		writer.newLine();
		writer.append("你好");
		writer.close();
	}
	@Test
	public void maxSize() {
		System.out.println(ValidatorBean.size(100));
	}

	@PersistenceContext
	EntityManager manager;

	@Test
	public void objectCopy() throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException {
		UserDmo dmo = manager.find(UserDmo.class, 10000000025L);
		UserVo vo=ObjectCopyUtils.copy(dmo, UserVo.class);
		System.out.println(vo.getNickname());
	}
	@Autowired
	RongCloudBean rongCloud;
	@Test
	public void sendJpush(){
		JpushBean.push("测试", "testAlias");
	}
	@Test
	public void testRongCloudBean(){
		try {
			rongCloud.sendMessageToFriend(Keys.RONGCLOUD_SYSTEM_ID, 10000000025L,
					new InfoNtfMessage("测试。", null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
