package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
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
			
			private String getId(ASTNode node) {
				return "[" + node.getStartPosition() + "]";
			}
			
			@Override
			public boolean visit(IfStatement node) {
				result = getId(node) + "<if " + Objects.toString(node.getExpression()) + ">";
				return false;
			}
			
			@Override
			public boolean visit(WhileStatement node) {
				result = getId(node) + "<while " + Objects.toString(node.getExpression()) + ">";
				return false;
			}
			
			@Override
			public boolean visit(DoStatement node) {
				result = getId(node) + "<dowhile " + Objects.toString(node.getExpression()) + ">";
				return false;
			}
			
			@Override
			public boolean visit(ExpressionStatement node) {
				result = getId(node) + Objects.toString(node.getExpression());
				return false;
			}
			
			@Override
			public boolean visit(ReturnStatement node) {
				result = getId(node) + (node.getExpression() == null ? "<return>" : "<return " + Objects.toString(node.getExpression()) + ">");
				return false;
			}
			
			@Override
			public boolean visit(VariableDeclarationStatement node) {
				String s = Objects.toString(node);
				result = getId(node) + s.substring(0, s.length() - 2); //Remove ; and newline from end of string
				return false;
			}
			
			@Override
			public boolean visit(EmptyStatement node) {
				result = getId(node) + "<nop>";
				return false;
			}
			
			@Override
			public boolean visit(ForStatement node) {
				result = getId(node) + "<for " + Objects.toString(node.getExpression()) + " with " + node.initializers() + " do " + node.updaters() + ">";
				return false;
			}
			
			@Override
			public boolean visit(AssertStatement node) {
				result = getId(node) + "<assert " + node.getExpression() + ">"; 
				return false;
			}
			
			@Override
			public boolean visit(Block node) {
				result = getId(node) + "<block>";
				return false;
			}
			
			@Override
			public boolean visit(BreakStatement node) {
				result = getId(node) +  (node.getLabel() == null ? "<break>" : "<break " + node.getLabel() + ">");
				return false;
			}
			
			@Override
			public boolean visit(ContinueStatement node) {
				result = getId(node) + (node.getLabel() == null ? "<continue>" : "<continue " + node.getLabel() + ">");
				return false;
			}
			
			@Override
			public boolean visit(LabeledStatement node) {
				result = getId(node) + "<label " + node.getLabel() + ">";
				return false;
			}
			
			public boolean visit(SwitchStatement node) {
				result = getId(node) + "<switch " + node.getExpression() + ">";
				return false;
			}
			
			public boolean visit(SwitchCase node) {
				result = getId(node) + (node.getExpression() == null ? "<default>" : "<case " + node.getExpression() + ">");
				return false;
			}
			
			public boolean visit(ConstructorInvocation node) {
				String s = Objects.toString(node);
				result = getId(node) + "<" + s.substring(0, s.length() - 2) + ">"; //Remove ; and newline from end of string
				return false;
			}
			
			public boolean visit(SuperConstructorInvocation node) {
				String s = Objects.toString(node);
				result = getId(node) + "<" + s.substring(0, s.length() - 2) + ">"; //Remove ; and newline from end of string
				return false;
			}
			
			
//			 *    {@link EnhancedForStatement}
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
