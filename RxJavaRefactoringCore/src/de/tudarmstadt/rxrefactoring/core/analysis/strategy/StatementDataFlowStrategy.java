package de.tudarmstadt.rxrefactoring.core.analysis.strategy;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import de.tudarmstadt.rxrefactoring.core.utils.Box;

public interface StatementDataFlowStrategy<Result> extends DataFlowStrategy<Result> {

	@Override
	default public Result transform(Statement statement, Result input) {
		
		Box<Result> result = new Box<Result>();
		
		new ASTVisitor() {
			@Override
			public boolean visit(AssertStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(Block node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(BreakStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(ConstructorInvocation node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(ContinueStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(DoStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(EmptyStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(EnhancedForStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(ExpressionStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(ForStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(IfStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(LabeledStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(ReturnStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(SuperConstructorInvocation node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(SwitchCase node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(SwitchStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(SynchronizedStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			
			@Override
			public boolean visit(ThrowStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(TryStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(TypeDeclarationStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(VariableDeclarationStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}
			
			@Override
			public boolean visit(WhileStatement node) {
				result.set(transformStatement(node, input));
				return false;
			}			
		}; 
		
		return result.get();
	}
	
	Result transformStatement(AssertStatement statement, Result input);
	
	Result transformStatement(Block statement, Result input);
	
	Result transformStatement(BreakStatement statement, Result input);
	
	Result transformStatement(ConstructorInvocation statement, Result input);
	
	Result transformStatement(ContinueStatement statement, Result input);
	
	Result transformStatement(DoStatement statement, Result input);
	
	Result transformStatement(EmptyStatement statement, Result input);
	
	Result transformStatement(EnhancedForStatement statement, Result input);
	
	Result transformStatement(ExpressionStatement statement, Result input);
	
	Result transformStatement(ForStatement statement, Result input);
	
	Result transformStatement(IfStatement statement, Result input);
	
	Result transformStatement(LabeledStatement statement, Result input);
	
	Result transformStatement(ReturnStatement statement, Result input);
	
	Result transformStatement(SuperConstructorInvocation statement, Result input);
	
	Result transformStatement(SwitchCase statement, Result input);
	
	Result transformStatement(SwitchStatement statement, Result input);
	
	Result transformStatement(SynchronizedStatement statement, Result input);
	
	Result transformStatement(ThrowStatement statement, Result input);

	Result transformStatement(TryStatement statement, Result input);

	Result transformStatement(TypeDeclarationStatement statement, Result input);

	Result transformStatement(VariableDeclarationStatement statement, Result input);

	Result transformStatement(WhileStatement statement, Result input);

}
