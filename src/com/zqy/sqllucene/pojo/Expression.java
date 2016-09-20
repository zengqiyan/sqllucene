package com.zqy.sqllucene.pojo;

public class Expression {
    private int id;
    private int parentId;
    private String expressionName;
    private String type;
    private String value;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getExpressionName() {
		return expressionName;
	}
	public void setExpressionName(String expressionName) {
		this.expressionName = expressionName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setExpression(int id,int parentId,String expressionName,String type,
			          String value ){
		setId(id);
		setParentId(parentId);
		setExpressionName(expressionName);
		setType(type);
		setValue(value);
	}
   }
