package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.futurewrapper;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractFutureWorker;


public class MethodDeclarationWorker extends AbstractFutureWorker<MethodDeclaration> {

	public MethodDeclarationWorker() {
		super("MethodDeclaration");
	}
	
	@Override
	protected Map<RewriteCompilationUnit, List<MethodDeclaration>> getNodesMap() {
		return collector.getMethodDeclarationsMap("future");
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		replaceReturnType(unit, methodDeclaration);
		
		replaceReturnStatements(unit, methodDeclaration);
	}

	private void replaceReturnType(RewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		Type returnType =  methodDeclaration.getReturnType2();
		
		// Check for super methods
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		
		// We don't refactor the return value if the method overrides a method outside the project.
		if(!collector.containsMethodDeclaration("future", ASTUtils.getSuperMethod(methodBinding).orElse(null))
				&& ASTUtils.overridesSuperMethod(methodBinding)) {
			return;
		}
		
		if(returnType instanceof ParameterizedType) {
			Type type = ((ParameterizedType)returnType).getType();
			
			JavaFutureASTUtils.replaceType(unit, type, "FutureObservable");
		}
	}
	
	private void replaceReturnStatements(RewriteCompilationUnit unit, MethodDeclaration methodDeclaration) {
		
		// Replace return statements
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(ReturnStatement returnStatement) {

				Expression returnExpression = returnStatement.getExpression();

				if(returnExpression instanceof MethodInvocation
						|| returnExpression instanceof NullLiteral) {
					
					JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "create", returnExpression);
					summary.addCorrect("futureCreation");
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
