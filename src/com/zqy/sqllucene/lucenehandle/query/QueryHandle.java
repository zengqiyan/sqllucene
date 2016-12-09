package com.zqy.sqllucene.lucenehandle.query;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.index.IndexHandle;
import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.sqlparser.SelectParser;

public class QueryHandle extends BaseQueryHandle{
	 private String dataBaseName;
	 private String[] tableNames;
	 private String tableName;
	 private String[] queryColumns;
	 private Sort sort;
	 private Boolean ishighlighter=false;
	 private SimpleHTMLFormatter simpleHTMLFormatter;
	 public void config(String dataBaseName,String[] tableNames){
		 this.dataBaseName = dataBaseName;
		 this.tableNames = tableNames;
	 }
	 public void config(String dataBaseName,String[] tableNames,String[] queryColumns){
		 this.dataBaseName = dataBaseName;
		 this.tableNames = tableNames;
		 this.queryColumns = queryColumns;
	 }
	 public void config(String dataBaseName,String tableName){
		 this.dataBaseName = dataBaseName;
		 this.tableName = tableName;
	 }
	 public void config(String dataBaseName,String tableName,String[] queryColumns){
		 this.dataBaseName = dataBaseName;
		 this.tableName = tableName;
		 this.queryColumns = queryColumns;
	 }
	public String[] getQueryColumns() {
		return queryColumns;
	}
	public void setQueryColumns(String[] queryColumns) {
		this.queryColumns = queryColumns;
	}
	
