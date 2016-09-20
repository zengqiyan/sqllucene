package com.zqy.sqllucene.sqlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zqy.sqllucene.pojo.Expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

public class SelectParser implements SelectVisitor, ExpressionVisitor {
    private List<String> tableNames;
    private List<Expression> expressionList;
    private String columnName=null;
    private String value=null;
    private Expression expression=null;
    private int tempId=0;
    private int tempParentId=0;
    public List selectParser(String sql){
    	try {
    		Select select =  (Select) CCJSqlParserUtil.parse(sql);
			init();
			select.getSelectBody().accept(this);
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    private void init(){
	   tableNames=new ArrayList<String>();
	   expressionList = new ArrayList<Expression>();
	  
    }
	@Override
	public void visit(PlainSelect plainSelect) {
		// TODO Auto-generated method stub
		if(plainSelect.getWhere()!=null){
			plainSelect.getWhere().accept(this);
		}
	}
	@Override
	public void visit(SetOperationList setOpList) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void visit(WithItem withItem) {
		// TODO Auto-generated method stub
		
	}
	
@Override
public void visit(NullValue nullValue) {
	// TODO Auto-generated method stub
	value =nullValue.toString();
}
@Override
public void visit(Function function) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(SignedExpression signedExpression) {
	// TODO Auto-generated method stub
}
@Override
public void visit(JdbcParameter jdbcParameter) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(JdbcNamedParameter jdbcNamedParameter) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(DoubleValue doubleValue) {
	// TODO Auto-generated method stub
	value =doubleValue.toString();
}
@Override
public void visit(LongValue longValue) {
	// TODO Auto-generated method stub
	value =longValue.toString();
}
@Override
public void visit(HexValue hexValue) {
	// TODO Auto-generated method stub
	value =hexValue.toString();
}
@Override
public void visit(DateValue dateValue) {
	// TODO Auto-generated method stub
	value =dateValue.toString();
}
@Override
public void visit(TimeValue timeValue) {
	// TODO Auto-generated method stub
	value = timeValue.getValue().toString();
}
@Override
public void visit(TimestampValue timestampValue) {
	// TODO Auto-generated method stub
	value = timestampValue.getValue().toString();
}
@Override
public void visit(Parenthesis parenthesis) {
	// TODO Auto-generated method stub
	System.out.println(parenthesis.toString());
	if(parenthesis.getExpression()!=null){
		parenthesis.getExpression().accept(this);
	}
}
@Override
public void visit(StringValue stringValue) {
	// TODO Auto-generated method stub
	value = stringValue.getValue();
}
@Override
public void visit(Addition addition) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(Division division) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(Multiplication multiplication) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(Subtraction subtraction) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(AndExpression andExpression) {
	System.out.println(andExpression.getStringExpression());
	tempParentId=tempId;
	addExpression(andExpression.getStringExpression(),null,null);
	if(andExpression.getLeftExpression()!=null){
		andExpression.getLeftExpression().accept(this);
	}
	if(andExpression.getRightExpression()!=null){
		andExpression.getRightExpression().accept(this);
	}
}

@Override
public void visit(Between between) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(EqualsTo equalsTo) {
	System.out.println(equalsTo.getStringExpression());
	if(columnName!=null){
		columnName=null;
	}
	if(value!=null){
	   value=null;
	}
	if(equalsTo.getLeftExpression()!=null){
		equalsTo.getLeftExpression().accept(this);
	}
	if(equalsTo.getRightExpression()!=null){
		equalsTo.getRightExpression().accept(this);
	}
	//System.out.println(equalsTo.toString());
	if(columnName!=null){
		System.out.println("columnName:"+columnName);
	}
	if(value!=null){
		System.out.println("value:"+value);
	}
}
@Override
public void visit(GreaterThan greaterThan) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(GreaterThanEquals greaterThanEquals) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(InExpression inExpression) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(IsNullExpression isNullExpression) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(LikeExpression likeExpression) {
	// TODO Auto-generated method stub
	System.out.println(likeExpression.getStringExpression());
	if(columnName!=null){
		columnName=null;
	}
	if(value!=null){
	   value=null;
	}
	if(likeExpression.getLeftExpression()!=null){
		likeExpression.getLeftExpression().accept(this);
	}
	if(likeExpression.getRightExpression()!=null){
		likeExpression.getRightExpression().accept(this);
	}
	if(columnName!=null){
		System.out.println("columnName:"+columnName);
	}
	if(value!=null){
		System.out.println("value:"+value);
	}
	
}
@Override
public void visit(MinorThan minorThan) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(MinorThanEquals minorThanEquals) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(NotEqualsTo notEqualsTo) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(Column tableColumn) {
	// TODO Auto-generated method stub
	columnName=tableColumn.getFullyQualifiedName();
}
@Override
public void visit(SubSelect subSelect) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(CaseExpression caseExpression) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(WhenClause whenClause) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(ExistsExpression existsExpression) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(AllComparisonExpression allComparisonExpression) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(AnyComparisonExpression anyComparisonExpression) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(Concat concat) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(Matches matches) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(BitwiseAnd bitwiseAnd) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(BitwiseOr bitwiseOr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(BitwiseXor bitwiseXor) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(CastExpression cast) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(Modulo modulo) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(AnalyticExpression aexpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(WithinGroupExpression wgexpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(ExtractExpression eexpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(IntervalExpression iexpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(OracleHierarchicalExpression oexpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(RegExpMatchOperator rexpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(JsonExpression jsonExpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(RegExpMySQLOperator regExpMySQLOperator) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(UserVariable var) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(NumericBind bind) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(KeepExpression aexpr) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(MySQLGroupConcat groupConcat) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(RowConstructor rowConstructor) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(OracleHint hint) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(TimeKeyExpression timeKeyExpression) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(DateTimeLiteralExpression literal) {
	// TODO Auto-generated method stub
	
}
@Override
public void visit(OrExpression orExpression) {
	System.out.println(orExpression.getStringExpression());
	tempParentId=tempId;
	addExpression(orExpression.getStringExpression(),null,null);
	if(orExpression.getLeftExpression()!=null){
		orExpression.getLeftExpression().accept(this);
	}
	if(orExpression.getRightExpression()!=null){
		orExpression.getRightExpression().accept(this);
	}
}
private void addExpression(String expressionName,String type,String value){
	Expression expression = new Expression();
	if(this.tempId==0){
		expression.setId(this.tempId);
	}else{
		expression.setId(this.tempId+1);
		expression.setParentId(this.tempParentId);
		tempId++;
	}
	expression.setExpressionName(expressionName);
	expression.setType(type);
	expression.setValue(value);
	expressionList.add(expression);
}
public static void main(String[] args) {
	SelectParser selectParser = new SelectParser();
	selectParser.selectParser("select * from abc where  cde like 'a?c^' and cd=2 or fg=1 and (hi=5 or ju=8)");
	/*HashMap expressionMap = new HashMap<String, Object>();
	expressionMap.put("1", "111");
	expressionMap.put("2", "222");
	expressionMap.put("3", "333");*/
	for(Expression e:selectParser.expressionList){
		System.out.println("Id:"+e.getId());
		System.out.println("ParentId:"+e.getParentId());
		System.out.println(e.getExpressionName());
	}
	
}
}
