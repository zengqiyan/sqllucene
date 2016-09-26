package com.zqy.sqllucene.lucenehandle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import test.Book;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.DataBase;
import com.zqy.sqllucene.pojo.Field;
import com.zqy.sqllucene.sqlparser.InsertParser;
import com.zqy.sqllucene.util.CommonUtil;
import com.zqy.sqllucene.util.LogUtil;

public class InsertHandle {
	 public void insert(String sql){
		    List<Field> fields =  InsertParser.getInstance().insertParser(sql);
	    	insert(InsertParser.getInstance().getDataBaseName(),InsertParser.getInstance().getTableName(),fields);
		}
	 public void insert(String dataBaseName,String sql){
		 List<Field> fields =  InsertParser.getInstance().insertParser(dataBaseName,sql);
		 insert(dataBaseName,InsertParser.getInstance().getTableName(),fields);
		}
	 public void insert(String dataBaseName,String tableName,List<Field> fields){
	        IndexWriter writer = null;
	        try{
	        	LogUtil.printLog("插入数据开始...");
	        	LogUtil.printLog("数据库："+dataBaseName);
	        	LogUtil.printLog("表："+tableName);
	            //1、创建Directory
	        	DataBase dataBase = DataBaseDefaultConfig.getInstance().getDataBaseByName(dataBaseName);
	        	String tablePath = dataBase.getPath()+"/"+tableName;
	        	File tableFile = new File(tablePath);
	        	if(!tableFile.exists()){
	        		throw new RuntimeException("表不存在！");
	        	}
	        	Directory directory =  FSDirectory.open(tableFile);//在硬盘上生成Directory;
	            //2、创建IndexWriter
	        	Analyzer analyzer = DataBaseDefaultConfig.getInstance().getAnalyzer();
	            IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_46,analyzer);
	            iwConfig.setMaxBufferedDocs(100);  
	            writer = new IndexWriter(directory, iwConfig);
	            //3、创建document对象
	            //4、为document添加field对象
	            //writer.deleteAll();
	            Document doc = new Document();  
	            for(Field field:fields){
	                //5、通过IndexWriter添加文档到索引中
	                 doc.add(getField(field.getColumnName(), field.getType(), field.getValue()));  
	            }
	            writer.addDocument(doc);
	            writer.commit();
	            LogUtil.printLog("插入数据成功...");
	        }catch(Exception e){
	            e.printStackTrace();
	        }finally{
	            //6、使用完成后需要将writer进行关闭
	            try {
	                writer.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	 }
	 public void insert(String dataBaseName,String tableName,Object data){
	        IndexWriter writer = null;
	        try{
	        	LogUtil.printLog("插入数据开始...");
	        	LogUtil.printLog("数据库："+dataBaseName);
	        	LogUtil.printLog("表："+tableName);
	            //1、创建Directory
	        	DataBase dataBase = DataBaseDefaultConfig.getInstance().getDataBaseByName(dataBaseName);
	        	String tablePath = dataBase.getPath()+"/"+tableName;
	        	File tableFile = new File(tablePath);
	        	if(!tableFile.exists()){
	        		throw new RuntimeException("表不存在！");
	        	}
	          	java.lang.reflect.Field[] reflectFields = data.getClass().getDeclaredFields();//获得对象所有属性
	        	java.lang.reflect.Field.setAccessible(reflectFields,true); 
	        	List<Column> columnList = 
	        			DataBaseDefaultConfig.getInstance().getColumns(dataBaseName, tableName);
	        	List<Field> fields= new ArrayList<Field>();
	        	for(int i=0;i<reflectFields.length;i++){
	        		for(int j=0;j<columnList.size();j++){
	        			Column column = columnList.get(j);
	        			if(column.getName().equals(reflectFields[i].getName())){
	        				Field field = new Field();
	        				field.setColumnName(column.getName());
	        				field.setType(column.getType());
	        				field.setValue(reflectFields[i].get(data));
	        				fields.add(field);
	        				columnList.remove(j);
	        				break;
	        			}
	        		}
	    		}
	        	Directory directory =  FSDirectory.open(tableFile);//在硬盘上生成Directory;
	            //2、创建IndexWriter
	        	Analyzer analyzer = DataBaseDefaultConfig.getInstance().getAnalyzer();
	            IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_46,analyzer);
	            iwConfig.setMaxBufferedDocs(100);  
	            writer = new IndexWriter(directory, iwConfig);
	            //3、创建document对象
	            //4、为document添加field对象
	            //writer.deleteAll();
	            Document doc = new Document();
	            for(Field field:fields){
	                //5、通过IndexWriter添加文档到索引中
	                 doc.add(getField(field.getColumnName(), field.getType(), field.getValue()));  
	                 // 添加到索引中去  
	            }
	            writer.addDocument(doc);
	            writer.commit();
	            LogUtil.printLog("插入数据成功...");
	        }catch(Exception e){
	            e.printStackTrace();
	        }finally{
	            //6、使用完成后需要将writer进行关闭
	            try {
	                writer.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	 }
	 public org.apache.lucene.document.Field getField(String columnName,String type,Object value){
		 if(type.equals("string")){
			 return new TextField(columnName,value.toString(),org.apache.lucene.document.Field.Store.YES);
		 }
		 if(type.equals("long")){
			 return new LongField(columnName, (Long) value,org.apache.lucene.document.Field.Store.YES);
		 }
		 if(type.equals("date")){
			 return new LongField(columnName, (Long) value,org.apache.lucene.document.Field.Store.YES);
		 }
		 if(type.equals("double")){
			 return new DoubleField(columnName,(Double) value,org.apache.lucene.document.Field.Store.YES);
		 }
		 if(type.equals("int")){
			 return new IntField(columnName,(Integer) value,org.apache.lucene.document.Field.Store.YES);
		 }
		return null;
		 
	 }
	
	 public static void main(String[] args) {
		 InsertHandle insertHandle= new InsertHandle();
		 //insertHandle.insert("testDatabase", "insert into testTable(id,title,content) values(1010,'mytitle5','查询正则')");
		 Book book = new Book(2,"西游记","xyj","小说",62.73,System.currentTimeMillis());
		 Book book1 = new Book(10,"七天七数据库","qtqsjk","技术",96.23,System.currentTimeMillis());
		 Book book2 = new Book(4,"编程,编程","bcbc","社会",10.37,System.currentTimeMillis());
		 Book book3 = new Book(5,"看清本质","kqbz","社会",10.37,System.currentTimeMillis());
		 Book book4 = new Book(6,"数据库实战","sjksz","技术",77.13,System.currentTimeMillis());
		 Book book5 = new Book(7,"编程宝典","bcbd","技术",100.3,System.currentTimeMillis());
		 Book book6= new Book(8,"职场关系论","zcgxl","职场",52.23,System.currentTimeMillis());
		 Book book7= new Book(9,"健康生活","jksh","生活",20.47,System.currentTimeMillis());
		 List<Book> list = new ArrayList<Book>();
		 list.add(book);
		 list.add(book2);
		 list.add(book3);
		 list.add(book4);
		 list.add(book5);
		 list.add(book6);
		 list.add(book7);
		 //list.forEach(l->insertHandle.insert("testDatabase", "book",l));
		 insertHandle.insert("testDatabase", "book",book1);
	}
}
