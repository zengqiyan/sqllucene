package com.zqy.sqllucene.pojo;


import java.util.LinkedList;
import java.util.List;

public class DeleteBox {
	private List<String[]> tables;
	private LinkedList wheres;
	private List<String> orderBys;
	private Integer limitOffset;
	private Integer limitRowCount;
	
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
	public List<String> getOrderBys() {
		return orderBys;
	}
	public void setOrderBys(List<String> orderBys) {
		this.orderBys = orderBys;
	}
	public Integer getLimitOffset() {
		return limitOffset;
	}
	public void setLimitOffset(Integer limitOffset) {
		this.limitOffset = limitOffset;
	}
	public Integer getLimitRowCount() {
		return limitRowCount;
	}
	public void setLimitRowCount(Integer limitRowCount) {
		this.limitRowCount = limitRowCount;
	}
}
