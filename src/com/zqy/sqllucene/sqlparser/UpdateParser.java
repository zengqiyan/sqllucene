package com.zqy.sqllucene.sqlparser;

import java.util.ArrayList;
import java.util.List;
import com.zqy.sqllucene.cfg.DataBaseDefaultConfig;
import com.zqy.sqllucene.lucenehandle.InsertHandle;
import com.zqy.sqllucene.pojo.Column;
import com.zqy.sqllucene.pojo.Field;
import com.zqy.sqllucene.pojo.ObjectExpression;
import com.zqy.sqllucene.pojo.UpdateBox;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

public class UpdateParser extends BaseParser{
	private String tableName;
	private List<Field> fields;
	public String getTableName(){
		return tableName;
	}
	private static final UpdateParser single = new UpdateParser();  
    //静态工厂方法   单例
    public static UpdateParser getInstance() {  
        return single;  
    }
    public UpdateBox updateParser(String sql){
    	UpdateBox updateBox= new UpdateBox();
    	try {
			Update update =  (Update) CCJSqlParserUtil.parse(sql);
			init();
			update.accept(this);
			List<Column> columnList = 
        			DataBaseDefaultConfig.getInstance().getColumns(dataBaseName, tableName);
			if(fields.size()==columnList.size() && fields.get(0).getColumnName()!=null){
				for(int i=0;i<fields.size();i++){
					Column column = columnList.get(i);
        			Field field = fields.get(i);
					field.setType(column.getType());
	    		}
			}else{
				for(int i=0;i<fields.size();i++){
	        		for(int j=0;j<columnList.size();j++){
	        			Column column = columnList.get(j);
	        			Field field = fields.get(i);
	        			if(column.getName().equals(field.getColumnName())){
	        				field.setColumnName(column.getName());
	        				field.setType(column.getType());
	        				//columnList.remove(j);
	        				break;
	        			}
	        		}
	    		}
			}
			updateBox.setFields(fields);
			if(update.getWhere()!=null){
				//where子句解析,update
				Expression e  = update.getWhere();
				ObjectExpression  equalExpression = WhereParseUtil.processExpression(e);
				if(!equalExpression.getExp().equals("=")){
					throw new RuntimeException("where子句暂时只支持单个=查询");
				}
				updateBox.setEqualExpression(equalExpression);
			}
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateBox;
    }
    public UpdateBox updateParser(String dataBaseName,String sql){
    	UpdateBox updateBox= new UpdateBox();
    	try {
    		Update update =  (Update) CCJSqlParserUtil.parse(sql);
			init();
			this.dataBaseName = dataBaseName;
			update.accept(this);
			List<Column> columnList = 
        			DataBaseDefaultConfig.getInstance().getColumns(dataBaseName, tableName);
			if(fields.size()==columnList.size() && fields.get(0).getColumnName()!=null){
				for(int i=0;i<fields.size();i++){
					Column column = columnList.get(i);
        			Field field = fields.get(i);
					field.setType(column.getType());
	    		}
			}else{
				for(int i=0;i<fields.size();i++){
	        		for(int j=0;j<columnList.size();j++){
	        			Column column = columnList.get(j);
	        			Field field = fields.get(i);
	        			if(column.getName().equals(field.getColumnName())){
	        				field.setColumnName(column.getName());
	        				field.setType(column.getType());
	        				columnList.remove(j);
	        				break;
	        			}
	        		}
	    		}
			}
			updateBox.setFields(fields);
			if(update.getWhere()!=null){
				//where子句解析,update
				Expression e  = update.getWhere();
				ObjectExpression  equalExpression = WhereParseUtil.processExpression(e);
				if(!equalExpression.getExp().equals("=")){
					throw new RuntimeException("where子句暂时只支持单个=查询");
				}
				updateBox.setEqualExpression(equalExpression);
			}
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateBox;
    }
    
    private void init(){
    	fields=new ArrayList<Field>();
     }
	@Override
	public void visit(Select select) {
		// TODO Auto-generated method stub
	}
	@Override
	public void visit(Update update) {
		if( dataBaseName==null || "".equals(dataBaseName)){
			String FullyQualifiedName=update.getTables().get(0).getFullyQualifiedName();
			dataBaseName=FullyQualifiedName.substring(0, FullyQualifiedName.indexOf("."));
			tableName = FullyQualifiedName.substring(FullyQualifiedName.indexOf(".")+1,FullyQualifiedName.length());
		}else{
			tableName = update.getTables().get(0).getName();
		}
		if(update.getColumns()!=null){
			for(net.sf.jsqlparser.schema.Column column:update.getColumns()){
				Field field = new Field();
				field.setColumnName(column.getFullyQualifiedName());
				fields.add(field);
			}
		}
		
		for(Expression e:update.getExpressions()){
			e.accept(this);
		}
	}
    private int valueIndex; 
	public void visit(ExpressionList expressionList) {
		valueIndex=0;
		if(expressionList.getExpressions()!=null){
			for(Expression expression: expressionList.getExpressions()){
				expression.accept(this);
			}
		}
		
	}
	public void visit(NullValue nullValue) {
		fields.get(valueIndex).setValue(nullValue.toString());
		valueIndex++;
	}
	public void visit(DoubleValue doubleValue) {
		fields.get(valueIndex).setValue(doubleValue.getValue());
		//fields.get(valueIndex).setType("double");
		valueIndex++;
	}
	
	public void visit(LongValue longValue) {
		fields.get(valueIndex).setValue(longValue.getValue());
		//fields.get(valueIndex).setType("long");
		valueIndex++;
	}
	
	public void visit(HexValue hexValue) {
		fields.get(valueIndex).setValue(hexValue.getValue());
		//fields.get(valueIndex).setType("hex");
		valueIndex++;
	}

	public void visit(DateValue dateValue) {
		fields.get(valueIndex).setValue(dateValue.getValue());
		//fields.get(valueIndex).setType("date");
		valueIndex++;
	}

	public void visit(TimeValue timeValue) {
		fields.get(valueIndex).setValue(timeValue.getValue());
		//fields.get(valueIndex).setType("timeValue");
		valueIndex++;
	}

	public void visit(TimestampValue timestampValue) {
		fields.get(valueIndex).setValue(timestampValue.getValue());
		//fields.get(valueIndex).setType("timeValue");
		valueIndex++;
	}
	public void visit(Parenthesis parenthesis) {
	}
	public void visit(StringValue stringValue) {
		fields.get(valueIndex).setValue(stringValue.getValue());
		//fields.get(valueIndex).setType("string");
		valueIndex++;
	}
	public static void main(String[] args) {
		 InsertHandle insertHandle= new InsertHandle();
		 insertHandle.insert("testDatabase", "insert into testTable values(1002,'mytitle3','注：注：kclbm、kcid、nrid三个参数只需传其中一个，若传递多个参数优先级是：nrid > kcid > kclbmkclbm、kcid、nrid三个参数只需传其中一个，若传递多个参数优先级是：nrid > kcid > kclbm')");
	}
}
