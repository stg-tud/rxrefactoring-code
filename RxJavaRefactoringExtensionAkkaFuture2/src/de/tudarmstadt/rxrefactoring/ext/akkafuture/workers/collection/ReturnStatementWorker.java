package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractFutureWorker;


public class ReturnStatementWorker extends AbstractFutureWorker<ReturnStatement> {

	public ReturnStatementWorker() {
		super("ReturnStatement");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<ReturnStatement>> getNodesMap() {
		return collector.getReturnStatementsMap("collection");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, ReturnStatement returnStatement) {
		IMethodBinding methodBinding = collector.getParentMethod(unit, returnStatement).resolveBinding();
		
		// We have to return Futures if the method overrides a method outside the project.
		if(!collector.containsMethodDeclaration("collection", ASTUtils.getSuperMethod(methodBinding).orElse(null))
				&& ASTUtils.overridesSuperMethod(methodBinding)) {
			
			
			// Kinda Hackish
			// We convert it back to a list of futures
			
			Expression expression = returnStatement.getExpression();
			
			if (expression instanceof SimpleName) {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "toFutures",  (SimpleName)expression, "Observables");
			} else {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "toFutures", expression);
			}
		}
	}
}
