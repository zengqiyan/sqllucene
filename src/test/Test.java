package test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zqy.sqllucene.lucenehandle.InsertHandle;
import com.zqy.sqllucene.sqlparser.finder.ColumnNamesFinder;
import com.zqy.sqllucene.sqlparser.finder.TableNamesFinder;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Select;


public class Test {
   public  void test(){
	   String sqls = "select * from table1";
	   CCJSqlParser parser = new CCJSqlParser(new StringReader(sqls));
	   try {
		System.out.println(parser.Statement());
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   public static String read(File file) throws IOException{
	   InputStreamReader read = new InputStreamReader(new FileInputStream(file),"gbk");       
       BufferedReader bufferedReader=new BufferedReader(read);  
	  String str="";
	  StringBuffer sb = new StringBuffer();
	  while((str=bufferedReader.readLine())!=null){
		  sb.append(str);
	  }
	  read.close();
	  bufferedReader.close();
	return sb.toString();
   }
   public static int count(String text) {
       String Reg="^[\u4e00-\u9fa5]{1}$";//正则
       int result=0;
       for(int i=0;i<text.length();i++){
        String b=Character.toString(text.charAt(i));
        if(b.matches(Reg))result++;
       }
       return result;
   }
   public static boolean status=true;
   public static void main(String[] args){
	   new Thread((Runnable)()->{
		   try {
			while(status){
				Thread.sleep(1000);
				System.out.println("test");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}).start();
	   try {
		Thread.sleep(10000);
		status=false;
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	// System.out.println(222);
   }
}
