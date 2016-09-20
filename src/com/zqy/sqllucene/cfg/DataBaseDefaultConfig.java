package com.zqy.sqllucene.cfg;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;














import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.xml.sax.SAXException;

import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.DataBase;
import com.zqy.sqllucene.pojo.Table;
import com.zqy.sqllucene.util.XmlUtil;

public class DataBaseDefaultConfig implements DataBaseConfiguration{
    private static final String DATABASE_CFG="lucene_database.cfg.xml";
    /*
	 * 初始化配置文件
	 */
     
	private DataBaseDefaultConfig(){		
		
	}
	public Document load(){
		return load(DATABASE_CFG);
		
	}
	public Document load(String dir){
		Document document = null;
		SAXReader reader = new SAXReader();              
	    try {
	    	InputStream in = this.getClass().getClassLoader().getResourceAsStream(dir);
	    	 this.getClass().getClassLoader().getResource(DATABASE_CFG);
		    document = reader.read(in);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return document; 
		
	}
	
	public void write(Document document) throws DocumentException{
		OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");    // 指定XML编码        
        XMLWriter writer;
		try {
			String fileName=this.getClass().getClassLoader().getResource(DATABASE_CFG).getFile();
			writer = new XMLWriter(new FileWriter(fileName),format);
			writer.write(document);
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public DataBase getDataBaseByName(String dataBaseName){
		Document document = load();
        Element lucene_database = document.getRootElement();
		Element databaseElement  = XmlUtil.parse(lucene_database,"database","name", dataBaseName);
		if(databaseElement==null){
			throw new RuntimeException("数据库不存在！");
		}
		DataBase dataBase= new DataBase();
		dataBase.setName(dataBaseName);
		String dataBasePath = databaseElement.attributeValue("path");
		dataBase.setPath(dataBasePath);
		return dataBase;
	}
	public Table getTable(String dataBaseName,String tableName){
		Document document = load();
        Element lucene_database = document.getRootElement();
		Element databaseElement  = XmlUtil.parse(lucene_database,"database","name", dataBaseName);
		if(databaseElement==null){
			throw new RuntimeException("数据库不存在！");
		}
		Element tableElement  = XmlUtil.parse(databaseElement,"table","name",tableName);
		if(tableElement==null){
			throw new RuntimeException("表不存在！");
		}
		Table table= new Table();
		table.setName(tableName);
		String tablePath = tableElement.attributeValue("path");
		table.setPath(tablePath);
		return table;
	}
	public List<Table> getTables(String dataBaseName,String... tableNames){
		Document document = load();
        Element lucene_database = document.getRootElement();
		Element databaseElement  = XmlUtil.parse(lucene_database,"database","name", dataBaseName);
		if(databaseElement==null){
			throw new RuntimeException("数据库不存在！");
		}
		List<Table> tables = new ArrayList<Table>();
		for(int i=0;i<tableNames.length;i++){
			Element tableElement  = XmlUtil.parse(databaseElement,"table","name",tableNames[i]);
			if(tableElement==null){
				throw new RuntimeException("表不存在！");
			}
			Table table= new Table();
			table.setName(tableNames[i]);
			String tablePath = tableElement.attributeValue("path");
			table.setPath(tablePath);
			tables.add(table);
		}
		return tables;
	}
	public String getTablePath(String dataBaseName,String tableName){
		Document document = load();
        Element lucene_database = document.getRootElement();
		Element databaseElement  = XmlUtil.parse(lucene_database,"database","name", dataBaseName);
		if(databaseElement==null){
			throw new RuntimeException("数据库不存在！");
		}
		Element tableElement  = XmlUtil.parse(databaseElement,"table","name",tableName);
		if(tableElement==null){
			throw new RuntimeException("表不存在！");
		}
		String tablePath = tableElement.attributeValue("path");
		System.out.println(tablePath);
		return tablePath;
	}
	public List<Table> getTables(String dataBaseName){
		return null;
	}
	public List<Column> getColumns(String dataBaseName,String tableName,List<String> columnNames){
		List<Column> columns = null;
    	try {
		Document document=load();
		Element lucene_database = document.getRootElement();
		
		Element database  = XmlUtil.parse(lucene_database,"database","name", dataBaseName);
		if(database==null){
			throw new RuntimeException("数据库不存在！");
		}
		Element tableElement  = XmlUtil.parse(database,"table","name",tableName);
		if(tableElement==null){
			throw new RuntimeException("表不存在！");
		}
		List<Element> columnElements = tableElement.elements("column");
	    columns = new ArrayList<Column>();
	    boolean flag = false;
	    if(columnNames!=null && columnNames.size()>0){
	    	flag=true;
	    }
		for(Element columnElement:columnElements){
			if(flag){
				for(int j=0;j<columnNames.size();j++){
					if(columnElement.attributeValue("name").equals(columnNames.get(j))){
						Column column = new Column();
						column.setName(columnElement.attributeValue("name"));
						column.setType(columnElement.attributeValue("type"));
						columns.add(column);
						columnNames.remove(j);
						break;
					}
				}
			}else{
				Column column = new Column();
				column.setName(columnElement.attributeValue("name"));
				column.setType(columnElement.attributeValue("type"));
				columns.add(column);
			}
		}
		DataBaseDefaultConfig.getInstance().write(document);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return columns;
		
	}
	public List<Column> getColumns(String dataBaseName,String tableName){
		return getColumns( dataBaseName, tableName,null);
	}
	public Analyzer getAnalyzer(){
		return new IKAnalyzer(true);
	}
	
	private static final DataBaseDefaultConfig single = new DataBaseDefaultConfig();  
    //静态工厂方法   单例
    public static DataBaseDefaultConfig getInstance() {  
        return single;  
    }
	public static void main(String[] args) {
		List<Element> databases=null;
		System.out.println(databases!=null && !databases.isEmpty());
	}
}
