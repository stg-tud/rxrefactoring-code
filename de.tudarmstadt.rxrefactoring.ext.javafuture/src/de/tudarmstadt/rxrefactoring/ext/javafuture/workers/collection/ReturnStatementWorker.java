package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class ReturnStatementWorker extends AbstractFutureWorker<ReturnStatement> {

	public ReturnStatementWorker() {
		super("ReturnStatement");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<ReturnStatement>> getNodesMap() {
		return collector.getReturnStatementsMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, ReturnStatement returnStatement) {
		IMethodBinding methodBinding = collector.getParentMethod(unit, returnStatement).resolveBinding();

		// We have to return Futures if the method overrides a method outside the
		// project.
		if (!collector.containsMethodDeclaration("collection", ASTUtils.getSuperMethod(methodBinding).orElse(null))
				&& ASTUtils.overridesSuperMethod(methodBinding)) {

			// Kinda Hackish
			// We convert it back to a list of futures

			Expression expression = returnStatement.getExpression();

			if (expression instanceof SimpleName) {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "toFutures",
						(SimpleName) expression, "Observables");
			} else {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "toFutures", expression);
			}
		}
	}
}
