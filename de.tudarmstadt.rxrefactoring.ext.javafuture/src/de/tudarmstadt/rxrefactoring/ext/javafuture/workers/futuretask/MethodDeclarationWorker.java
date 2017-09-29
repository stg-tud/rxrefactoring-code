package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

public class MethodDeclarationWorker extends AbstractFutureTaskWorker<MethodDeclaration> {

	public MethodDeclarationWorker() {
		super("MethodDeclaration");
	}
	
	@Override
	protected Map<IRewriteCompilationUnit, List<MethodDeclaration>> getNodesMap() {
		return collector.getMethodDeclarationsMap("futuretask");
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		replaceReturnType(unit, methodDeclaration);
		
		replaceReturnStatements(unit, methodDeclaration);
	}

	private void replaceReturnType(IRewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		Type returnType =  methodDeclaration.getReturnType2();				
		if(returnType instanceof ParameterizedType) {
			Type type = ((ParameterizedType)returnType).getType();
			
			// Don't refactor return values for now
			//unit.replaceType(type, "SimpleFutureTaskObservable");
		}
	}
	
	private void replaceReturnStatements(IRewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		
		// Replace return statements
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(ReturnStatement returnStatement) {

				Expression returnExpression = returnStatement.getExpression();

				if(returnExpression instanceof MethodInvocation
						|| returnExpression instanceof NullLiteral) {
					
					JavaFutureASTUtils.moveInsideMethodInvocation(unit, "SimpleFutureTaskObservable", "create", returnExpression);
					summary.addCorrect("futureTaskCreation");
				}

				return true;
			}
			
			@Override
			public boolean visit(LambdaExpression lambdaExpression) {
				return false;
			}
		};
		
		methodDeclaration.accept(visitor);
	}
}
