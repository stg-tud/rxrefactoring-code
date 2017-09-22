package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class MethodInvocationWorker extends AbstractFutureWorker<MethodInvocation> {

	public MethodInvocationWorker() {
		super("MethodInvocation");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<MethodInvocation>> getNodesMap() {
		return collector.getMethodInvocationsMap("collection");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().getIdentifier();

		// list.add(future) -> listObservables.add(Observable.from(future))
		if(methodName.equals("add")) {
			Expression expression = (Expression)methodInvocation.arguments().get(0);

			if(collector.isPure(unit, methodInvocation)) {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Observable", "from", expression);
			} else {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "create", expression);
			}
			summary.addCorrect("futureCreation");
		}
	}
}
