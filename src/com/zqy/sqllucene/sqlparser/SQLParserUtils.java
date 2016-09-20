package com.zqy.sqllucene.sqlparser;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.SQLParserResult;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;





/**
 * SQL解析工具类.
 * @author lixiangyang.
 *
 */
public class SQLParserUtils {
	
	/**
	 * 日志.
	 */
	
	
	/**
	 * SQL解析管理器.
	 */
	private static CCJSqlParserManager parserManager = new CCJSqlParserManager();
	
	/**
	 * 获取SELECT对象.
	 * @param selectString SELECT查询语句.
	 * @return SELECT对象.
	 * @throws JSQLParserException SQL解析异常 .
	 */
	public static Select parser(final String selectString) {
		Select selectStatement = null;
		try {
			Statement stmt = parserManager.parse(new StringReader(selectString));
			if (stmt instanceof Select) {
				selectStatement = (Select)stmt;
			}
		} catch (JSQLParserException e) {
			e.printStackTrace();
		}
		
		return selectStatement;
	}
	
	/**
	 * 获取WHERE表达式对象.
	 * @param whereString WHERE条件语句.
	 * @return WHERE表达式对象.
	 * @throws JSQLParserException SQL解析异常.
	 */
	public static Expression parserWhere(final String whereString) {
		Expression whrExp = null;
		try {
			if (whereString != null && whereString.length() > 0) {
				if (whereString.trim().toUpperCase().startsWith("WHERE")) {
					whrExp = parserManager.parseWhere(whereString);
				} else {
					whrExp = parserManager.parseWhere("WHERE " + whereString);
				}
			}
		} catch (JSQLParserException e) {
		
		}
		
		return whrExp;
	}
	
	/**
	 * 获取SQL解析结果对象.
	 * @param sqlString SQL语句.
	 * @return SQL解析结果对象.
	 * @throws JSQLParserException SQL解析异常 .
	 */
	public static SQLParserResult parseWithTree(final String sqlString) {
		SQLParserResult parserResult = null;
		try {
			parserResult = parserManager.parseWithTree(sqlString);
		} catch (JSQLParserException e) {
			
		}
		
		return parserResult;
	}
	
	/**
	 * 据节点值查找对应的节点.
	 * @param nodeValue 节点值.
	 * @param node 节点.
	 * @return 值对应的节点.
	 */
	public static SimpleNode findNodeByValue(Object nodeValue, SimpleNode node) {
		  SimpleNode findNode = null;
		  if (node.jjtGetValue() != null && node.jjtGetValue() == nodeValue) {
			  findNode = node;
		  } else if (node.jjtGetNumChildren() > 0) {
			  SimpleNode childNode = null;
		      for (int i = 0; i < node.jjtGetNumChildren(); i++) {
		        childNode = (SimpleNode)node.jjtGetChild(i);
		        findNode = findNodeByValue(nodeValue, childNode);
		        if (findNode != null) {
		        	break;
		        }
		      }
		  }
		  
		  return findNode;
	}
	
	/**
	 * 递归查询上级节点中相应类型的对象.
	 * @param objectClazz 要查找的节点中值的类型 .
	 * @param curNode 当前节点.
	 * @return 相应类型的对象.
	 */
	public static Object findParentByType(Class objectClazz, SimpleNode curNode) {
		final SimpleNode parentNode = (SimpleNode) curNode.jjtGetParent();
		if (parentNode != null) {
			final Object object = parentNode.jjtGetValue();
			if (object != null) {
				if (object.getClass().equals(objectClazz)) {
					return object;
				} else {
					return findParentByType(objectClazz, parentNode);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 据FromItem部分查找WHERE条件 部分.
	 * @param fromItem FromItem部分.
	 * @param sqlParserRootNode 根节点，包含所有下级节点.
	 * @return WHERE条件部分.
	 */
	public static Expression findWhereByFromItem(FromItem fromItem, SimpleNode sqlParserRootNode) {
		Expression where = null;
		if (sqlParserRootNode != null) {
			final SimpleNode fromItemNode = SQLParserUtils.findNodeByValue(fromItem, sqlParserRootNode);
			final Object object = findParentByType(PlainSelect.class, fromItemNode);
			if (object != null) {
				where = ((PlainSelect) object).getWhere();
			}
		}
		
		return where;
	}
	
	/**
	 * 追加Where条件语句.
	 * @param originWhrExp 原始条件语句.
	 * @param appendWhereString 追加的Where条件语句.
	 * @return 追加后的Where条件语句.
	 */
	public static Expression appendWhere(final Expression originWhrExp, final String appendWhereString) {
		Expression whrExp = null;
		final Expression appendWhrExp = SQLParserUtils.parserWhere(appendWhereString);
		if (originWhrExp != null) {
			final Parenthesis leftExpression = addParenthesis(originWhrExp);
			final Parenthesis rightExpression = addParenthesis(appendWhrExp);
			whrExp = new AndExpression(leftExpression, rightExpression);
		} else {
			whrExp = appendWhrExp;
		}
		
		return whrExp;
	}
	
	/**
	 * 为Expression表达式加圆括号.
	 * @param expression 表达式对象.
	 * @return 加圆括号后的表达式对象.
	 */
	public static Parenthesis addParenthesis(final Expression expression) {		
		return (expression instanceof Parenthesis) ? (Parenthesis)expression : new Parenthesis(expression);
	}
	public static void main(String[] args) {
		SQLParserResult sqlParserResult = SQLParserUtils.parseWithTree("select * from abc where 1=1 or 2=2 ");
		System.out.println(sqlParserResult.getStatementNode());
	}
}
