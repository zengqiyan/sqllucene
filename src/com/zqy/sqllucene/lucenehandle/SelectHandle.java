package com.zqy.sqllucene.lucenehandle;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.pojo.SelectBox;
import com.zqy.sqllucene.sqlparser.SelectParser;


public class SelectHandle {
  public  List select(String dataBaseName,String sql){
   	SelectBox selectBox =  SelectParser.getInstance().selectParser(sql);
   	QueryHandle queryHandle = new QueryHandle();
   	BooleanQuery booleanQuery=null;
   	List<String[]> tables=null;
   	if(selectBox.getTables()!=null){
    		tables = selectBox.getTables();
    		String[] table = new String[tables.size()];
    		for(int i=0;i<tables.size();i++){
    			table[i]=tables.get(i)[0];
    		}
    		queryHandle.config(dataBaseName,table);
    	}
   	
   	if(selectBox.getQueryColumns()!=null){
   		if(!selectBox.getQueryColumns().get(0).equals("*")){
   			String[] queryColumns = selectBox.getQueryColumns().toArray(new String[selectBox.getQueryColumns().size()]);
   			queryHandle.setQueryColumns(queryColumns);
   		}
   		
   	 }
    if(selectBox.getWheres()!=null){
       	booleanQuery = whereHandle(queryHandle,selectBox.getWheres());
   	}
       Sort sort=null;
       if(selectBox.getOrderBys()!=null){
       	List<String> orderBys = selectBox.getOrderBys();
       	String[] orderByColumns= new String[orderBys.size()];
       	Boolean[] bools= new Boolean[orderBys.size()];
       	for(int i=0;i<orderBys.size();i++){
       		String orderBy = orderBys.get(i);
       		if(orderBys.get(i).contains("desc")){
       			String orderByColumn = orderBy.substring(0,orderBy.lastIndexOf("desc")).trim();
       			orderByColumns[i]=orderByColumn;
       			bools[i]=true;
       		}else if(orderBys.get(i).contains("asc")){
       			String orderByColumn = orderBy.substring(0,orderBy.lastIndexOf("asc")).trim();
       			orderByColumns[i]=orderByColumn;
       			bools[i]=false;
       		}else{
       			String orderByColumn = orderBy.trim();
       			orderByColumns[i]=orderByColumn;
       			bools[i]=false;
       		}
       	}
       	sort=queryHandle.getSort(orderByColumns, bools);
   	}
       int offset =0;
       int rowCount =0;
       if(selectBox.getLimitOffset()!=null && selectBox.getLimitRowCount()!=null){
       	 offset =selectBox.getLimitOffset();
       	 rowCount =selectBox.getLimitRowCount();
   	}
		return  queryHandle.getResult(booleanQuery, sort, offset, rowCount);
   	 
   }
   private  BooleanQuery whereHandle(QueryHandle queryHandle,LinkedList linkList){
   	BooleanQuery booleanQuery = new BooleanQuery();
		for(int i=0;i<linkList.size();i++){
			Object object = linkList.get(i);
			if(object instanceof String){
				System.out.println(object);
				if(i+1<=linkList.size()){
					 Object node = linkList.get(i+1);
                     orandExpressionHandle(queryHandle, booleanQuery, node, object.toString());
				}
				if(i==1){
					 Object node = linkList.get(i-1);
					 orandExpressionHandle(queryHandle, booleanQuery, node, object.toString());
				}
				}
			if(linkList.size()==1){
				 Object node = linkList.get(i);
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
			//System.out.println("%ddscds%".replaceAll("%.+%", "111sd"));
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
    	SelectHandle selectHandle = new SelectHandle();
    	String sql = "select * from book where  bookname like '编程'  order by price desc limit 1,10";
    	String sql1 = "select * from book";
    	List list = selectHandle.select("testDatabase", sql);
     	list.forEach(x->{System.out.println(x);System.out.println("-----");});
    }
   }
