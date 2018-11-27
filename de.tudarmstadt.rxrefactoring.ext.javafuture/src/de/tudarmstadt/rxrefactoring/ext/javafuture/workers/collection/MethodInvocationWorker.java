package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.MethodInvocationVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class MethodInvocationWorker extends AbstractFutureWorker<MethodInvocation> {

	public MethodInvocationWorker() {
		super("MethodInvocation");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<MethodInvocation>> getNodesMap() {
		return collector.getMethodInvocationsMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, MethodInvocation methodInvocation) {
		
		// e.g. list.add(future) -> listObservables.add(Observable.from(future))
		Expression expression = (Expression) methodInvocation.arguments().get(0);
		
		// MethodDeclarations in future, not collector, group
		MethodInvocationVisitor visitor = new MethodInvocationVisitor(collector, "future");
		
		expression.accept(visitor);
		
		if (visitor.isExternalMethod()) {

			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Observable", "fromFuture", expression);
			summary.addCorrect("futureCreation");
		}
	}
	
}
