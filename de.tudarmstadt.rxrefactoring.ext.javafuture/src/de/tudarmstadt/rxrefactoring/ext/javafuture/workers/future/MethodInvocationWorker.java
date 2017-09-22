package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class MethodInvocationWorker extends AbstractFutureWorker<MethodInvocation> {

	public MethodInvocationWorker() {
		super("MethodInvocation");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<MethodInvocation>> getNodesMap() {
		return collector.getMethodInvocationsMap("future");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		
		super.endRefactorNode(unit);
	}

	/**
	 * Replaces a future.get with an observable.toBlocking().single()
	 * @param unit
	 * @param methodInvocation
	 */
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().getIdentifier();

		switch (methodName) {
		case "get":
			
			Expression expression = methodInvocation.getExpression();
			String newName = "";
			
			if (expression instanceof SimpleName) {
				SimpleName simpleName = (SimpleName)expression;
				newName = simpleName.getIdentifier() + "Observable";
			} else if (expression instanceof ArrayAccess) {
				ArrayAccess arrayAccess = (ArrayAccess)expression;
				
				SimpleName simpleName = (SimpleName)arrayAccess.getArray();
				newName = simpleName.getIdentifier() + "Observables";
			}
			
			if(!newName.isEmpty())
				JavaFutureASTUtils.replaceMethodInvocation(unit, newName, "toBlocking", "single", methodInvocation);
			
			break;

		default:
			System.err.println("Method " + methodName + " not supported!");
			break;
		}
	}
}
