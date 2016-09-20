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
	   * term涓烘煡璇㈢殑鏈�灏忓崟浣嶏紝termQuery鐨勬煡璇㈠瓧绗︿覆灏嗕綔涓轰竴涓暣浣擄紝涓嶄細琚垎璇�
	   * 鎸囧畾field杩涜鏌ヨ锛宼ermquery涓嶈兘杩涜鏁板瓧鍜屾棩鏈熺殑鏌ヨ
       * 鏃ユ湡鐨勬煡璇㈤渶瑕佽浆鎴愭暟瀛楄繘琛屾煡璇紝
       * 鏁板瓧鏌ヨ浣跨敤NumbericRangeQuery
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
            System.out.println("鍏辨壘鍒�" + topdocs.scoreDocs.length + ":鏉¤褰�");
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
     * 浣跨敤parser鍙互鏇夸唬query
     * 濡備笅鐩稿綋浜� 閽㈤搧  / 渚� 浣跨敤 BooleanQuery鐨凪ust
     */

    public void parser(String columnName,String text) {
        try {
            QueryParser parser = new QueryParser(Version.LUCENE_46, columnName,
                    new IKAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(text);
            System.out.println("query:" + query);
            TopDocs topdocs = getSearcher().search(query, sizes);
            System.out.println("鍏辨壘鍒�" + topdocs.scoreDocs.length + ":鏉¤褰�");
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
     * parser鐩稿綋浜巖angeQuery
     */
    public void rangeQueryParser(String columnName,float min ,float max) {
        try {

            String queryString = "["+min+" TO "+max+"]";
            QueryParser parser = new QueryParser(Version.LUCENE_46,
            		columnName, dataBaseDefaultConfig.getAnalyzer());
            Query query = parser.parse(queryString);
            System.out.println("query:" + query.toString());
            TopDocs topDocs = getSearcher().search(query, 10000);
            System.out.println("鍏辨壘鍒�:" + topDocs.totalHits + "鏉¤褰�");
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
     * 鍓嶇紑鏌ヨ
     */
    
    public void prefixQuery(String columnName,String prefix) {
        try {
            Term prefixTerm = new Term(columnName, prefix);
            PrefixQuery query = new PrefixQuery(prefixTerm);
            TopDocs topDoc = getSearcher().search(query, sizes);
            System.out.println("鎵惧埌璁板綍鏁�:" + topDoc.totalHits);
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
     * 鐭鏌ヨ
     * 濡備笅榛樿鎼滅储涓�  闆呴挗閾�
     * 鍙互閫氳繃璁剧疆鍧″害锛坰etSlop锛夋潵闄愬畾涓棿鍙互鎻掑叆鐨勫瓧鏁�
     * 榛樿涓�0
     */
    
    public void phraseQuery(String columnName,int slop,String...texts) {
        try {
        	PhraseQuery phraseQuery = new PhraseQuery();
        	for(int i=0;i<texts.length;i++){
        		 Term term = new Term(columnName, texts[i]);
        		 phraseQuery.add(term);
        	}
            //Term term1 = new Term("bookName", "闆�");
            //Term term2 = new Term("bookName", "閽㈤搧");
            //phraseQuery.add(term1);
            //phraseQuery.add(term2);
            phraseQuery.setSlop(slop);
            System.out.println(phraseQuery.toString());
            TopDocs topDocs = getSearcher().search(phraseQuery, sizes);
            System.out.println("鎵惧埌璁板綍鏁�:" + topDocs.totalHits);
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
     * 澶氱煭璇煡璇�
     * 鏌ヨ缁撴灉涓洪挗閾佷緺 鎴� 閽㈤搧鎯呯紭
     */
    
    
    public void multiPhraseQuery(String columnName,List<Object> textList){
        try {
            /*String queryStr1 = "閽㈤搧";
            String queryStr2 = "渚�";
            String queryStr3 = "鎯呯紭";
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
            System.out.println("鎵惧埌璁板綍鏁�:"+topDocs.totalHits);
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
     * 妯＄硦鏌ヨ锛堢浉浼煎害鏌ヨ锛�
     * 榛樿鐨勭浉浼煎害涓�0.5
     * 鍦ㄥ疄渚嬪寲FuzzyQuery鏃跺彲浠ユ敼鍙樼浉浼煎害
     */
    
    public void fuzzyQuery(String columnName,String text,int maxEdits){
        try {
            Term term = new Term(columnName,text);
            FuzzyQuery fuzzyQuery = new FuzzyQuery(term,maxEdits);
            TopDocs topDocs = getSearcher().search(fuzzyQuery, sizes);
            System.out.println("鎵惧埌璁板綍鏁�:"+topDocs.totalHits);
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
     * 閫氶厤绗︽煡璇�
     */
    
    
    public void wildcardQuery(String columnName,String text){
        try {
            //String queryString = "?鍝�*";
            Term term = new Term(columnName,text);
            WildcardQuery wildcardQuery = new WildcardQuery(term);
            TopDocs topDocs = getSearcher().search(wildcardQuery, 10);
            System.out.println("鎵惧埌璁板綍鏁帮細"+topDocs.totalHits);
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
     * 鍥哄畾浣嶇疆鐨勮瘝鏉℃煡璇�
     * eg:鏌ヨ鍒嗚瘝鐨勭2涓綅缃负澶х帇鐨勮褰�
     */
    
    public void spanFirstQuery(String columnName,String text,int end){
        try {
            //String queryString = "澶х帇";
            Term term = new Term(columnName,text);
            SpanTermQuery spanTermQuery = new SpanTermQuery(term);
            SpanFirstQuery spanFirstQuery = new SpanFirstQuery(spanTermQuery,end);
            TopDocs topDocs =  getSearcher().search(spanFirstQuery,sizes);
            System.out.println("鎵惧埌璁板綍:"+topDocs.totalHits);
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
     * 姝ｅ垯鏌ヨ锛屾煡璇㈢殑瀛楃涓蹭负涓�涓鍒欒〃杈惧紡
     */
    
    
    public void regexQuery(String columnName,String regexText){
        //String queryString = "^閽㈤搧";
        Term t = new Term(columnName,regexText);
        RegexQuery regexQuery = new RegexQuery(t);
        try {
            TopDocs topDocs = getSearcher().search(regexQuery, sizes);
            System.out.println("鎵惧埌鏁版嵁锛�"+topDocs.totalHits);
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
     * parse绫讳技浜巔hraseQuery
     * 浣跨敤parser鏃舵妸涓�涓煡璇㈠瓧绗︿覆鐢ㄥ弻寮曞彿鎷捣鏉ヨ〃绀鸿瀛楃涓蹭笉浼氳鍒嗚瘝
     */
    
    
    public void parseLikePhraseQuery(String columnName,String parseText){
        try {
            //String parseString = "\"閽㈤搧渚燶"瑁呭";
            QueryParser parser = new QueryParser(Version.LUCENE_46, columnName,dataBaseDefaultConfig.getAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(parseText);
            System.out.println("query:"+query);
            TopDocs topDocs = getSearcher().search(query, sizes);
            System.out.println("鎵惧埌璁板綍鏁�:"+topDocs.totalHits);
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
     * parser鍜宖uzzyQuery
     */
    
    public void parseLikeFuzzyQuery(String columnName,String text){
        try {
            //String parseStr = "閽㈤搧渚爚0.6";
            QueryParser parser = new QueryParser(Version.LUCENE_46,columnName,dataBaseDefaultConfig.getAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(text);
            TopDocs topDocs = getSearcher().search(query, sizes);
            System.out.println("鎵惧埌璁板綍鏁帮細"+topDocs.totalHits);
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
     * 澶氬煙鏌ヨ
     */
    
    
    public void multiFieldQueryParser(String [] queries,String [] fields,BooleanClause.Occur [] booleanClauseOccur){
        try {
            //String bookNameString = "閽㈤搧鏄�庢牱鐐兼垚 鐨�";
            //String bookPriceString = "[70 TO 80]";
            //String [] queries = new String []{bookNameString,bookPriceString};
            //String [] fields = {"bookName","bookPrice"};
            //BooleanClause.Occur [] booleanClauseOccur = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
            Analyzer ikAnalyzer = new IKAnalyzer();
            Query query = MultiFieldQueryParser.parse(Version.LUCENE_46, queries, fields,booleanClauseOccur,ikAnalyzer);
            System.out.println("query:"+query);
            TopDocs topDocs = getSearcher().search(query,sizes);
            System.out.println("鎵惧埌璁板綍鏁�:"+topDocs.totalHits);
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
            //String bookNameString = "閽㈤搧鏄�庢牱鐐兼垚 鐨�";
            //String bookPriceString = "[70 TO 80]";
            //String [] queries = new String []{bookNameString,bookPriceString};
            //String [] fields = {"bookName","bookPrice"};
            //BooleanClause.Occur [] booleanClauseOccur = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
            QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_46,columnNames ,dataBaseDefaultConfig.getAnalyzer()); 
            System.out.println("query:"+queryParser);
            Query  query = queryParser.parse(text);
            TopDocs topDocs = getSearcher().search(queryParser.parse(text),sizes);
            System.out.println("鎵惧埌璁板綍鏁�:"+topDocs.totalHits);
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
        selectHandle.multiFieldQueryParser("1007","id","title","content");
	}
}
