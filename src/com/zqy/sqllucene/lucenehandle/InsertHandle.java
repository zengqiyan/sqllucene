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

import test.Wz;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.DataBase;
import com.zqy.sqllucene.pojo.Field;
import com.zqy.sqllucene.sqlparser.InsertParser;
import com.zqy.sqllucene.util.CommonUtil;

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
	            writer.commit();
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
	        		System.out.println(reflectFields[i].getName());
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
		 //insertHandle.insert("testDatabase", "insert into testTable values(1002,'mytitle3','注：注：kclbm、kcid、nrid三个参数只需传其中一个，若传递多个参数优先级是：nrid > kcid > kclbmkclbm、kcid、nrid三个参数只需传其中一个，若传递多个参数优先级是：nrid > kcid > kclbm')");
		 Wz wz = new Wz();
		 wz.setId(1007);
		 wz.setTitle("1007");
		 wz.setContent("IndexWriter可以根据多种情况进行删除deleteAll（）删除所有的document、deleteDocuments（Query… queries）删除多个查询出来的document，deleteDocuments（Query query）删除query查询出来的document等等，但用Indexwriter执行删除的话一定要进行关闭，否则删除不会立马生效");
		 insertHandle.insert("testDatabase", "testTable",wz);
	}
}