package com.zqy.sqllucene.sqlparser;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zqy.sqllucene.pojo.DeleteBox;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.pojo.SelectBox;

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
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class DeleteParser{
	private static final DeleteParser single = new DeleteParser();  
    //静态工厂方法   单例
    public static DeleteParser getInstance() {  
        return single;  
    }
	public DeleteBox deleteParser(String sql){
		LinkedList wheres = new LinkedList();
		DeleteBox deleteBox = new DeleteBox();
		try {
			CCJSqlParserManager pm = new CCJSqlParserManager();
			Delete delete = (Delete) pm.parse(new StringReader(sql));
			//获取查询表
		    List<String[]> tables = new ArrayList<String[]>();
			for(Table table :delete.getTables()){
				String aliaName = "";
				 if(table.getAlias()!=null){
					aliaName = table.getAlias().getName();
				  }
				 tables.add(new String[]{table.getFullyQualifiedName(),aliaName});
			}
			Table table = delete.getTable();
			String aliaName = "";
			 if(table.getAlias()!=null){
				aliaName = table.getAlias().getName();
			  }
			tables.add(new String[]{table.getFullyQualifiedName(),aliaName});
			deleteBox.setTables(tables);
			if(delete.getWhere()!=null){
				Expression e  = delete.getWhere();
				e = getExpressionWithoutParenthesis(e);
				//where子句解析
				wheres = generateList(e, wheres);
				deleteBox.setWheres(wheres);
			}
			//order by
			if(delete.getOrderByElements()!=null){
			List<String> orderBys = new ArrayList<String>();
			for(OrderByElement orderByElement:delete.getOrderByElements()){
				orderBys.add(orderByElement.toString().toLowerCase());
			}
			deleteBox.setOrderBys(orderBys);
			}
			if(delete.getLimit()!=null){
				deleteBox.setLimitOffset((int)delete.getLimit().getOffset());
				deleteBox.setLimitRowCount((int)delete.getLimit().getRowCount());
			}
		} catch (JSQLParserException e1) {
			e1.printStackTrace();
		}
		return deleteBox;
	}
	public LinkedList generateList(Expression ex , LinkedList wheres){
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
	private Object invokeMethod(Object obj, String methodFunc){
		try {
			Method method = obj.getClass().getMethod(methodFunc, null);
			return method.invoke(obj, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	//解析单个表达式生成比较对象
	protected ObjectExpression processExpression(Expression e){
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
	public Expression getExpressionForSQL(String sql) throws JSQLParserException{
		CCJSqlParserManager pm = new CCJSqlParserManager();
		PlainSelect plainSelect =  (PlainSelect)((Select) pm.parse(new StringReader(sql))).getSelectBody();
		Expression e  = plainSelect.getWhere();
		return getExpressionWithoutParenthesis(e);
	}
	
	/**
	 *  get Expression until instance is not Parenthesis
	 */
	protected Expression getExpressionWithoutParenthesis(Expression ex){
		if(ex instanceof Parenthesis){
			Expression child = ((Parenthesis)ex).getExpression();
			return getExpressionWithoutParenthesis(child);
		}else{
			return ex;
		}
		
	}

    
	public static void main(String[] args) {
		DeleteParser deleteParser = new DeleteParser();
		deleteParser.deleteParser("select a.aa,b.bb from abc a,cde b where  a.cde like 'a?c^' and cd=2 or fg=1 and (hi=5 or ju=8) order by abc desc,cde limit 1,10");
		/*HashMap expressionMap = new HashMap<String, Object>();
		expressionMap.put("1", "111");
		expressionMap.put("2", "222");
		expressionMap.put("3", "333");*/
		//SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
		//System.out.println(formatter.format(new Date()));
	}	
}
