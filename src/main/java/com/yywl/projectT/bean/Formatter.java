package com.yywl.projectT.bean;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;

public class Formatter {
	public final static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public final static Gson gson = new Gson();
	public final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	public static final DecimalFormat decimalFormat = new DecimalFormat("#");
}
