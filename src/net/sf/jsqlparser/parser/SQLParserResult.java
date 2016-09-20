package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.statement.Statement;

/**
 * SqlParser解析结果.
 * @author lixiangyang
 *
 */
public class SQLParserResult {

	private Statement stmt;
	
	private SimpleNode statementNode;

	public Statement getStmt() {
		return stmt;
	}

	public void setStmt(Statement stmt) {
		this.stmt = stmt;
	}
	
	public SimpleNode getStatementNode() {
		return statementNode;
	}

	public void setStatementNode(SimpleNode statementNode) {
		this.statementNode = statementNode;
	}
}
