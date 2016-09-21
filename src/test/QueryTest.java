package test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;




public class QueryTest {
    private static IndexSearcher indexSearcher;
    
    /*
     * 读取索引
     */
    static {
        try {
            Directory directory = FSDirectory.open(new File(
                    ""));
            IndexReader reader = IndexReader.open(directory);
            indexSearcher = new IndexSearcher(reader);
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * term为查询的最小单位，termQuery的查询字符串将作为一个整体，不会被分词
     */
    
    public void termQuery() {
        try {
            Term term = new Term("bookName", "迪士尼");
            TermQuery termQuery = new TermQuery(term);
            System.out.println(termQuery);
            TopDocs topdocs = indexSearcher.search(termQuery, 100000);
            System.out.println("共找到" + topdocs.scoreDocs.length + ":条记录");
            for (ScoreDoc scoreDocs : topdocs.scoreDocs) {
                int documentId = scoreDocs.doc;
                Document document = indexSearcher.doc(documentId);
                System.out.println(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 布尔查询，当出现  与/或时可以使用该查询
     */
    
    public void booleanQuery() {
        try {
            Term term1 = new Term("bookName", "钢铁");
            Term term2 = new Term("bookName", "炼成");
            TermQuery termQuery1 = new TermQuery(term1);
            TermQuery termQuery2 = new TermQuery(term2);
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(termQuery1, Occur.MUST);
            booleanQuery.add(termQuery2, Occur.MUST_NOT);
            System.out.println("query:"+booleanQuery);
            TopDocs topdocs = indexSearcher.search(booleanQuery, 20);
            System.out.println("共找到" + topdocs.scoreDocs.length + ":条记录");
            for (ScoreDoc scoreDocs : topdocs.scoreDocs) {
                int documentId = scoreDocs.doc;
                Document document = indexSearcher.doc(documentId);
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

    
    public void parser() {
        try {
            String bookName = "钢铁侠";
            QueryParser parser = new QueryParser(Version.LUCENE_46, "bookName",
                    new IKAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(bookName);
            System.out.println("query:" + query);
            TopDocs topdocs = indexSearcher.search(query, 10000);
            System.out.println("共找到" + topdocs.scoreDocs.length + ":条记录");
            for (ScoreDoc scoreDocs : topdocs.scoreDocs) {
                int documentId = scoreDocs.doc;
                Document document = indexSearcher.doc(documentId);
                System.out.println(document);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * 范围查询（可以是日期，数字）
     * 格式为[number1 TO number2]
     * [date1 TO date2]
     */

    
/*    public void rangeQuery() {
        try {
            TermRangeQuery rangeQuery = new TermRangeQuery("bookPrice", "50.00",
                    "100.00", true, true);
            System.out.println(rangeQuery.toString());
            TopDocs topDocs = indexSearcher.search(rangeQuery, 10000);
            System.out.println("共找到:" + topDocs.totalHits + "条记录");
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
                System.out.println(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
    
    /*
     * parser相当于rangeQuery
     */

    
    public void rangeQueryParser() {
        try {

            String queryString = "[40 TO 42]";
            QueryParser parser = new QueryParser(Version.LUCENE_36,
                    "bookPrice", new IKAnalyzer());
            Query query = parser.parse(queryString);

            System.out.println("query:" + query.toString());
            TopDocs topDocs = indexSearcher.search(query, 10000);
            System.out.println("共找到:" + topDocs.totalHits + "条记录");
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
                System.out.println(document);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    
    /*public void ikParser() {
        try {
            String queryString = "钢铁侠  bookDisAccount:56折";
            Query query = IKQueryParser.parse("bookName", queryString);
            System.out.println("query:" + query.toString());
            TopDocs topDoc = indexSearcher.search(query, 10000);
            System.out.println("共找到:" + topDoc.totalHits + "条记录");
            for (ScoreDoc socreDoc : topDoc.scoreDocs) {
                int docId = socreDoc.doc;
                System.out.println("文档评分:"
                        + indexSearcher.explain(query, docId).getValue());
                Document document = indexSearcher.doc(docId);
                System.out.println(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*
     * 前缀查询
     */
    
    public void prefixQuery() {
        String queryString = "钢铁";
        try {
            Term prefixTerm = new Term("bookName", queryString);
            PrefixQuery query = new PrefixQuery(prefixTerm);
            TopDocs topDoc = indexSearcher.search(query, 10000);
            System.out.println("找到记录数:" + topDoc.totalHits);
            for (ScoreDoc scoreDoc : topDoc.scoreDocs) {
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    public void phraseQuery() {
        try {
            Term term1 = new Term("bookName", "雅");
            Term term2 = new Term("bookName", "钢铁");
            PhraseQuery phraseQuery = new PhraseQuery();
            phraseQuery.add(term1);
            phraseQuery.add(term2);
            phraseQuery.setSlop(10);
            System.out.println(phraseQuery.toString());
            TopDocs topDocs = indexSearcher.search(phraseQuery, 100);
            System.out.println("找到记录数:" + topDocs.totalHits);
            for (ScoreDoc score : topDocs.scoreDocs) {
                int docId = score.doc;
                Document document = indexSearcher.doc(docId);
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
    
    
    public void multiPhraseQuery(){
        try {
            String queryStr1 = "钢铁";
            String queryStr2 = "侠";
            String queryStr3 = "情缘";
            Term term1 = new Term("bookName", queryStr1);
            Term term2 = new Term("bookName",queryStr2);
            Term term3 = new Term("bookName",queryStr3);
            MultiPhraseQuery multiPhraseQuery = new MultiPhraseQuery();
            multiPhraseQuery.setSlop(10);
            multiPhraseQuery.add(term1);
            multiPhraseQuery.add(new Term[]{term2,term3});
            TopDocs topDocs = indexSearcher.search(multiPhraseQuery, 200);
            System.out.println("找到记录数:"+topDocs.totalHits);
            for(ScoreDoc scoreDoc : topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    public void fuzzyQuery(){
        try {
            String queryString = "男人";
            Term term = new Term("bookName",queryString);
            FuzzyQuery fuzzyQuery = new FuzzyQuery(term,1);
            TopDocs topDocs = indexSearcher.search(fuzzyQuery, 10);
            System.out.println("找到记录数:"+topDocs.totalHits);
            for(ScoreDoc scoreDoc :topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
                System.out.println(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    /*
     * 通配符查询
     */
    
    
    public void wildcardQuery(){
        try {
            String queryString = "?品*";
            Term term = new Term("bookName",queryString);
            WildcardQuery wildcardQuery = new WildcardQuery(term);
            TopDocs topDocs = indexSearcher.search(wildcardQuery, 10);
            System.out.println("找到记录数："+topDocs.totalHits);
            for(ScoreDoc scoreDoc : topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    
    public void spanFirstQuery(){
        try {
            String queryString = "大王";
            Term term = new Term("bookName",queryString);
            SpanTermQuery spanTermQuery = new SpanTermQuery(term);
            SpanFirstQuery spanFirstQuery = new SpanFirstQuery(spanTermQuery,2);
            TopDocs topDocs =  indexSearcher.search(spanFirstQuery,10);
            System.out.println("找到记录:"+topDocs.totalHits);
            for(ScoreDoc scoreDoc :topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    
    public void regexQuery(){
        String queryString = "^钢铁";
        Term t = new Term("bookName",queryString);
        RegexQuery regexQuery = new RegexQuery(t);
        try {
            TopDocs topDocs = indexSearcher.search(regexQuery, 10);
            System.out.println("找到数据："+topDocs.totalHits);
            for(ScoreDoc scoreDoc : topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    
    public void parseLikePhraseQuery(){
        try {
            String parseString = "\"钢铁侠\"装备";
            QueryParser parser = new QueryParser(Version.LUCENE_36, "bookName", new IKAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(parseString);
            System.out.println("query:"+query);
            TopDocs topDocs = indexSearcher.search(query, 10);
            System.out.println("找到记录数:"+topDocs.totalHits);
            for(ScoreDoc scoreDoc :topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    public void parseLikeFuzzyQuery(){
        try {
            String parseStr = "钢铁侠~0.6";
            QueryParser parser = new QueryParser(Version.LUCENE_36,"bookName",new IKAnalyzer());
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(parseStr);
            TopDocs topDocs = indexSearcher.search(query, 10);
            System.out.println("找到记录数："+topDocs.totalHits);
            for(ScoreDoc scoreDoc :topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    
    public void multiFieldQueryParser(){
        try {
            String bookNameString = "钢铁是怎样炼成 的";
            String bookPriceString = "[70 TO 80]";
            String [] queries = new String []{bookNameString,bookPriceString};
            String [] fields = {"bookName","bookPrice"};
            BooleanClause.Occur [] booleanClauseOccur = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
            Analyzer ikAnalyzer = new IKAnalyzer();
            Query query = MultiFieldQueryParser.parse(Version.LUCENE_36, queries, fields,booleanClauseOccur,ikAnalyzer);
            System.out.println("query:"+query);
            TopDocs topDocs = indexSearcher.search(query, 80);
            System.out.println("找到记录数:"+topDocs.totalHits);
            Highlighter highLighter = new Highlighter(new QueryScorer(query));
            for(ScoreDoc scoreDoc : topDocs.scoreDocs){
                int docId = scoreDoc.doc;
                Document document = indexSearcher.doc(docId);
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
    
    
   
    


}
