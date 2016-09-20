package com.zqy.sqllucene.pojo;

import java.io.Serializable;

public class Field implements Serializable{
     /**
	 * 
	 */
	private static final long serialVersionUID = 7877606188261257924L;
	private Object value;
	private String type;
	private String columnName;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
