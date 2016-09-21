package com.zqy.sqllucene.lucenehandle;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericRangeQueryNode;
import org.apache.lucene.queryparser.xml.builders.NumericRangeQueryBuilder;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;

public class SelectHandle {
	 private String dataBaseName;
	 private String[] tableNames;
	 private String[] queryColumn;
	 private int sizes;
	 public void queryConfig(String dataBaseName,String[] tableNames,int sizes){
		 this.dataBaseName = dataBaseName;
		 this.tableNames = tableNames;
		 this.sizes = sizes;
	 }
	 public void setQueryColumn(String[] queryColumn){
		 this.queryColumn = queryColumn;
	 }
	 private DataBaseDefaultConfig dataBaseDefaultConfig = DataBaseDefaultConfig.getInstance();
	 public IndexSearcher getSearcher() {
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
	  /*
	   * term为查询的最小单位，termQuery的查询字符串将作为一个整体，不会被分词
	   * 指定field进行查询，termquery不能进行数字和日期的查询
       * 日期的查询需要转成数字进行查询，
       * 数字查询使用NumbericRangeQuery
	     */	 
    public Term termQuery(String columnName,Object keyWord) {
        Term term = getTerm(columnName,keyWord);
		TermQuery termQuery = new TermQuery(term);
		resultHandle(termQuery);
		return term;
    }
    /*
     * 前缀查询
     */
    
    public Term prefixQuery(String columnName,String prefix) {
    	Term prefixTerm = null;
        prefixTerm = new Term(columnName,prefix);
		PrefixQuery query = new PrefixQuery(prefixTerm);
		resultHandle(query);
		return prefixTerm;
    }
    /*
     * 数字查询
     * 字符串有效
     */
    public void termRangeQuery(String columnName,String start,String end) {
		Query query = new TermRangeQuery(columnName,new BytesRef(start.getBytes()),new BytesRef(end.getBytes()) , true, true);
		resultHandle(query);
    }
   
    /*
     * 数字查询
     * NumericRangeQuery只对
     * IntField,
     * LongField,
     * FloatField,
     * DoubleField等这些表示数字的Field域有效
     */
    public void numericRangeQuery(String columnName,Float start,Float end) {
        Query query = NumericRangeQuery.newFloatRange(columnName, start,end, true, true);   		
		resultHandle(query);
    }
    public void numericRangeQuery(String columnName,Long start,Long end) {
        Query query = NumericRangeQuery.newLongRange(columnName, start,end, true, true);   		
		resultHandle(query);
    }
    public void numericRangeQuery(String columnName,Integer start,Integer end) {
        Query query = NumericRangeQuery.newIntRange(columnName, start,end, true, true);   		
		resultHandle(query);
    }
    public void numericRangeQuery(String columnName,Double start,Double end) {
        Query query = NumericRangeQuery.newDoubleRange(columnName, start,end, true, true);   		
		resultHandle(query);
    }
   
    /*
     * 通配符查询
     */
    public Term wildcardQuery(String columnName,String keyWord){
    	Term term =null;
        //String queryString = "?品*";
		term = new Term(columnName, keyWord);
		WildcardQuery wildcardQuery = new WildcardQuery(term);
		resultHandle(wildcardQuery);
		return term;
    }
 /*   SpanTermQuery，他和TermQuery用法很相似，
  *   唯一区别就是SapnTermQuery可以得到Term的span跨度信息*/
    public void spanTermQuery(String columnName,String keyWord,int end){
        //String queryString = "大王";
    	Term term = new Term(columnName, keyWord);
		SpanTermQuery spanTermQuery = new SpanTermQuery(term);
		resultHandle(spanTermQuery);
    }
    /*
     * 固定位置的词条查询,在指定距离可以找到第一个单词的查询。
     * eg:查询分词的第2个位置为大王的记录
     */
    
    public void spanFirstQuery(String columnName,String keyWord,int end){
    	Term term =null;
        //String queryString = "大王";
		term = new Term(columnName, keyWord);
		SpanTermQuery spanTermQuery = new SpanTermQuery(term);
		SpanFirstQuery spanFirstQuery = new SpanFirstQuery(spanTermQuery,end);
		resultHandle(spanFirstQuery);
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
    public void spanNearQuery(String columnName,String keyWordStart,String keyWordEnd,int slop){
    	 SpanQuery queryStart = new SpanTermQuery(new Term(columnName,keyWordStart));
         SpanQuery queryEnd = new SpanTermQuery(new Term(columnName,keyWordEnd));
         SpanQuery spanNearQuery = new SpanNearQuery(
             new SpanQuery[] {queryStart,queryEnd}, slop, false, false);
     	resultHandle(spanNearQuery);
    }
   /* SpanNearQuery：查询的几个语句之间保持者一定的距离。
    SpanOrQuery：同时查询几个词句查询。
    SpanNotQuery：从一个词距查询结果中，去除一个词距查询。*/
  
    /*
     * RegexQuery
     * 正则查询，查询的字符串为一个正则表达式
     */
    public void regexQuery(String columnName,String regexText){
        //String queryString = "^钢铁";
        Term t = new Term(columnName,regexText);
        RegexQuery regexQuery = new RegexQuery(t);
        try {
            TopDocs topDocs = getSearcher().search(regexQuery, sizes);
            System.out.println("找到数据："+topDocs.totalHits);
            for(ScoreDoc scoreDoc : topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = getSearcher().doc(docId);
                System.out.println(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
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
            TopDocs topdocs = getSearcher().search(query, sizes);
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
    
    public void phraseQuery(String columnName,int slop,Object...keyWords) {
        try {
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
            System.out.println(phraseQuery.toString());
            TopDocs topDocs = getSearcher().search(phraseQuery, sizes);
            System.out.println("找到记录数:" + topDocs.totalHits);
            for (ScoreDoc score : topDocs.scoreDocs) {
                int docId = score.doc;
                Document document = getSearcher().doc(docId);
                System.out.println(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
     * 多短语查询
     * 查询结果为钢铁侠 或 钢铁情缘
     */
    
    
    public void multiPhraseQuery(String columnName,List<Object> keyWordList){
        try {
            /*String queryStr1 = "钢铁";
            String queryStr2 = "侠";
            String queryStr3 = "情缘";
            Term term1 = new Term("bookName", queryStr1);
            Term term2 = new Term("bookName",queryStr2);
            Term term3 = new Term("bookName",queryStr3);*/
            MultiPhraseQuery multiPhraseQuery = new MultiPhraseQuery();
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
            TopDocs topDocs = getSearcher().search(multiPhraseQuery,sizes);
            System.out.println("找到记录数:"+topDocs.totalHits);
            for(ScoreDoc scoreDoc : topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = getSearcher().doc(docId);
                System.out.println(docId+":"+document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }
    /*
     * 模糊查询（相似度查询）
     * 默认的相似度为0.5
     * 在实例化FuzzyQuery时可以改变相似度
     */
    
    public void fuzzyQuery(String columnName,Object keyWord,int maxEdits){
        try {
            Term term =getTerm(columnName,keyWord);
            FuzzyQuery fuzzyQuery = new FuzzyQuery(term,maxEdits);
            TopDocs topDocs = getSearcher().search(fuzzyQuery, sizes);
            System.out.println("找到记录数:"+topDocs.totalHits);
            for(ScoreDoc scoreDoc :topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = getSearcher().doc(docId);
                System.out.println(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
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
            resultHandle(query);
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
            resultHandle(query);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
    }
    
    /*
     * 多域查询
     */
    
    
    public void multiFieldQueryParser(String [] queries,String [] fields,BooleanClause.Occur [] booleanClauseOccur){
        try {
            //String bookNameString = "钢铁是怎样炼成 的";
            //String bookPriceString = "[70 TO 80]";
            //String [] queries = new String []{bookNameString,bookPriceString};
            //String [] fields = {"bookName","bookPrice"};
            //BooleanClause.Occur [] booleanClauseOccur = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
            Analyzer ikAnalyzer = new IKAnalyzer();
            Query query = MultiFieldQueryParser.parse(Version.LUCENE_46, queries, fields,booleanClauseOccur,ikAnalyzer);
            resultHandle(query);
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
    }
    public void multiFieldQueryParser(String text,String... columnNames){
        try {
            //String bookNameString = "钢铁是怎样炼成 的";
            //String bookPriceString = "[70 TO 80]";
            //String [] queries = new String []{bookNameString,bookPriceString};
            //String [] fields = {"bookName","bookPrice"};
            //BooleanClause.Occur [] booleanClauseOccur = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
            QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_46,columnNames ,dataBaseDefaultConfig.getAnalyzer()); 
            System.out.println("query:"+queryParser);
            Query  query = queryParser.parse(text);
            resultHandle(query);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void resultHandle(Query query){
    	 System.out.println(query);
         TopDocs topdocs;
		try {
		 topdocs = getSearcher().search(query, sizes);
         System.out.println("共找到" + topdocs.scoreDocs.length + ":条记录");
         Highlighter highLighter = new Highlighter(new QueryScorer(query));
         for (ScoreDoc scoreDocs : topdocs.scoreDocs) {
             int documentId = scoreDocs.doc;
             Document document = getSearcher().doc(documentId);
             System.out.println(document);
         }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
    public static void main(String[] args) {
    	SelectHandle selectHandle = new SelectHandle();
    	selectHandle.queryConfig("testDatabase", new String[]{"testTable"}, 10);
    	selectHandle.setQueryColumn(new String[]{"id"});
    	//selectHandle.termQuery("id", 1005L);
    	//selectHandle.fuzzyQuery("id", 1007l,2);
        //selectHandle.rangeQueryParser("title", 10,1200);
        //selectHandle.termRangeQuery("id", "10", "1200");
    	selectHandle.prefixQuery("title","1");
    	//selectHandle.spanFirstQuery("title", "y", 2);
	}
}
