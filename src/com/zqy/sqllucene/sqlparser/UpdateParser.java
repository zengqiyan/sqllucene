package com.zqy.sqllucene.sqlparser;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zqy.sqllucene.pojo.Field;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.pojo.SelectBox;
import com.zqy.sqllucene.pojo.UpdateBox;

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
import net.sf.jsqlparser.statement.update.Update;

public class UpdateParser{
	private static final UpdateParser single = new UpdateParser();  
    //静态工厂方法   单例
    public static UpdateParser getInstance() {  
        return single;  
    }
	public UpdateBox updateParser(String sql){
		LinkedList wheres = new LinkedList();
		UpdateBox updateBox = new UpdateBox();
		try {
			CCJSqlParserManager pm = new CCJSqlParserManager();
			Update update = (Update) pm.parse(new StringReader(sql));
		/*	Select select = update.getSelect();
			PlainSelect plainSelect =  (PlainSelect)select.getSelectBody();
		  
			List<String> queryColumns = new ArrayList<String>();
			if(plainSelect.getSelectItems()!=null){
				for(SelectItem selectItem:plainSelect.getSelectItems()){
					queryColumns.add(selectItem.toString());
				}
			}*/
		  //获取更新字段
			if(update.getColumns()!=null){
				List<Field> fields = new ArrayList<Field>();
				for(net.sf.jsqlparser.schema.Column column:update.getColumns()){
					Field field = new Field();
					field.setColumnName(column.getFullyQualifiedName());
					fields.add(field);
				}
				updateBox.setFields(fields);
			}
			//获取更新的表
		    List<String[]> tables = new ArrayList<String[]>();
				for(Table table :update.getTables()){
					String aliaName = "";
					 if(table.getAlias()!=null){
						aliaName = table.getAlias().getName();
					  }
					 tables.add(new String[]{table.getFullyQualifiedName(),aliaName});
				}
			updateBox.setTables(tables);
			if(update.getWhere()!=null){
				//where子句解析,update暂时只支持单个=条件查询
				Expression e  = update.getWhere();
				ObjectExpression  oe = processExpression(e);
				if(!oe.getExp().equals("=")){
					throw new RuntimeException("update暂时只支持单个=条件查询");
				}
				updateBox.setEqualExpression(oe);
			}
		} catch (JSQLParserException e1) {
			e1.printStackTrace();
		}
		return updateBox;
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
	
	
	


    
	public static void main(String[] args) {
		UpdateParser selectParser = new UpdateParser();
		//selectParser.selectParser("select a.aa,b.bb from abc a,cde b where  a.cde like 'a?c^' and cd=2 or fg=1 and (hi=5 or ju=8) order by abc desc,cde limit 1,10");
		/*HashMap expressionMap = new HashMap<String, Object>();
		expressionMap.put("1", "111");
		expressionMap.put("2", "222");
		expressionMap.put("3", "333");*/
		//SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
		//System.out.println(formatter.format(new Date()));
	}	
}
