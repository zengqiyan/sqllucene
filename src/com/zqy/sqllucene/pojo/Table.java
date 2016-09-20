package com.zqy.sqllucene.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class Table implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8101974715457377296L;
	private String name;
	private String path;
	private String dataBaseName;
	private List<Column> columns;
	/*public Table(String name,String path,String dataBaseName,List<Column> columns){
		setName(name);
		setPath(path);
		setDataBaseName(dataBaseName);
		setColumns(columns);
	}*/
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
	public List<Column> getColumns() {
		return columns;
	}
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	public String getDataBaseName() {
		return dataBaseName;
	}
	public void setDataBaseName(String dataBaseName) {
		this.dataBaseName = dataBaseName;
	}
	
    
}