	 private DataBaseDefaultConfig dataBaseDefaultConfig = DataBaseDefaultConfig.getInstance();
	 public IndexSearcher getSearcher() {
		 if(tableNames!=null && tableNames.length>0){
			 return getSearcher(tableNames);
		 }else if(tableNames==null || tableNames.length==0){
			 if(tableName!=null)return getSearcher(tableName);
		 }
		return null;
	 }
	 private IndexSearcher getSearcher(String[] tableNames) {
        return  IndexHandle.getSearcher(dataBaseName, tableNames);
    }
	 private IndexSearcher getSearcher(String tableName) {
        return IndexHandle.getSearcher(dataBaseName, tableName);
    }
   /**
    * @Title: searchTotalRecord
    * @Description: 获取符合条件的总记录数
    * @param query
    * @return
    * @throws IOException
    */
   public static int searchTotalRecord(IndexSearcher searcher,Query query) throws IOException {
     TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
     if(topDocs == null || topDocs.scoreDocs == null || topDocs.scoreDocs.length == 0) {
       return 0;
     }
     ScoreDoc[] docs = topDocs.scoreDocs;
     return docs.length;
   }
    //true:降序
 	//false:升序
   public Sort getSort(String[] columnNames,Boolean[] bools){
		  if(columnNames.length!=bools.length){
	        	throw new RuntimeException("布尔条件与排序字段个数不符合！");
	        }
		 SortField[] sortFields = new SortField[columnNames.length];
		 List<Column> columnList = null;
		 if(tableNames!=null && tableNames.length>0){
			 if(tableNames.length==1){
				 columnList = dataBaseDefaultConfig.getColumns(dataBaseName,tableNames[0],columnNames); 
			 }else{
				 columnList = dataBaseDefaultConfig.getColumns(dataBaseName,tableNames,columnNames);
			 }
		 }else{
			 columnList = dataBaseDefaultConfig.getColumns(dataBaseName,tableName,columnNames);
		 }
		
	     for(int i=0;i<columnList.size();i++){
	    	 switch (columnList.get(i).getType()) {
	    	 	case "string":
				 sortFields[i] = new SortField(columnList.get(i).getName(), Type.STRING, bools[i]);
				 break;
	    	 	case "double":
				 sortFields[i] = new SortField(columnList.get(i).getName(), Type.DOUBLE, bools[i]);
				 break;
	    	 	case "long":
				 sortFields[i] = new SortField(columnList.get(i).getName(), Type.LONG, bools[i]);
				 break;
	    	 	case "float":
				 sortFields[i] = new SortField(columnList.get(i).getName(), Type.FLOAT, bools[i]);
				 break;
	    	 	case "int":
				 sortFields[i] = new SortField(columnList.get(i).getName(), Type.INT, bools[i]);
				 break;
			 default:
				break;
			}
	     }
	     return new Sort(sortFields);
	}
	public void setSort(Sort sort){
 		this.sort = sort;
 	}
 	public void setSort(String[] columnNames,Boolean[] bools){
 		sort = getSort(columnNames, bools);
 	}
 	public void isHighlighter(boolean ishighlighter){
 		
 	}
 	public void setHighlighter(String  prefix,String suffix){
 		 simpleHTMLFormatter = new SimpleHTMLFormatter(prefix,suffix); //如果不指定参数的话，默认是加粗，即<b><b/>
 		 ishighlighter=true;
 	}
   public List getResult(Query query,Sort sort,int offset,int rowCount){
   	   if(query==null)query=new MatchAllDocsQuery();
   	      Highlighter highlighter=null;
          if( ishighlighter==true){
        	  QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
              Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片段
              highlighter = new Highlighter(simpleHTMLFormatter, scorer);
              highlighter.setTextFragmenter(fragmenter); //设置一下要显示的片段
          }
          ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();
  		try {
  		   //int totalRecord = searchTotalRecord(getSearcher(),query);
  		  //System.out.println("共找到" + totalRecord + ":条记录");
  		  long start = System.currentTimeMillis();
  		  if(sort==null) sort = Sort.RELEVANCE;
  		  if(rowCount==0)rowCount = searchTotalRecord(getSearcher(),query);
  		  System.out.println("共找到" + rowCount + ":条记录");
  		  if(rowCount==0)return null;
          TopFieldCollector c = TopFieldCollector.create(sort, offset+rowCount, false, false, false, false);
          getSearcher().search(query, c);
          long end = System.currentTimeMillis();
          ScoreDoc[] hits = c.topDocs(offset,rowCount).scoreDocs;
          System.out.println("查询花费时间："+((end-start)));
          long start1 = System.currentTimeMillis();
          if (hits == null || hits.length < 1)return null;
          if(queryColumns==null || queryColumns.length==0){
          	     if(tableNames!=null && tableNames.length>0){
          	    	queryColumns = dataBaseDefaultConfig.getColumnNames(dataBaseName,tableNames);
          		 }else{
          			queryColumns = dataBaseDefaultConfig.getColumnNames(dataBaseName,tableName);
          		 }
          }
          for (ScoreDoc scoreDoc : hits) {
              int documentId = scoreDoc.doc;
              Document document = getSearcher().doc(documentId);
              HashMap<String,String> map = new HashMap<>();
              if(queryColumns!=null && queryColumns.length>0){
             	 for(int i=0;i<queryColumns.length;i++){
             		  String value = document.get(queryColumns[i]);
             		 if(ishighlighter==true){
             		  if(value != null) {
                           TokenStream tokenStream = dataBaseDefaultConfig.getAnalyzer().tokenStream(queryColumns[i],new StringReader(value));
                           String summary = highlighter.getBestFragment(tokenStream, value);
                           if(summary!=null){
                           	value = summary;
                           }
                       }
             		 }
             		  map.put(queryColumns[i], value);
             	 }
              }
              list.add(map);
          }
          long end1 = System.currentTimeMillis();
          System.out.println("装载查询花费时间："+((end1-start1)));
  		} catch (IOException e) {
  			e.printStackTrace();
  		} catch (InvalidTokenOffsetsException e) {
  			e.printStackTrace();
  		}
  		return list;
      }
   public List getResult(Query query,int currentPage,int pageSize){
       return getResult(query,sort,currentPage,pageSize);
     }
   public List getResult(Sort sort,int currentPage,int pageSize){
       return getResult(null,sort,currentPage,pageSize);
   }
   public List getResult(int currentPage,int pageSize){
     return getResult(null,sort,currentPage,pageSize);
   }
   public static void main(String[] args) {
   	QueryHandle queryHandle = new QueryHandle();
   	queryHandle.config("testDatabase", "book");
   	//selectHandle.setQueryColumns(new String[]{"id","title","content"});
   	//selectHandle.termQuery("id", 1007L);
   	//selectHandle.fuzzyQuery("id", 1007l,2);
       //selectHandle.rangeQueryParser("title", 10,1200);
       //selectHandle.termRangeQuery("id", "10", "1200");
  /* 	PageBean pageBean = new PageBean();
   	pageBean.setCurrentPage(1);
   	pageBean.setPageSize(2);*/
   	//queryHandle.setSort(new String[]{"type","price"}, new Boolean[]{false,false});
   	Query query = queryHandle.parserQuery("bookname",1, "数据库  编程");
   	Query query1 = queryHandle.termQuery("bookname","编程");
   	Query query3 = queryHandle.fuzzyQuery("bookname","西",1);
   	Query[] querys = new Query[]{query,query1};
   	Occur[] occurs = new Occur[]{Occur.SHOULD,Occur.SHOULD};
   	Query query2 = queryHandle.booleanQuery(querys, occurs);
   	List list = queryHandle.getResult(query,1,10);
   	//List list = queryHandle.select("testDatabase", sql);
   	System.out.println("size:"+list.size());
   	//list.stream().filter(l->l==null).forEach(x->{System.out.println(x);System.out.println("-----");});
   	list.forEach(x->{System.out.println(x);System.out.println("-----");});
   	//selectHandle.spanFirstQuery("title", "y", 2);
   	//selectHandle.regexQuery("title", "^1007");
   	//ArrayList<Object> alist = new ArrayList<>();
   	//alist.forEach(list->System.out.println("aa"+list));
   	//selectHandle.setResult(alist);
   	//alist.forEach(list->System.out.println("111:"+list));
	}
}
