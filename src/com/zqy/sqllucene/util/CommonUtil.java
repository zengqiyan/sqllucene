package com.zqy.sqllucene.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommonUtil {
	/**
	 * 
	 * @param name
	 * @param value
	 * @param object
	 */
	public static void setFieldValue(String name, Object value, Object object) {
		Class<? extends Object> clazz = object.getClass();
		for (Method m : clazz.getDeclaredMethods()) {
			/**
			 * m.getName().toLowerCase().equals(("set" + name).toLowerCase())
			 * 这样比较是为了防止不规范的发生，方法名：setAbc，属性名是：aBc<br/>
			 * 比较后半截的小写是否相等
			 */
			if (m.getName().toLowerCase().equals(("set" + name).toLowerCase())) {
				try {
					m.invoke(object, new Object[] { value });
				} catch (IllegalArgumentException e) {

					e.printStackTrace();
				} catch (IllegalAccessException e) {

					e.printStackTrace();
				} catch (InvocationTargetException e) {

					e.printStackTrace();
				}
			}
		}
	}
	public static void getFieldValue(String name, Object value, Object object) {
		Class<? extends Object> clazz = object.getClass();
		for (Method m : clazz.getDeclaredMethods()) {
			/**
			 * m.getName().toLowerCase().equals(("set" + name).toLowerCase())
			 * 这样比较是为了防止不规范的发生，方法名：setAbc，属性名是：aBc<br/>
			 * 比较后半截的小写是否相等
			 */
			if (m.getName().toLowerCase().equals(("get" + name).toLowerCase())) {
				try {
					m.invoke(object, new Object[] { value });
				} catch (IllegalArgumentException e) {

					e.printStackTrace();
				} catch (IllegalAccessException e) {

					e.printStackTrace();
				} catch (InvocationTargetException e) {

					e.printStackTrace();
				}
			}
		}
	}
	public static java.lang.reflect.Field[] getField(Object object) {
		Class<? extends Object> clazz = object.getClass();
		Field[] fields=clazz.getClass().getDeclaredFields();//获得对象所有属性
		
		return fields;
		
	}
}
