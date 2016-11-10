package com.zqy.sqllucene.pojo;


import java.util.LinkedList;
import java.util.List;

public class UpdateBox {
	private List<String[]> tables;
	private LinkedList wheres;
	private List<Field> fields;
	private ObjectExpression equalExpression;
	public List<String[]> getTables() {
		return tables;
	}
	public void setTables(List<String[]> tables) {
		this.tables = tables;
	}
	public LinkedList getWheres() {
		return wheres;
	}
	public void setWheres(LinkedList wheres) {
		this.wheres = wheres;
	}
	public List<Field> getFields() {
		return fields;
	}
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	public ObjectExpression getEqualExpression() {
		return equalExpression;
	}
	public void setEqualExpression(ObjectExpression equalExpression) {
		this.equalExpression = equalExpression;
	}
	
}
