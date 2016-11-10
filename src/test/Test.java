package test;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
   public static void main(String[] args) throws  ClassNotFoundException, SQLException {
	   Class.forName("oracle.jdbc.driver.OracleDriver");
	   String url="jdbc:oracle:thin:@192.168.1.234:1521:zhyz";
	   String user="zhyz";
	   String password="111";
	   Connection con = DriverManager.getConnection(url, user, password);
	   PreparedStatement pre = con.prepareStatement("select ryid,xbm,rylbm,xm,sfzh,csrq from ry");// 创建预编译语句对象，一般都是用这个而不用Statement
	   ResultSet result  = pre.executeQuery();
	   InsertHandle insertHandle= new InsertHandle();
		 //insertHandle.insert("testDatabase", "insert into testTable(id,title,content) values(1010,'mytitle5','查询正则')");
	   while(result.next()){
		   insertHandle.insert("testDatabase", "insert into ry values('"+result.getString("ryid")+"','"+result.getString("xbm")+"'"
		   		+ "            ,'"+result.getString("rylbm")+"','"+result.getString("xm")+"','"+result.getString("sfzh")+"'"
		   				+      ",'"+result.getString("csrq")+"')");
	   }
   }
}
