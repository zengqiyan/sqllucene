package test;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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
	  List<String> list =  new ArrayList<String>();
	  String[] strings = new String[100000000];
	  for(int i=0;i<50000000;i++){
		  list.add("testest");
	  }
	  for(int i=49999999;i<100000000;i++){
		  list.add("testest1");
	  }
	  for(int i=0;i<50000000;i++){
		  strings[i]="testest";
	  }
	  for(int i=49999999;i<100000000;i++){
		  strings[i]="testest1";
	  }
	  String[] stringss = new String[]{"testest","testest1"};
	  long start = System.currentTimeMillis();
	  for(int i=0;i<stringss.length;i++){
			for(int j=0;j<list.size();j++){
					if(stringss.equals(list.get(i))){
						list.remove(j);
					}
			}
			
		}
	  long middle = System.currentTimeMillis();
	  for(int i=0;i<stringss.length;i++){
			for(int j=0;j<strings.length;j++){
					if(stringss.equals(strings[i])){
					}
			}
			
		}
	  long end = System.currentTimeMillis();
	  System.out.println("list:"+(middle-start));
	  System.out.println("string[]:"+(end-middle));
   }
}
