package com.yywl.projectT.bean;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ObjectCopyUtils {
	/**
	 * 拷贝对象，targetType中与origin相同的属性将被拷贝过来。不包含对象属性，集合属性。
	 * 
	 * @param origin
	 * @param targetType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T copy(Object origin, Class<?> targetType) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		Class<?> originType = origin.getClass();
		Field[] fields = targetType.getDeclaredFields();
		Object target = targetType.newInstance();
		for (Field field : fields) {
			Class<?> fieldType=field.getType();
			String fieldName = field.getName();
			String setterName = setMethodFromField(fieldName);
			Method setter = targetType.getMethod(setterName, fieldType);
			String getterName = getMethodFromField(fieldName);
			Method getter = originType.getMethod(getterName);
			Object value = getter.invoke(origin);
			setter.invoke(target, value);
		}
		return (T) target;
	}

	/**
	 * 给定set和get方法名，获取属性名
	 * 
	 * @param methodName
	 * @return
	 */
	public static String fieldNameFromMethod(String methodName) {
		String fieldName = methodName.replaceFirst("get", "").replaceFirst("set", "");
		String firstChar = new String(new char[] { fieldName.charAt(0) });
		fieldName = firstChar.toLowerCase() + fieldName.substring(1, fieldName.length());
		return fieldName;
	}

	/**
	 * 给定属性名获取get方法
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String getMethodFromField(String fieldName) {
		String firstChar = new String(new char[] { fieldName.charAt(0) });
		firstChar = firstChar.toUpperCase();
		fieldName = firstChar + fieldName.substring(1, fieldName.length());
		return "get" + fieldName;
	}
	/**
	 * 给定属性名获取set方法
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String setMethodFromField(String fieldName) {
		String firstChar = new String(new char[] { fieldName.charAt(0) });
		firstChar = firstChar.toUpperCase();
		fieldName = firstChar + fieldName.substring(1, fieldName.length());
		return "set" + fieldName;
	}
}
