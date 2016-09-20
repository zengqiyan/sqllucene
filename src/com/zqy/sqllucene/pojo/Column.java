package com.zqy.sqllucene.pojo;

import java.io.Serializable;

public class Column implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -483578955317877215L;
    private String name;
	private String type;
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
