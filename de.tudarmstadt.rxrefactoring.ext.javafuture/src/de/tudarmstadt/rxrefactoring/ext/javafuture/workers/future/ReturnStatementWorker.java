package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.MethodInvocationVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class ReturnStatementWorker extends AbstractFutureWorker<ReturnStatement> {
	public ReturnStatementWorker() {
		super("ReturnStatement");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<ReturnStatement>> getNodesMap() {
		return collector.getReturnStatementsMap("future");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, ReturnStatement returnStatement) {
		Expression expression = returnStatement.getExpression();
		refactorExpression(unit, expression);
	}

	/**
	 * Replaces return someMethod with return Observable.from(someMethod) But only if we
	 * didn't refactor the method ourselves before.
	 * 
	 * @param unit
	 * @param expression
	 */
	private void refactorExpression(IRewriteCompilationUnit unit, Expression expression) {

		// look for a methodinvocation here
		MethodInvocationVisitor visitor = new MethodInvocationVisitor(collector, "future");

		expression.accept(visitor);

		if (visitor.shouldRefactor().orElse(false)) {
			// move the initializer expression inside an "Observable.from(expression)"

			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Observable", "fromFuture", expression);
			summary.addCorrect("futureCreation");
		}
	}

}
