package com.zqy.sqllucene.cache;

import java.util.LinkedHashMap;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public class IndexCache {
     private static LinkedHashMap<String,IndexSearcher> cacheIndexSearcherMap;
     private static LinkedHashMap<String,Directory> cacheDirectoryMap;
     static{
    	if(cacheIndexSearcherMap==null){
    		cacheIndexSearcherMap=new LinkedHashMap<String,IndexSearcher>();
    	}
    	if(cacheDirectoryMap==null){
    		cacheDirectoryMap=new LinkedHashMap<String,Directory>();
    	}
     }
     private static int max=10;
     public static int getMax() {
		return max;
	 }
	 public static void setMax(int max) {
		IndexCache.max = max;
	 }
	 //缓存IndexSearcher
     public synchronized static void putCacheIndexSearcher(String dataBaseName,String tableName,IndexSearcher indexSearcher){
    	 if(cacheIndexSearcherMap.size()==max){
    		 IndexSearcher oldSearcher = (IndexSearcher)cacheIndexSearcherMap.entrySet().iterator().next().getValue();
    		 if(oldSearcher!=null){
    			 oldSearcher=null;
    		 }
    		 cacheIndexSearcherMap.remove(cacheIndexSearcherMap.entrySet().iterator().next().getKey());
    	 }
    	 cacheIndexSearcherMap.put(dataBaseName+"."+tableName, indexSearcher);
     }
     public synchronized static void putCacheIndexSearcher(String dataBaseName,String[] tableNames,IndexSearcher indexSearcher){
    	 if(cacheIndexSearcherMap.size()==max){
    		 IndexSearcher oldSearcher = (IndexSearcher)cacheIndexSearcherMap.entrySet().iterator().next().getValue();
    		 if(oldSearcher!=null){
    			 oldSearcher=null;
    		 }
    		 cacheIndexSearcherMap.remove(cacheIndexSearcherMap.entrySet().iterator().next().getKey());
    	 }
    	 StringBuffer tableName = new StringBuffer(dataBaseName+".");
    	 for(String table:tableNames){
    		 tableName.append("[");
    		 tableName.append(table);
    		 tableName.append("]");
    	 }
    	 cacheIndexSearcherMap.put(dataBaseName+"."+tableName, indexSearcher);
     }
     //从缓存中获取IndexSearcher
     public  synchronized static IndexSearcher getCacheIndexSearcher(String dataBaseName,String tableName ){
  		return cacheIndexSearcherMap.get(dataBaseName+"."+tableName);
       }
     public  synchronized static IndexSearcher getCacheIndexSearcher(String dataBaseName,String[] tableNames ){
     	 StringBuffer tableName = new StringBuffer(dataBaseName+".");
     	 for(String table:tableNames){
     		 tableName.append("[");
     		 tableName.append(table);
     		 tableName.append("]");
     	 }
   		return cacheIndexSearcherMap.get(dataBaseName+"."+tableName.toString());
        }
     //缓存Directory
     public synchronized static void putCacheDirectory(String dataBaseName,String tableName,Directory directory){
    	 if(cacheIndexSearcherMap.size()==max){
    		 Directory oldDirectory = (Directory)cacheDirectoryMap.entrySet().iterator().next().getValue();
    		 if(oldDirectory!=null){
    			 oldDirectory=null;
    		 }
    		 cacheDirectoryMap.remove(cacheDirectoryMap.entrySet().iterator().next().getKey());
    	 }
    	 cacheDirectoryMap.put(dataBaseName+"."+tableName, directory);
     }
     public synchronized static void putCacheDirectory(String dataBaseName,String[] tableNames,Directory directory){
    	 if(cacheIndexSearcherMap.size()==max){
    		 Directory oldDirectory = (Directory)cacheDirectoryMap.entrySet().iterator().next().getValue();
    		 if(oldDirectory!=null){
    			 oldDirectory=null;
    		 }
    		 cacheDirectoryMap.remove(cacheDirectoryMap.entrySet().iterator().next().getKey());
    	 }
    	 StringBuffer tableName = new StringBuffer(dataBaseName+".");
    	 for(String table:tableNames){
    		 tableName.append("[");
    		 tableName.append(table);
    		 tableName.append("]");
    	 }
    	 cacheDirectoryMap.put(dataBaseName+"."+tableName, directory);
     }
   //从缓存中获取Directory
     public  synchronized static Directory getCacheDirectory(String dataBaseName,String tableName){
 		return cacheDirectoryMap.get(dataBaseName+"."+tableName);
      }
     
     public  synchronized static Directory getCacheDirectory(String dataBaseName,String[] tableNames ){
    	 StringBuffer tableName = new StringBuffer(dataBaseName+".");
    	 for(String table:tableNames){
    		 tableName.append("[");
    		 tableName.append(table);
    		 tableName.append("]");
    	 }
  		return cacheDirectoryMap.get(dataBaseName+"."+tableName.toString());
       }
    
     public static void main(String[] args) {
		
	}
}
