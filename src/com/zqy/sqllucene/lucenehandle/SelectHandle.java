package com.zqy.sqllucene.lucenehandle;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.spans.SpanFirstQuery;
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
    public void termQuery(String columnName,Object text) {
        try {
        	 Term term = null;
        	if(text instanceof Long){
        		BytesRef bytes = new BytesRef(NumericUtils.BUF_SIZE_LONG);
            	NumericUtils.longToPrefixCoded((Long)text, 0, bytes);
                term = new Term(columnName, bytes);
        	}else{
        		term = new Term(columnName, text.toString());
        	}
            TermQuery termQuery = new TermQuery(term);
            System.out.println(termQuery);
            TopDocs topdocs = getSearcher().search(termQuery, sizes);
            System.out.println("共找到" + topdocs.scoreDocs.length + ":条记录");
            for (ScoreDoc scoreDocs : topdocs.scoreDocs) {
                int documentId = scoreDocs.doc;
                Document document = getSearcher().doc(documentId);
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
     * 前缀查询
     */
    
    public void prefixQuery(String columnName,String prefix) {
        try {
            Term prefixTerm = new Term(columnName, prefix);
            PrefixQuery query = new PrefixQuery(prefixTerm);
            TopDocs topDoc = getSearcher().search(query, sizes);
            System.out.println("找到记录数:" + topDoc.totalHits);
            for (ScoreDoc scoreDoc : topDoc.scoreDocs) {
                int docId = scoreDoc.doc;
                Document document = getSearcher().doc(docId);
                System.out.println(document);
            }
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
    
    public void phraseQuery(String columnName,int slop,String...texts) {
        try {
        	PhraseQuery phraseQuery = new PhraseQuery();
        	for(int i=0;i<texts.length;i++){
        		 Term term = new Term(columnName, texts[i]);
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
    
    
    public void multiPhraseQuery(String columnName,List<Object> textList){
        try {
            /*String queryStr1 = "钢铁";
            String queryStr2 = "侠";
            String queryStr3 = "情缘";
            Term term1 = new Term("bookName", queryStr1);
            Term term2 = new Term("bookName",queryStr2);
            Term term3 = new Term("bookName",queryStr3);*/
            MultiPhraseQuery multiPhraseQuery = new MultiPhraseQuery();
            for(Object o :textList){
            	Term termString;
            	if(o instanceof String){
            		 termString = new Term(columnName, o.toString());
            		 multiPhraseQuery.add(termString);
            	}else if(o instanceof List){
            		List<String> list = (List<String>) o;
            		Term[] terms = new Term[list.size()];
            		for(int i=0;i<list.size();i++){
            			terms[i] = new Term(columnName, list.get(i));
            		}
            		multiPhraseQuery.add(terms);
            	}else if(o instanceof String[]){
            		String[] texts =  (String[]) o;
            		Term[] terms = new Term[texts.length];
            		for(int i=0;i<texts.length;i++){
            			terms[i] = new Term(columnName, texts[i]);
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
    
    public void fuzzyQuery(String columnName,String text,int maxEdits){
        try {
            Term term = new Term(columnName,text);
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
     * 通配符查询
     */
    
    
    public void wildcardQuery(String columnName,String text){
        try {
            //String queryString = "?品*";
            Term term = new Term(columnName,text);
            WildcardQuery wildcardQuery = new WildcardQuery(term);
            TopDocs topDocs = getSearcher().search(wildcardQuery, 10);
            System.out.println("找到记录数："+topDocs.totalHits);
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
     * 固定位置的词条查询
     * eg:查询分词的第2个位置为大王的记录
     */
    
    public void spanFirstQuery(String columnName,String text,int end){
        try {
            //String queryString = "大王";
            Term term = new Term(columnName,text);
            SpanTermQuery spanTermQuery = new SpanTermQuery(term);
            SpanFirstQuery spanFirstQuery = new SpanFirstQuery(spanTermQuery,end);
            TopDocs topDocs =  getSearcher().search(spanFirstQuery,sizes);
            System.out.println("找到记录:"+topDocs.totalHits);
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
     * parse类似于phraseQuery
     * 使用parser时把一个查询字符串用双引号括起来表示该字符串不会被分词
     */
    
    
    public void parseLikePhraseQuery(String columnName,String parseText){
        try {
            //String parseString = "\"钢铁侠\"装备";
            QueryParser parser = new QueryParser(Version.LUCENE_46, columnName,dataBaseDefaultConfig.getAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(parseText);
            System.out.println("query:"+query);
            TopDocs topDocs = getSearcher().search(query, sizes);
            System.out.println("找到记录数:"+topDocs.totalHits);
            for(ScoreDoc scoreDoc :topDocs.scoreDocs){
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
     * parser和fuzzyQuery
     */
    
    public void parseLikeFuzzyQuery(String columnName,String text){
        try {
            //String parseStr = "钢铁侠~0.6";
            QueryParser parser = new QueryParser(Version.LUCENE_46,columnName,dataBaseDefaultConfig.getAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(text);
            TopDocs topDocs = getSearcher().search(query, sizes);
            System.out.println("找到记录数："+topDocs.totalHits);
            for(ScoreDoc scoreDoc :topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = getSearcher().doc(docId);
                System.out.println(document);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            System.out.println("query:"+query);
            TopDocs topDocs = getSearcher().search(query,sizes);
            System.out.println("找到记录数:"+topDocs.totalHits);
            Highlighter highLighter = new Highlighter(new QueryScorer(query));
            for(ScoreDoc scoreDoc : topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = getSearcher().doc(docId);
                String bookNameResult = document.get("bookName");
                TokenStream tokenStream = ikAnalyzer.tokenStream("bookName", new StringReader(bookNameResult));
                bookNameResult = highLighter.getBestFragment(tokenStream, bookNameResult);
                System.out.println(bookNameResult);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            // TODO Auto-generated catch block
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
            TopDocs topDocs = getSearcher().search(queryParser.parse(text),sizes);
            System.out.println("找到记录数:"+topDocs.totalHits);
            Highlighter highLighter = new Highlighter(new QueryScorer(query));
            resultHandle(topDocs);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void resultHandle(TopDocs topDocs){
    	  for(ScoreDoc scoreDoc : topDocs.scoreDocs){
              int docId = scoreDoc.doc;
             
              try {
				Document document = getSearcher().doc(docId);
				System.out.println(document.getField("content").stringValue());
				if(document.getField("id")!=null)System.out.println(document.getField("id").stringValue());
				if(document.getField("title")!=null)System.out.println(document.getField("title").stringValue());
			
			} catch (IOException e) {
				e.printStackTrace();
			}
              //String bookNameResult = document.get("bookName");
              //TokenStream tokenStream = dataBaseDefaultConfig.getAnalyzer().tokenStream("bookName", new StringReader(bookNameResult));
              //bookNameResult = highLighter.getBestFragment(tokenStream, bookNameResult);
             // System.out.println(bookNameResult);
          }
    }
    public static void main(String[] args) {
    	SelectHandle selectHandle = new SelectHandle();
    	selectHandle.queryConfig("testDatabase", new String[]{"testTable"}, 10);
    	selectHandle.setQueryColumn(new String[]{"id"});
    	//selectHandle.termQuery("id", 1005L);
    	selectHandle.fuzzyQuery("id", "1007",1);
        //selectHandle.multiFieldQueryParser("1007","id","title","content");
	}
}
