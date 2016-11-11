package com.zqy.sqllucene.sqlparser;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.LinkedList;

import com.zqy.sqllucene.pojo.ObjectExpression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class WhereParseUtil {
	public static LinkedList generateList(Expression ex , LinkedList wheres){
		if(ex==null){
			return null;
		}
		if(ex instanceof OrExpression||ex instanceof AndExpression){//如果是and or 连接
			BinaryExpression be = (BinaryExpression)ex;
			
			generateList(be.getLeftExpression(), wheres);//设置左侧分离后的表达式
			
			//设置连接符
			wheres.add((ex instanceof OrExpression)?"OR":"AND");
			
			generateList(be.getRightExpression(), wheres);

		}else if(ex instanceof Parenthesis){//括号
			LinkedList childList = new LinkedList();
			Expression exp = getExpressionWithoutParenthesis(ex);//获取括号内 表达式
			wheres.add(childList);
			generateList(exp,childList); //添加下级内容
			
		}else{//单目表达式
			wheres.add(processExpression(ex));
		}
		return wheres;
	}	
	private static Object invokeMethod(Object obj, String methodFunc){
		try {
			Method method = obj.getClass().getMethod(methodFunc, null);
			return method.invoke(obj, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	//解析单个表达式生成比较对象
	protected static ObjectExpression processExpression(Expression e){
		ObjectExpression oe = new ObjectExpression();
		Object columnObj = invokeMethod(e, "getLeftExpression");
		if(columnObj instanceof LongValue){//如果解析后是 1=1
			LongValue longValue = (LongValue)columnObj;
			oe.setColumnname(longValue.getStringValue());
		}else{
			Column column = (Column)invokeMethod(e, "getLeftExpression");
			oe.setColumnname(column.getColumnName());
			
		}
		if (e instanceof BinaryExpression) {//对比表达式
			BinaryExpression be = (BinaryExpression) e;
			oe.setExp(be.getStringExpression());
			if(be.getRightExpression() instanceof Function){
				oe.setValue(invokeMethod(be.getRightExpression(), "toString"));
			}else{
				oe.setValue(invokeMethod(be.getRightExpression(), "getValue"));
			}
		}else{
			oe.setExp((String)invokeMethod(e, "toString"));
		}
		return oe;
	}
	
	//获取第一个不是括号的表达式
	public static Expression getExpressionForSQL(String sql) throws JSQLParserException{
		CCJSqlParserManager pm = new CCJSqlParserManager();
		PlainSelect plainSelect =  (PlainSelect)((Select) pm.parse(new StringReader(sql))).getSelectBody();
		Expression e  = plainSelect.getWhere();
		return getExpressionWithoutParenthesis(e);
	}
	
	/**
	 *  get Expression until instance is not Parenthesis
	 */
	protected static Expression getExpressionWithoutParenthesis(Expression ex){
		if(ex instanceof Parenthesis){
			Expression child = ((Parenthesis)ex).getExpression();
			return getExpressionWithoutParenthesis(child);
		}else{
			return ex;
		}
		
	}
}
