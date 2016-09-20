package test;
import java.io.StringReader;
import java.util.List;

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
   public static void main(String[] args) throws JSQLParserException {
	  
   }
}
