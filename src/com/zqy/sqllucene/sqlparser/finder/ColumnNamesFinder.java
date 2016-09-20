/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package com.zqy.sqllucene.sqlparser.finder;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;

import java.util.ArrayList;
import java.util.List;


import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

/**
 * Find all used tables within an select statement.
 */
public class ColumnNamesFinder extends BaseFinder{

	private ColumnNamesFinder(){		
		
	}
	private static final ColumnNamesFinder single = new ColumnNamesFinder();  
    //静态工厂方法   单例
    public static ColumnNamesFinder getInstance() {  
        return single;  
    }
    private List<String> columns;
    
    public List<String> getColumnsList(Statement statement) {
        init();
        statement.accept(this);
        return columns;
    }
    public List<String> getColumnsList(Expression expr) {
        init();
        expr.accept(this);
        return columns;
    }
  
    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                item.accept(this);
            }
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                join.getRightItem().accept(this);
            }
        }
       /* if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }*/
        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(this);
        }
    }

    @Override
    public void visit(Column tableColumn) {
    	columns.add(tableColumn.getFullyQualifiedName());
    }

  
    protected void init() {
        otherItemNames = new ArrayList<String>();
        columns = new ArrayList<String>();
    }

    
    @Override
    public void visit(Update update) {
        for (Column column : update.getColumns()) {
        	columns.add(column.getColumnName());
        }
        if (update.getExpressions() != null) {
            for (Expression expression : update.getExpressions()) {
                expression.accept(this);
            }
        }

        if (update.getFromItem() != null) {
            update.getFromItem().accept(this);
        }

        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                join.getRightItem().accept(this);
            }
        }

        /*if (update.getWhere() != null) {
            update.getWhere().accept(this);
        }*/
    }

    @Override
    public void visit(Insert insert) {
    	if(!insert.getColumns().isEmpty()){
   		 for (Column column : insert.getColumns()) {
   	         	columns.add(column.getColumnName());
   	         }
   	    }
        if (insert.getItemsList() != null) {
            insert.getItemsList().accept(this);
        }
        if (insert.getSelect() != null) {
            visit(insert.getSelect());
        }
    }

    @Override
    public void visit(Replace replace) {
    	if(!replace.getColumns().isEmpty()){
    		 for (Column column : replace.getColumns()) {
    	         	columns.add(column.getColumnName());
    	         }
    	}
        
        if (replace.getExpressions() != null) {
            for (Expression expression : replace.getExpressions()) {
                expression.accept(this);
            }
        }
        if (replace.getItemsList() != null) {
            replace.getItemsList().accept(this);
        }
    }

    @Override
    public void visit(CreateTable create) {
    	if(!create.getColumnDefinitions().isEmpty()){
      		 for (ColumnDefinition column : create.getColumnDefinitions()) {
      	         	columns.add(column.getColumnName());
      	         	System.out.println(column.getColDataType());
      	         	System.out.println(column.getColumnSpecStrings());
      	         }
      	    }
    	
    	
        if (create.getSelect() != null) {
            create.getSelect().accept(this);
        }
    }
}
