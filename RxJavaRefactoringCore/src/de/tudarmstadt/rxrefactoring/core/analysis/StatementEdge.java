package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

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
			String result = "STRING NOT AVAILABLE";
			
			@Override
			public boolean visit(IfStatement node) {
				result = "if " + Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(WhileStatement node) {
				result = "while " + Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(DoStatement node) {
				result = "dowhile " + Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(ExpressionStatement node) {
				result = Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(ReturnStatement node) {
				result = node.getExpression() == null ? "<return>" : "<return " + Objects.toString(node.getExpression()) + ">";
				return false;
			}
			
			@Override
			public boolean visit(VariableDeclarationStatement node) {
				result = Objects.toString(node).substring(0, Objects.toString(node).length() - 2); //Remove ; and newline from end of string
				return false;
			}
			
			@Override
			public boolean visit(EmptyStatement node) {
				result = "<nop>";
				return false;
			}
			
			@Override
			public boolean visit(ForStatement node) {
				result = "for " + Objects.toString(node.getExpression()) + " with " + node.initializers() + " do " + node.updaters();
				return false;
			}
			
			@Override
			public boolean visit(AssertStatement node) {
				return false;
			}
			
			@Override
			public boolean visit(Block node) {
				result = "<block size=" + node.statements().size() + ">";
				return false;
			}
			
			@Override
			public boolean visit(BreakStatement node) {
				result = "<break>";
				return false;
			}
			
			@Override
			public boolean visit(ContinueStatement node) {
				result = "<continue label=" + node.getLabel() + ">";
				return false;
			}
			
			@Override
			public boolean visit(LabeledStatement node) {
				result = "<label name=" + node.getLabel() + ">";
				return false;
			}
			
			
			
//			 *    {@link AssertStatement},
//			 *    {@link ConstructorInvocation},
//			 *    {@link EnhancedForStatement}
//			 *    {@link LabeledStatement},
//			 *    {@link SuperConstructorInvocation},
//			 *    {@link SwitchCase},
//			 *    {@link SwitchStatement},
//			 *    {@link SynchronizedStatement},
//			 *    {@link ThrowStatement},
//			 *    {@link TryStatement},
//			 *    {@link TypeDeclarationStatement},
			
		}
		
		StatementVisitor v = new StatementVisitor();
		stmt.accept(v);
		
		
		return v.result;
		
	}

}
