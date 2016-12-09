package com.zqy.sqllucene.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.zqy.sqllucene.cache.IndexCache;
import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.pojo.DataBase;

public class IndexHandle {
	private static DataBaseDefaultConfig dataBaseDefaultConfig = DataBaseDefaultConfig.getInstance();
	public static IndexSearcher getSearcher(String dataBaseName,String[] tableNames) {
	   IndexSearcher indexSearcher = IndexCache.getCacheIndexSearcher(dataBaseName, tableNames);
	   if(indexSearcher!=null)return indexSearcher;
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
			indexSearcher = new IndexSearcher(new MultiReader(indexReaders));
			IndexCache.putCacheIndexSearcher(dataBaseName, tableNames, indexSearcher);
       return indexSearcher;
   }
	 public static IndexSearcher getSearcher(String dataBaseName,String tableName) {
	   IndexSearcher indexSearcher = IndexCache.getCacheIndexSearcher(dataBaseName, tableName);
	   if(indexSearcher!=null)return indexSearcher;
       IndexReader indexReader = null;
			try {
				indexReader = DirectoryReader.
						open(FSDirectory.open(new File(dataBaseDefaultConfig.
								getTablePath(dataBaseName, tableName))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			indexSearcher = new IndexSearcher(indexReader);
			IndexCache.putCacheIndexSearcher(dataBaseName, tableName, indexSearcher);
       return indexSearcher;
   }
    public static IndexWriter getIndexWriter(String dataBaseName,String tableName ) throws IOException{
    	Directory directory = IndexCache.getCacheDirectory(dataBaseName, tableName);
    	 //1、创建Directorys
    	if(directory==null){
    		DataBase dataBase = DataBaseDefaultConfig.getInstance().getDataBaseByName(dataBaseName);
        	String tablePath = dataBase.getPath()+"/"+tableName;
        	File tableFile = new File(tablePath);
        	if(!tableFile.exists()){
        		throw new RuntimeException("表不存在！");
        	}
         directory =  FSDirectory.open(tableFile);//在硬盘上生成Directory;
    	}
    	
        //2、创建IndexWriter
    	Analyzer analyzer = DataBaseDefaultConfig.getInstance().getAnalyzer();
        IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_46,analyzer);
        iwConfig.setMaxBufferedDocs(100);  
        IndexWriter writer = new IndexWriter(directory, iwConfig);
        IndexCache.putCacheDirectory(dataBaseName, tableName, directory);
		return writer;
    }
}
