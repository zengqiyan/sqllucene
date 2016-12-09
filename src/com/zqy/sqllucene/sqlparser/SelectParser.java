package com.zqy.sqllucene.sqlparser;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class SelectParser{
	private static final SelectParser single = new SelectParser();  
    //静态工厂方法   单例
    public static SelectParser getInstance() {  
        return single;  
    }
	public SelectBox selectParser(String sql){
		LinkedList wheres = new LinkedList();
		SelectBox selectBox = new SelectBox();
		try {
			CCJSqlParserManager pm = new CCJSqlParserManager();
			Select select = (Select) pm.parse(new StringReader(sql));
			PlainSelect plainSelect =  (PlainSelect)select.getSelectBody();
			//获取查询字段
			List<String> queryColumns = new ArrayList<String>();
			if(plainSelect.getSelectItems()!=null){
				for(SelectItem selectItem:plainSelect.getSelectItems()){
					queryColumns.add(selectItem.toString());
				}
			}
			//Collections.reverse(queryColumns);
			selectBox.setQueryColumns(queryColumns);
			//获取查询表
		    List<String[]> tables = new ArrayList<String[]>();
			Table table=null;
			if(plainSelect.getFromItem() instanceof Table){
				 table = (Table) plainSelect.getFromItem();
				 String aliaName = "";
				 if(table.getAlias()!=null){
					aliaName = table.getAlias().getName();
				  }
				 tables.add(new String[]{table.getFullyQualifiedName(),aliaName});
			}
			if(plainSelect.getJoins()!=null){
				for(Join join:plainSelect.getJoins()){
					if(join.getRightItem() instanceof Table){
					    table = (Table) join.getRightItem();
					    String aliaName = "";
					   if(table.getAlias()!=null){
						 aliaName = table.getAlias().getName();
					   }
					    tables.add(new String[]{table.getFullyQualifiedName(),aliaName});
					}
				}
			}
			selectBox.setTables(tables);
			
			if(plainSelect.getWhere()!=null){
				Expression e  = plainSelect.getWhere();
				e = WhereParseUtil.getExpressionWithoutParenthesis(e);
				//where子句解析
				wheres = WhereParseUtil.generateList(e, wheres);
				selectBox.setWheres(wheres);
			}
			//order by
			if(plainSelect.getOrderByElements()!=null){
			List<String> orderBys = new ArrayList<String>();
			for(OrderByElement orderByElement:plainSelect.getOrderByElements()){
				orderBys.add(orderByElement.toString().toLowerCase());
			}
			selectBox.setOrderBys(orderBys);
			}
			if(plainSelect.getLimit()!=null){
				selectBox.setLimitOffset((int)plainSelect.getLimit().getOffset());
				selectBox.setLimitRowCount((int)plainSelect.getLimit().getRowCount());
			}
		} catch (JSQLParserException e1) {
			e1.printStackTrace();
		}
		return selectBox;
	}

	public static void main(String[] args) {
		SelectParser selectParser = new SelectParser();
		selectParser.selectParser("select a.aa,b.bb from abc a,cde b where  a.cde like 'a?c^' and cd=2 or fg=1 and (hi=5 or ju=8) order by abc desc,cde limit 1,10");
		/*HashMap expressionMap = new HashMap<String, Object>();
		expressionMap.put("1", "111");
		expressionMap.put("2", "222");
		expressionMap.put("3", "333");*/
		//SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
		//System.out.println(formatter.format(new Date()));
	}	
}
