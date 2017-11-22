package com.yywl.projectT;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.bean.Formatter;

@SpringBootTest(classes = ProjectTApplication.class)
@RunWith(SpringRunner.class)
public class TestFormatter {
	
	@Test
	public void getDate() throws ParseException{
		Date date=Formatter.dateTimeFormatter.parse("2017-3-6 12:10:55");
		System.out.println(Formatter.dateTimeFormatter.format(date));
	}
	
	@Test
	public void getCurrentTime(){
		Date date=new Date(System.currentTimeMillis());
		System.out.println(Formatter.dateTimeFormatter.format(date));
	}
}
