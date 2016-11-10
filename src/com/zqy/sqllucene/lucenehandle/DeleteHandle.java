package com.zqy.sqllucene.lucenehandle;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.pojo.DataBase;
import com.zqy.sqllucene.pojo.DeleteBox;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.sqlparser.DeleteParser;
import com.zqy.sqllucene.util.LogUtil;

public class DeleteHandle {
	 public void delete(String dataBaseName,String tableName,Query query){
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
	            if(query!=null){
	            	  writer.deleteDocuments(query);
	            }else{
	            	  writer.deleteAll();
	            }
	            writer.commit();
	            LogUtil.printLog("删除数据成功...");
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
	  public  void delete(String dataBaseName,String sql){
		  DeleteBox deleteBox =  DeleteParser.getInstance().deleteParser(sql);
		   	QueryHandle queryHandle = new QueryHandle();
		   	BooleanQuery booleanQuery=null;
		   	String tableName=null;
		   	//处理table
		   	if(deleteBox.getTables()!=null){
		   		tableName = deleteBox.getTables().get(0)[0];
		    	}
		   	//处理where
		    if(deleteBox.getWheres()!=null){
		       	booleanQuery = whereHandle(queryHandle,deleteBox.getWheres());
		   	}
		    delete(dataBaseName, tableName, booleanQuery);
		   }
		   /**
		    * where子句处理器
		    * @param queryHandle
		    * @param linkList
		    * @return
		    */
		   private  BooleanQuery whereHandle(QueryHandle queryHandle,LinkedList wheres){
		   	BooleanQuery booleanQuery = new BooleanQuery();
				for(int i=0;i<wheres.size();i++){
					Object object = wheres.get(i);
					if(object instanceof String){
						System.out.println(object);
						if(i+1<=wheres.size()){
							 Object node = wheres.get(i+1);
		                     orandExpressionHandle(queryHandle, booleanQuery, node, object.toString());
						}
						if(i==1){
							 Object node = wheres.get(i-1);
							 orandExpressionHandle(queryHandle, booleanQuery, node, object.toString());
						}
						}
					if(wheres.size()==1){
						 Object node = wheres.get(i);
						 orandExpressionHandle(queryHandle, booleanQuery, node,null);
					}
				}
				return booleanQuery;
			}
		    private void orandExpressionHandle(QueryHandle queryHandle,BooleanQuery booleanQuery,Object node,String orand){
		    	if(orand==null)orand="and";
				if(node instanceof ObjectExpression){
					ObjectExpression objectExpression = (ObjectExpression)node;
					System.out.println(objectExpression.getColumnname()+" "+objectExpression.getExp()+" "+objectExpression.getValue());
					Occur occur=null;
					if(orand.toLowerCase().equals("and")){
						occur = Occur.MUST;
					}else if(orand.toLowerCase().equals("or")){
						occur = Occur.SHOULD;
					}
					booleanQuery.add(objectExpressionHandle(queryHandle, objectExpression),occur);
				}
				if(node instanceof LinkedList){
					System.out.println("(");
					Occur occur=null;
					if(orand.toLowerCase().equals("and")){
						occur = Occur.MUST;
					}else if(orand.toLowerCase().equals("or")){
						occur = Occur.SHOULD;
					}
					booleanQuery.add(whereHandle(queryHandle,(LinkedList)node), occur);
					System.out.println(")");
				}
		    }
		    private Query objectExpressionHandle(QueryHandle queryHandle,ObjectExpression objectExpression){
		    	Query query =null;
		    	switch (objectExpression.getExp().toLowerCase()) {
				case "=":
					query = queryHandle.termQuery(objectExpression.getColumnname(), objectExpression.getValue());
					break;
				case "like":
					query = likeHandle(queryHandle,objectExpression.getColumnname(),objectExpression.getValue().toString());
					break;
				case "in":
					if(objectExpression.getValue().toString().matches("\\((.+)\\)")){
						Pattern p = Pattern.compile("\\((.+)\\)");  
						Matcher m = p.matcher(objectExpression.getValue().toString());  
						while(m.find()){  
						System.out.println(m.group(1));
						BooleanQuery booleanQuery = new BooleanQuery();
						String[] likeStrs = m.group(1).split(",");
						for (int i = 0; i < likeStrs.length; i++) {
							 booleanQuery.add(queryHandle.termQuery(objectExpression.getColumnname(), likeStrs[i]), Occur.MUST); 
						}
						query = booleanQuery;
						}
					}
					break;
				default:
					break;
				}
				return query;
		    }
		    private Query likeHandle(QueryHandle queryHandle,String columnName,String likeStr){
		    	Query query=null;
				if(likeStr.matches("%(.+)%")){
					Pattern p = Pattern.compile("%(.+)%");  
					Matcher m = p.matcher(likeStr);  
					while(m.find()){  
					query = queryHandle.wildcardQuery(columnName, "*"+m.group(1)+"*");
					}
				}else if(likeStr.matches("%(.+)")){
					Pattern p = Pattern.compile("%(.+)");  
					Matcher m = p.matcher(likeStr);  
					while(m.find()){  
					query = queryHandle.prefixQuery(columnName, m.group(1));
					}
				}else if(likeStr.matches("(.+)%")){
					Pattern p = Pattern.compile("(.+)%");  
					Matcher m = p.matcher(likeStr);  
					while(m.find()){  
						query = queryHandle.wildcardQuery(columnName, m.group(1)+"*");  
					}
				}else if(likeStr.matches("\\((.+)\\)")){
					Pattern p = Pattern.compile("\\((.+)\\)");  
					Matcher m = p.matcher(likeStr);  
					while(m.find()){  
					System.out.println(m.group(1));
					BooleanQuery booleanQuery = new BooleanQuery();
					String[] likeStrs = m.group(1).split(",");
					for (int i = 0; i < likeStrs.length; i++) {
						 query = likeHandle(queryHandle,columnName,likeStrs[i]);
						 booleanQuery.add(query, Occur.MUST); 
					}
					query = booleanQuery;
					}
				}else{
					query = queryHandle.fuzzyQuery(columnName, likeStr, 0);
				}
				return query;
			
		    }
		    public static void main(String[] args) {
		    	DeleteHandle deleteHandle = new DeleteHandle();
		    	String sql2 = "delete from ry where ryid = '440111108978'";
		    	deleteHandle.delete("testDatabase", sql2);
		    }
		   }
