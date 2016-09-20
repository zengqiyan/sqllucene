package com.zqy.sqllucene.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.pojo.Column;

import test.Wz;

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
	public static void main(String[] args) {
		 Wz wz = new Wz();
		 wz.setId(1005);
		 wz.setTitle("1asdasdasdasda");
		 wz.setContent("IndexWriter可以根据多种情况进行删除deleteAll（）"
		 		+ "删除所有的document、deleteDocuments（"
		 		+ "Query… queries）删除多个查询出来的document，"
		 		+ "deleteDocuments（Query query）删除query查询出来的document等等"
		 		+ "，但用Indexwriter执行删除的话一定要进行关闭，否则删除不会立马生效");
		 Class<? extends Object> clazz = wz.getClass();
       	java.lang.reflect.Field[] reflectFields = clazz.getDeclaredFields();//获得对象所有属性
     	java.lang.reflect.Field.setAccessible(reflectFields,true); 
     	for(int i=0;i<reflectFields.length;i++){
     		reflectFields[i].setAccessible(true);
     		System.out.println(reflectFields[i].getName());
     		try {
				System.out.println(reflectFields[i].get(wz).toString());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
		 
	}
}
