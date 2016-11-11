package com.zqy.sqllucene.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;

public class IndexHandle {
	private static DataBaseDefaultConfig dataBaseDefaultConfig = DataBaseDefaultConfig.getInstance();
	private static IndexSearcher getSearcher(String dataBaseName,String[] tableNames) {
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
	 private static IndexSearcher getSearcher(String dataBaseName,String tableName) {
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
}
