package com.zqy.sqllucene.sqlparser;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.merge.Merge;

import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.Table;

public class CreateParser extends BaseParser{
	private String tableName;
	private List<Column> columns;
	private String dataBaseName;
	private CreateParser(){
		
	}
	private static final CreateParser single = new CreateParser();  
    //静态工厂方法   单例
    public static CreateParser getInstance() {  
        return single;  
    }
	public Table createTableParser(String dataBaseName,String sql){
		 this.dataBaseName = dataBaseName;
		 return createTableParser(sql);
	    }
    public Table createTableParser(String sql){
    	try {
			CreateTable createStatement = (CreateTable) CCJSqlParserUtil.parse(sql);
			init();
			createStatement.accept(this);
			Table table = new Table();
			table.setName(tableName);
			table.setColumns(columns);
			table.setDataBaseName(dataBaseName);
			return table;
		} catch (JSQLParserException e) {
			e.printStackTrace();
		}
		return null;
    	
    }
    private void init(){
	   columns=new ArrayList<Column>();
    }
	
	@Override
	public void visit(CreateTable createTable) {
		// TODO Auto-generated method stub
		if( dataBaseName==null || "".equals(dataBaseName)){
			String FullyQualifiedName=createTable.getTable().getFullyQualifiedName();
			dataBaseName = FullyQualifiedName.substring(0, FullyQualifiedName.indexOf("."));
			tableName = FullyQualifiedName.substring(FullyQualifiedName.indexOf(".")+1,FullyQualifiedName.length());
		}else{
			tableName = createTable.getTable().getName();
		}
		List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
		for(ColumnDefinition columnDefinition:columnDefinitions){
			Column column = new Column();
			column.setName(columnDefinition.getColumnName());
			column.setType(columnDefinition.getColDataType().getDataType());
			columns.add(column);
		}
	}
	
	@Override
	public void visit(SetStatement set) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Merge merge) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		CreateParser createParser = new CreateParser();
		createParser.createTableParser("create table database.table1 (abc int)");
	
	}
}
