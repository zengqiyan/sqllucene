package com.zqy.sqllucene.lucenehandle;

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
import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.sqlparser.SelectParser;

public class QueryHandle {
	 private String dataBaseName;
	 private String[] tableNames;
	 private String tableName;
	 private String[] queryColumns;
	 private Sort sort;
	 private Highlighter highlighter;
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
	 private IndexSearcher getSearcher(String tableName) {
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
	  /*
	   * term为查询的最小单位，termQuery的查询字符串将作为一个整体，不会被分词
	   * 指定field进行查询，termquery不能进行数字和日期的查询
      * 日期的查询需要转成数字进行查询，
      * 数字查询使用NumbericRangeQuery
	     */	 
   public TermQuery termQuery(String columnName,Object keyWord) {
       Term term = getTerm(columnName,keyWord);
		TermQuery termQuery = new TermQuery(term);
		return termQuery;
   }
   /*
    * 前缀查询
    */
   
   public PrefixQuery prefixQuery(String columnName,String prefix) {
   	Term prefixTerm = null;
       prefixTerm = new Term(columnName,prefix);
		PrefixQuery query = new PrefixQuery(prefixTerm);
		return query;
   }
   /*
    * 数字查询
    * 字符串有效
    */
   public TermRangeQuery termRangeQuery(String columnName,String start,String end) {
   	TermRangeQuery termRangeQuery = new TermRangeQuery(columnName,new BytesRef(start.getBytes()),new BytesRef(end.getBytes()) , true, true);
		return termRangeQuery;
   }
  
   /*
    * 数字查询
    * NumericRangeQuery只对
    * IntField,
    * LongField,
    * FloatField,
    * DoubleField等这些表示数字的Field域有效
    */
   public NumericRangeQuery<Float> numericRangeQuery(String columnName,Float start,Float end) {
   	NumericRangeQuery<Float> numericRangeQuery = NumericRangeQuery.newFloatRange(columnName, start,end, true, true);   		
		return numericRangeQuery;
   }
   public NumericRangeQuery<Long> numericRangeQuery(String columnName,Long start,Long end) {
   	NumericRangeQuery<Long> numericRangeQuery = NumericRangeQuery.newLongRange(columnName, start,end, true, true);   		
		return numericRangeQuery;
   }
   public NumericRangeQuery<Integer> numericRangeQuery(String columnName,Integer start,Integer end) {
   	NumericRangeQuery<Integer> numericRangeQuery = NumericRangeQuery.newIntRange(columnName, start,end, true, true);   		
		return numericRangeQuery;
   }
   public NumericRangeQuery<Double> numericRangeQuery(String columnName,Double start,Double end) {
   	NumericRangeQuery<Double> numericRangeQuery = NumericRangeQuery.newDoubleRange(columnName, start,end, true, true);   		
		return numericRangeQuery;
   }
  
   /*
    * 通配符查询
    */
   public WildcardQuery wildcardQuery(String columnName,String keyWord){
       //String queryString = "?品*";
	    Term term = new Term(columnName, keyWord);
		WildcardQuery wildcardQuery = new WildcardQuery(term);
		return wildcardQuery;
   }
/*   SpanTermQuery，他和TermQuery用法很相似，
 *   唯一区别就是SapnTermQuery可以得到Term的span跨度信息*/
   public SpanTermQuery spanTermQuery(String columnName,String keyWord,int end){
       //String queryString = "大王";
   		Term term = new Term(columnName, keyWord);
		SpanTermQuery spanTermQuery = new SpanTermQuery(term);
		return spanTermQuery;
   }
   /*
    * 固定位置的词条查询,在指定距离可以找到第一个单词的查询。
    * eg:查询分词的第2个位置为大王的记录
    */
   
   public SpanFirstQuery spanFirstQuery(String columnName,String keyWord,int end){
   	Term term =null;
       //String queryString = "大王";
		term = new Term(columnName, keyWord);
		SpanTermQuery spanTermQuery = new SpanTermQuery(term);
		SpanFirstQuery spanFirstQuery = new SpanFirstQuery(spanTermQuery,end);
		return spanFirstQuery;
   }
 /*  SpanNearQuery：用来匹配两个Term之间的跨度的，即一个Term经过几个跨度可以到达另一个Term,
  * slop为跨度因子，用来限制两个Term之间的最大跨度，不可能一个Term和另一个Term之间要经过十万八千个跨度
  * 才到达也算两者相近，这不符合常理。所以有个slop因子进行限制。还有一个inOrder参数要引起注意，它用来设置是
  * 否允许进行倒序跨度，什么意思？即TermA到TermB不一定是从左到右去匹配也可以从右到左，而从右到左就是倒序，
  * inOrder为true即表示order(顺序)很重要不能倒序去匹配必须正向去匹配，false则反之。 注意停用词不在slop统计范围内 。
   Slop的理解很重要：
             在默认情况下slop的值是0, 就相当于TermQuery的精确匹配, 
             通过设置slop参数(比如”one five”匹配”one two three four five”就需要slop=3,
             如果slop=2就无法得到结果。这里我们可以认为slope是单词移动得次数，可以左移或者右移。这里特别提 醒,
   PhraseQuery不保证前后单词的次序,在上面的例子中,”two one”就需要2个slop,也就是认为one 向左边移动2位, 就是能够匹配的”one two”如果是“five three one” 就需要slope=6才能匹配。
   */
   public SpanNearQuery spanNearQuery(String columnName,String keyWordStart,String keyWordEnd,int slop){
   	 SpanQuery queryStart = new SpanTermQuery(new Term(columnName,keyWordStart));
        SpanQuery queryEnd = new SpanTermQuery(new Term(columnName,keyWordEnd));
        SpanNearQuery spanNearQuery = new SpanNearQuery(
            new SpanQuery[] {queryStart,queryEnd}, slop, false, false);
		return spanNearQuery;
   }
   //SpanNotQuery:使用场景是当使用SpanNearQuery时，如果两个Term从TermA到TermB有多种情况，
   //即可能出现TermA或者TermB在索引中重复出现，则可能有多种情况，SpanNotQuery就是用来限制TermA和
   //TermB之间不存在TermC,从而排除一些情况，实现更精确的控制
   //slop:keyWordStart与keyWordEnd跨度
   //pre:keyWordExclude之前有多少字符
   //post:keyWordExclude之后有多少字符
   public SpanNotQuery spanNotQuery(String columnName,String keyWordStart,String keyWordEnd,String keyWordExclude,int slop,int pre, int post){
        SpanQuery queryStart = new SpanTermQuery(new Term("columnName",keyWordStart));
        SpanQuery queryEnd = new SpanTermQuery(new Term("columnName",keyWordEnd));
        SpanQuery excludeQuery = new SpanTermQuery(new Term("columnName",keyWordExclude));
        SpanQuery spanNearQuery = new SpanNearQuery(
            new SpanQuery[] {queryStart,queryEnd}, slop, false, false);
        SpanNotQuery spanNotQuery = new SpanNotQuery(spanNearQuery, excludeQuery, pre,post);
		return spanNotQuery;
  }
  public SpanNotQuery spanNotQuery(String columnName,String keyWordStart,String keyWordEnd,String keyWordExclude,int slop){
       SpanQuery queryStart = new SpanTermQuery(new Term(columnName,keyWordStart));
       SpanQuery queryEnd = new SpanTermQuery(new Term(columnName,keyWordEnd));
       SpanQuery excludeQuery = new SpanTermQuery(new Term(columnName,keyWordExclude));
       SpanQuery spanNearQuery = new SpanNearQuery(
           new SpanQuery[] {queryStart,queryEnd}, slop, false, false);
        SpanNotQuery spanNotQuery = new SpanNotQuery(spanNearQuery, excludeQuery);
		return spanNotQuery;
 }
  /* 
   SpanOrQuery：同时查询几个词句查询。
  */
  public SpanOrQuery spanOrQuery(SpanQuery... spanQuerys){
	 SpanOrQuery spanOrQuery = new SpanOrQuery(spanQuerys);
	return spanOrQuery;
}
  
   /*
    * RegexQuery
    * 正则查询，查询的字符串为一个正则表达式
    */
   public RegexQuery regexQuery(String columnName,String regexText){
       //String queryString = "^钢铁";
       Term t = new Term(columnName,regexText);
       RegexQuery regexQuery = new RegexQuery(t);
       return regexQuery;
   }
   /*
    * 模糊查询（相似度查询）
    * 默认的相似度为0.5
    * 在实例化FuzzyQuery时可以改变相似度
    */
   public FuzzyQuery fuzzyQuery(String columnName,Object keyWord,int maxEdits){
           Term term =getTerm(columnName,keyWord);
           FuzzyQuery fuzzyQuery = new FuzzyQuery(term, maxEdits);
           return fuzzyQuery; 
   }
   
   //多个query查询
   public BooleanQuery booleanQuery(Query[] querys,Occur[] occurs){
       BooleanQuery booleanQuery= new BooleanQuery();
       if(querys.length!=occurs.length){
       	throw new RuntimeException("query布尔条件与query个数不符合！");
       }
       for(int i=0;i<querys.length;i++){
       	  booleanQuery.add(querys[i], occurs[i]);
       }
		return booleanQuery;
   }
   
   /*
    * 使用parser可以替代query
    * 如下相当于 钢铁  / 侠 使用 BooleanQuery的Must
    */
   public void parser(String columnName,String text) {
       try {
           QueryParser parser = new QueryParser(Version.LUCENE_46, columnName,
                   new IKAnalyzer());
           parser.setDefaultOperator(Operator.AND);
           Query query = parser.parse(text);
           System.out.println("query:" + query);
           TopDocs topdocs = getSearcher().search(query, 10);
           System.out.println("共找到" + topdocs.scoreDocs.length + ":条记录");
           for (ScoreDoc scoreDocs : topdocs.scoreDocs) {
               int documentId = scoreDocs.doc;
               Document document = getSearcher().doc(documentId);
               System.out.println(document);
           }
       } catch (ParseException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
   
   /*
    * parser相当于rangeQuery
    */
   public void rangeQueryParser(String columnName,float min ,float max) {
       try {
           String queryString = "["+min+" TO "+max+"]";
           QueryParser parser = new QueryParser(Version.LUCENE_46,
           		columnName, dataBaseDefaultConfig.getAnalyzer());
           Query query = parser.parse(queryString);
           System.out.println("query:" + query.toString());
           TopDocs topDocs = getSearcher().search(query, 10000);
           System.out.println("共找到:" + topDocs.totalHits + "条记录");
           for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
               int docId = scoreDoc.doc;
               Document document = getSearcher().doc(docId);
               System.out.println(document);
           }
       } catch (ParseException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }

   }
  
   /*
    * 短语查询
    * 如下默认搜索为  雅钢铁
    * 可以通过设置坡度（setSlop）来限定中间可以插入的字数
    * 默认为0
    */
   
   public PhraseQuery phraseQuery(String columnName,int slop,Object...keyWords) {
       PhraseQuery phraseQuery = new PhraseQuery();
		for(int i=0;i<keyWords.length;i++){
			 Term term = getTerm(columnName, keyWords[i]);
			 phraseQuery.add(term);
		}
		//Term term1 = new Term("bookName", "雅");
		//Term term2 = new Term("bookName", "钢铁");
		//phraseQuery.add(term1);
		//phraseQuery.add(term2);
		phraseQuery.setSlop(slop);
		return phraseQuery;
   }
   /*
    * 多短语查询
    * 查询结果为钢铁侠 或 钢铁情缘
    */
   
   public MultiPhraseQuery multiPhraseQuery(String columnName,List<Object> keyWordList){
   	 MultiPhraseQuery multiPhraseQuery = null;
   	/*String queryStr1 = "钢铁";
		String queryStr2 = "侠";
		String queryStr3 = "情缘";
		Term term1 = new Term("bookName", queryStr1);
		Term term2 = new Term("bookName",queryStr2);
		Term term3 = new Term("bookName",queryStr3);*/
		multiPhraseQuery = new MultiPhraseQuery();
		for(Object o :keyWordList){
			Term termString;
			if(o instanceof String){
				 termString = new Term(columnName, o.toString());
				 multiPhraseQuery.add(termString);
			}else if(o instanceof List){
				List<Object> list = (List<Object>) o;
				Term[] terms = new Term[list.size()];
				for(int i=0;i<list.size();i++){
					terms[i] = getTerm(columnName, list.get(i));
				}
				multiPhraseQuery.add(terms);
			}else if(o instanceof Object[]){
				Object[] keyWords =  (Object[]) o;
				Term[] terms = new Term[keyWords.length];
				for(int i=0;i<keyWords.length;i++){
					terms[i] = getTerm(columnName, keyWords[i]);
				}
				multiPhraseQuery.add(terms);
			}
		}
		multiPhraseQuery.setSlop(10);
		return multiPhraseQuery;
   
   }
   
   
   /*
    * parse类似于phraseQuery
    * 使用parser时把一个查询字符串用双引号括起来表示该字符串不会被分词
    */
   
   
   public void parseLikePhraseQuery(String columnName,String parseText){
       try {
           //String parseString = "\"钢铁侠\"装备";
           QueryParser parser = new QueryParser(Version.LUCENE_46, columnName,dataBaseDefaultConfig.getAnalyzer());
           parser.setDefaultOperator(Operator.AND);
           Query query = parser.parse(parseText);
       } catch (ParseException e) {
           e.printStackTrace();
       }
   }
   
   /*
    * parser和fuzzyQuery
    */
   
   public void parseLikeFuzzyQuery(String columnName,String text) throws IOException{
       try {
           //String parseStr = "钢铁侠~0.6";
           QueryParser parser = new QueryParser(Version.LUCENE_46,columnName,dataBaseDefaultConfig.getAnalyzer());
           parser.setDefaultOperator(Operator.AND);
           Query query = parser.parse(text);
       } catch (ParseException e) {
           e.printStackTrace();
       }
       
   }
   
   /*
    * 多域查询
    */
   
   
   public Query multiFieldQueryParser(String [] queries,String [] fields,BooleanClause.Occur [] booleanClauseOccur){
   	Query query =null;
   	try {
           //String bookNameString = "钢铁是怎样炼成 的";
           //String bookPriceString = "[70 TO 80]";
           //String [] queries = new String []{bookNameString,bookPriceString};
           //String [] fields = {"bookName","bookPrice"};
           //BooleanClause.Occur [] booleanClauseOccur = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
           query = MultiFieldQueryParser.parse(Version.LUCENE_46, queries, fields,booleanClauseOccur,dataBaseDefaultConfig.getAnalyzer());
           /*TopDocs topDocs = getSearcher().search(query,sizes);
           System.out.println("找到记录数:"+topDocs.totalHits);
           Highlighter highLighter = new Highlighter(new QueryScorer(query));
           for(ScoreDoc scoreDoc : topDocs.scoreDocs){
               int docId = scoreDoc.doc;
               Document document = getSearcher().doc(docId);
               String bookNameResult = document.get("bookName");
               TokenStream tokenStream = ikAnalyzer.tokenStream("bookName", new StringReader(bookNameResult));
               bookNameResult = highLighter.getBestFragment(tokenStream, bookNameResult);
               System.out.println(bookNameResult);
           }*/
       } catch (ParseException e) {
           e.printStackTrace();
       }
		return query;
   }
   public Query multiFieldQueryParser(String text,String... columnNames){
           //String bookNameString = "钢铁是怎样炼成 的";
           //String bookPriceString = "[70 TO 80]";
           //String [] queries = new String []{bookNameString,bookPriceString};
           //String [] fields = {"bookName","bookPrice"};
           //BooleanClause.Occur [] booleanClauseOccur = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
           QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_46,columnNames ,dataBaseDefaultConfig.getAnalyzer()); 
           System.out.println("query:"+queryParser);
           Query query = null;
			try {
				query = queryParser.parse(text);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return query;
   }
   private Term getTerm(String columnName,Object keyWord ){
		 Term term = null;
		 if(keyWord instanceof Long){
			BytesRef bytes = new BytesRef(NumericUtils.BUF_SIZE_LONG);
	    	NumericUtils.longToPrefixCoded((Long)keyWord, 0, bytes);
	    	//NumericUtils.doubleToSortableLong(val)
	        term = new Term(columnName, bytes);
		 }else{
			term = new Term(columnName, keyWord.toString());
		 }
		    return term;
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

   public List getResult(Query query,Sort sort,int offset,int rowCount){
   	   if(query==null)query=new MatchAllDocsQuery();
          SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("【","】"); //如果不指定参数的话，默认是加粗，即<b><b/>
          QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
          Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片段
          Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
          highlighter.setTextFragmenter(fragmenter); //设置一下要显示的片段
          ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();
  		try {
  		   //int totalRecord = searchTotalRecord(getSearcher(),query);
  		  //System.out.println("共找到" + totalRecord + ":条记录");
  		  if(sort==null) sort = Sort.RELEVANCE;
  		  if(rowCount==0)rowCount = searchTotalRecord(getSearcher(),query);
          TopFieldCollector c = TopFieldCollector.create(sort, offset+rowCount, false, false, false, false);
          getSearcher().search(query, c);
          for (ScoreDoc scoreDoc : c.topDocs(offset, rowCount).scoreDocs) {
              int documentId = scoreDoc.doc;
              Document document = getSearcher().doc(documentId);
              HashMap<String,String> map = new HashMap<>();
              if(queryColumns!=null && queryColumns.length>0){
             	 for(int i=0;i<queryColumns.length;i++){
             		  String value = document.get(queryColumns[i]);
             		  if(value != null) {
                           TokenStream tokenStream = dataBaseDefaultConfig.getAnalyzer().tokenStream(queryColumns[i],new StringReader(value));
                           String summary = highlighter.getBestFragment(tokenStream, value);
                           if(summary!=null){
                           	value = summary;
                           }
                       }
             		  map.put(queryColumns[i], value);
             	 }
              }else{
           	   List<Column> columnList = null;
           		 if(tableNames!=null && tableNames.length>0){
           			 columnList = dataBaseDefaultConfig.getColumns(dataBaseName,tableNames);
           		 }else{
           			 columnList = dataBaseDefaultConfig.getColumns(dataBaseName,tableName);
           		 }
           	   for(Column column:columnList){
              		  String value = document.get(column.getName());
              		  if(value != null) {
                            TokenStream tokenStream = dataBaseDefaultConfig.getAnalyzer().tokenStream(column.getName(),new StringReader(value));
                            String summary = highlighter.getBestFragment(tokenStream, value);
                            if(summary!=null){
                           	 value = summary;
                            }
                        }
              		  map.put(column.getName(),value);
           	   }
           	   
              }
              list.add(map);
          }
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
   private String showHighlight(String field){
		return field;
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
   	Query query = queryHandle.termQuery("bookname","数据库");
   	Query query1 = queryHandle.termQuery("bookname","编程");
   	Query query3 = queryHandle.fuzzyQuery("bookname","西",1);
   	Query[] querys = new Query[]{query,query1};
   	Occur[] occurs = new Occur[]{Occur.SHOULD,Occur.SHOULD};
   	Query query2 = queryHandle.booleanQuery(querys, occurs);
   	List list = queryHandle.getResult(query3,1,100);
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
