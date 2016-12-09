package com.zqy.sqllucene.lucenehandle;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.DataBase;
import com.zqy.sqllucene.pojo.Table;
import com.zqy.sqllucene.sqlparser.CreateParser;
import com.zqy.sqllucene.util.XmlUtil;

public class CreateHandle {
	
    public void createTable(String sql){
    	Table table =  CreateParser.getInstance().createTableParser(sql);
    	createTable(table);
	}
    public void createTable(String dataBaseName,String sql){
    	Table table =  CreateParser.getInstance().createTableParser(dataBaseName,sql);
    	createTable(table);
	}
    public void createDataBase(DataBase database){
    	try {
    	Document document= DataBaseDefaultConfig.getInstance().load();
		Element lucene_database = document.getRootElement();
		List<Element> databaseElements = lucene_database.elements("database");
		String databaseName = database.getName();
		String databasePath = database.getPath();
		if(databaseElements!=null && !databaseElements.isEmpty()){
			for(Element element: databaseElements){
				if(element.attribute("name").getText().equals(databaseName)){
					throw new RuntimeException("数据库名称已存在");
				}
			}
			for(Element element: databaseElements){
				if(element.attribute("path").getText().equals(databasePath)){
					throw new RuntimeException("数据库路径已存在");
				}
			}
		}
		Element newDataBase = lucene_database.addElement("database");
		newDataBase.addAttribute("name", databaseName);
		newDataBase.addAttribute("path", databasePath);
		File dataBaseFile = new File(databasePath);
		if(!dataBaseFile.exists()){
			dataBaseFile.mkdirs();
		}
		DataBaseDefaultConfig.getInstance().write(document);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
    public void createTable(Table table){
    	try {
		Document document= DataBaseDefaultConfig.getInstance().load();
		Element lucene_database = document.getRootElement();
		String databaseName = table.getDataBaseName();
		Element database  = XmlUtil.parse(lucene_database,"database","name", databaseName);
		if(database==null){
			throw new RuntimeException("数据库不存在！");
		}
		List<Element> tableElements = database.elements("table");
		String databasePath = database.attributeValue("path");
		String tableName = table.getName();
		if(tableElements!=null && !tableElements.isEmpty()){
			for(Element element: tableElements){
				if(element.attribute("name").getText().equals(tableName)){
					throw new RuntimeException("表名称已存在");
				}
			}
		}
		Element newTable = database.addElement("table");
		newTable.addAttribute("name", tableName);
		newTable.addAttribute("path", databasePath+"/"+tableName);
		for(Column column:table.getColumns()){
			Element columnElement = newTable.addElement("column");
			columnElement.addAttribute("name", column.getName());
			columnElement.addAttribute("type", column.getType());
		}
		File tableFile = new File(databasePath+"/"+tableName);
		if(!tableFile.exists()){
			tableFile.mkdirs();
		}
		DataBaseDefaultConfig.getInstance().write(document);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
   
    public static void main(String[] args) {
    	CreateHandle createHandle= new CreateHandle();
    	/*DataBase database = new DataBase();
    	database.setName("testDatabase");
    	database.setPath("d:/test/testDatabase");
    	createHandle.createDataBase(database);*/
    	createHandle.createTable("create table testDatabase.novel(id string,name string,content string,size string)");
    	
	}
}
