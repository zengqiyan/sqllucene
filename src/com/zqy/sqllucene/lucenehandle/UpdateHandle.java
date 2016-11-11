package com.zqy.sqllucene.lucenehandle;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.pojo.DataBase;
import com.zqy.sqllucene.pojo.Field;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.pojo.UpdateBox;
import com.zqy.sqllucene.sqlparser.UpdateParser;
import com.zqy.sqllucene.util.LogUtil;

public class UpdateHandle {
	 public void update(String sql){
		   UpdateBox updateBox =  UpdateParser.getInstance().updateParser(sql);
		   	Term term =null;
		   	String dataBaseName = UpdateParser.getInstance().getDataBaseName();
		   	String tableName=UpdateParser.getInstance().getTableName();
		   	//处理where
		   	ObjectExpression equalExpression = updateBox.getEqualExpression();
		    if(equalExpression!=null){
		    	term = new Term(equalExpression.getColumnname(),equalExpression.getValue().toString());
		   	}
		    List<Field> fields = updateBox.getFields();
		    update(dataBaseName,tableName,fields,term);
	 }
	 public void update(String dataBaseName,String sql){
		    UpdateBox updateBox =  UpdateParser.getInstance().updateParser(dataBaseName,sql);
		   	Term term =null;
		   	String tableName=UpdateParser.getInstance().getTableName();
		   	//处理where
		   	ObjectExpression equalExpression = updateBox.getEqualExpression();
		    if(equalExpression!=null){
		    	term = new Term(equalExpression.getColumnname(),equalExpression.getValue().toString());
		   	}
		    List<Field> fields = updateBox.getFields();
		    update(dataBaseName,tableName,fields,term);
	 }
	 public void update(String dataBaseName,String tableName,List<Field> fields,Term term){
	        IndexWriter writer = null;
	        try{
	        	LogUtil.printLog("更新数据开始...");
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
	            IndexSearcher indexSearcher = getSearcher(dataBaseName, tableName);
	            TermQuery termQuery = new TermQuery(term);
	            TopDocs topDocs = indexSearcher.search(termQuery, Integer.MAX_VALUE);
	            ScoreDoc[] docs = topDocs.scoreDocs;
	            for(ScoreDoc scoreDoc: docs){
	            	  int documentId = scoreDoc.doc;
	                  Document document = getSearcher(dataBaseName, tableName).doc(documentId);
	                  for(Field field:fields){
	                	  document.removeField(field.getColumnName());
	                	  document.add(getField(field.getColumnName(), field.getType(), field.getValue()));  
	 	            }
	                  writer.updateDocument(term, document);
	            }
	            writer.commit();
	            LogUtil.printLog("更新数据成功...");
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
	  private DataBaseDefaultConfig dataBaseDefaultConfig = DataBaseDefaultConfig.getInstance();
	  private IndexSearcher getSearcher(String dataBaseName,String[] tableNames) {
	        IndexReader indexReader = null;
	        IndexReader[] indexReaders = new IndexReader[tableNames.length];
				try {
					for(int i=0;i<tableNames.length;i++){
						indexReader = DirectoryReader.
								open(FSDirectory.open(new File(dataBaseDefaultConfig.
										getTablePath(dataBaseName, tableNames[i]))));
						indexReaders[i]=indexReader;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        return new IndexSearcher(new MultiReader(indexReaders));
	    }
		 private IndexSearcher getSearcher(String dataBaseName,String tableName) {
	        IndexReader indexReader = null;
				try {
					indexReader = DirectoryReader.
							open(FSDirectory.open(new File(dataBaseDefaultConfig.
									getTablePath(dataBaseName, tableName))));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        return new IndexSearcher(indexReader);
	    }
	    public static void main(String[] args) {
	    	UpdateHandle updateHandle = new UpdateHandle();
	    	String sql2 = "update ry set xbm='2',xm='秦寿生'  where ryid = '440111109035'";
	    	updateHandle.update("testDatabase", sql2);
	    }
		   

}
