package com.zqy.sqllucene.pojo;

import java.io.Serializable;
import java.util.Set;

public class DataBase implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8857894264214516020L;
    private String name;
    private String path;
   
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
