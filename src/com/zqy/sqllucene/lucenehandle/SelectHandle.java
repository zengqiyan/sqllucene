package com.zqy.sqllucene.lucenehandle;

import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
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
   		String[] queryColumns = (String[]) (selectBox.getQueryColumns()).toArray();
   		queryHandle.setQueryColumns(queryColumns);
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
       int rowCount =Integer.MAX_VALUE;
       if(selectBox.getLimitOffset()!=null && selectBox.getLimitRowCount()!=null){
       	 offset =selectBox.getLimitOffset();
       	 rowCount =selectBox.getLimitRowCount();
   	}
		return  queryHandle.getResult(booleanQuery, sort, offset, rowCount);
   	 
   }
   public  BooleanQuery whereHandle(QueryHandle queryHandle,LinkedList linkList){
   	BooleanQuery booleanQuery = new BooleanQuery();
		for(int i=0;i<linkList.size();i++){
			Object object = linkList.get(i);
			if(object instanceof String){
				System.out.println(object);
				if(i+1<=linkList.size()){
					    object = linkList.get(i+1);
						if(object instanceof ObjectExpression){
							ObjectExpression ob = (ObjectExpression)object;
							System.out.println(ob.getColumnname()+" "+ob.getExp()+" "+ob.getValue());
							Occur occur=null;
							if(ob.getExp().toLowerCase().equals("and")){
								occur = Occur.MUST;
							}else if(ob.getExp().toLowerCase().equals("or")){
								occur = Occur.SHOULD;
							}
							booleanQuery.add(queryHandle.termQuery(ob.getColumnname(), ob.getValue()),occur);
						}
						if(object instanceof LinkedList){
							System.out.println("(");
							Occur occur=null;
							if(((String) object).toLowerCase().equals("and")){
								occur = Occur.MUST;
							}else if(((String) object).toLowerCase().equals("or")){
								occur = Occur.SHOULD;
							}
							booleanQuery.add(whereHandle(queryHandle,(LinkedList)object), occur);
							System.out.println(")");
						}
					}
				}
			if(i==0){
				if(object instanceof LinkedList){
					System.out.println("(");
					booleanQuery.add(whereHandle(queryHandle,(LinkedList)object), Occur.MUST);
					System.out.println(")");
				}
				if(object instanceof ObjectExpression){
					ObjectExpression ob = (ObjectExpression)object;
					if(ob.getExp().equals("=")){
						booleanQuery.add(queryHandle.termQuery(ob.getColumnname(), ob.getValue()), Occur.MUST);
					}
					System.out.println(ob.getColumnname()+" "+ob.getExp()+" "+ob.getValue());
				}
			}
//			System.out.println(object);
		}
		return booleanQuery;
	}
  
   }
