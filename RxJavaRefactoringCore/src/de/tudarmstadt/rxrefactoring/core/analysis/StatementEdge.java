package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class StatementEdge extends SimpleEdge<Statement> {

	
	public StatementEdge(Statement head, Statement tail) {
		super(head, tail);
	}

	@Override
	public String toString() {
		return "Edge(" + statementAsNode(getHead()) + ", " +  statementAsNode(getTail()) + ")";
	}
	
	private String statementAsNode(Statement stmt) {
		
		class StatementVisitor extends ASTVisitor {
			String result = null;
			
			@Override
			public boolean visit(IfStatement node) {
				result = "if " + Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(ExpressionStatement node) {
				result = Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(ReturnStatement node) {
				result = node.getExpression() == null ? "return" : "return " + Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(VariableDeclarationStatement node) {
				result = Objects.toString(node).substring(0, Objects.toString(node).length() - 2); //Remove ; and newline from end of string
				return false;
			}
			
			
			
//			 *    {@link AssertStatement},
//			 *    {@link Block},
//			 *    {@link BreakStatement},
//			 *    {@link ConstructorInvocation},
//			 *    {@link ContinueStatement},
//			 *    {@link DoStatement},
//			 *    {@link EmptyStatement},
//			 *    {@link EnhancedForStatement}
//			 *    {@link ForStatement},
//			 *    {@link LabeledStatement},
//			 *    {@link SuperConstructorInvocation},
//			 *    {@link SwitchCase},
//			 *    {@link SwitchStatement},
//			 *    {@link SynchronizedStatement},
//			 *    {@link ThrowStatement},
//			 *    {@link TryStatement},
//			 *    {@link TypeDeclarationStatement},
//			 *    {@link WhileStatement}
			
		}
		
		StatementVisitor v = new StatementVisitor();
		stmt.accept(v);
		
		
		return v.result;
		
	}

}
