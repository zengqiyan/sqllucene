package test;

import java.util.LinkedList;

import com.zqy.sqllucene.pojo.ObjectExpression;

import net.sf.jsqlparser.parser.CCJSqlParserManager;

public class TestParser {
	public static void main(String[] args) throws Exception {
		SQLParserUtil parser = new SQLParserUtil();
		String sql = "SELECT * FROM D_FILE13 WHERE" +
				"" +
				" keyword is not null" +
				"" +
				" or" +
				" abc>12 " +
				"and " +
				"(D_FILE13.KEYWORD >= '12' AND D_FILE13.HH = '122' AND ((D_FILE13.FLH > '1' OR D_FILE13.GDFS < 2 ) OR D_FILE13.MJ > '秘密') AND 1=1)  ";
//		String sql1 = "SELECT A FROM TAB WHERE B IN (expr1,expr2,expr3)";
		
		LinkedList linkList = parser.getLevelObjectByExpression(sql);

		printFor1(linkList);

	}
	
	private static void printFor(LinkedList linkList){
		for (Object object : linkList) {
			if(object instanceof LinkedList){
				System.out.println("(");
				printFor((LinkedList)object);
				System.out.println(")");
			}
			if(object instanceof String){
				System.out.println(object);
			}
			if(object instanceof ObjectExpression){
				ObjectExpression ob = (ObjectExpression)object;
				System.out.println(ob.getColumnname()+" "+ob.getExp()+" "+ob.getValue());
			}
//			System.out.println(object);
		}
	}
	private static void printFor1(LinkedList linkList){
		for(int i=0;i<linkList.size();i++){
			Object object = linkList.get(i);
			if(object instanceof LinkedList){
				System.out.println("(");
				printFor((LinkedList)object);
				System.out.println(")");
			}
			if(object instanceof String){
				System.out.println(object);
				if(i+1<=linkList.size()){
					 object = linkList.get(i+1);
						if(object instanceof ObjectExpression){
							ObjectExpression ob = (ObjectExpression)object;
							System.out.println(ob.getColumnname()+" "+ob.getExp()+" "+ob.getValue());
						}
					}
				}
			if(i==0){
				if(object instanceof ObjectExpression){
					ObjectExpression ob = (ObjectExpression)object;
					System.out.println(ob.getColumnname()+" "+ob.getExp()+" "+ob.getValue());
				}
			}
//			System.out.println(object);
		}
	}
}
