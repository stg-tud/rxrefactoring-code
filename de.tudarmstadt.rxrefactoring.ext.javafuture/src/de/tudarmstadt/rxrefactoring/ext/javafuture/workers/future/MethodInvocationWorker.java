package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class MethodInvocationWorker extends AbstractFutureWorker<MethodInvocation> {

	public MethodInvocationWorker() {
		super("MethodInvocation");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<MethodInvocation>> getNodesMap() {
		return collector.getMethodInvocationsMap("future");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);

		super.endRefactorNode(unit);
	}

	/**
	 * Replaces a future.get with an observable.toBlocking().single()
	 * 
	 * @param unit
	 * @param methodInvocation
	 */
	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, MethodInvocation methodInvocation) {
		if (Methods.hasSignature(methodInvocation.resolveMethodBinding(), "java.util.concurrent.Future", "get") || 
				Methods.hasSignature(methodInvocation.resolveMethodBinding(), "java.util.concurrent.Future", "get", "long", "java.util.concurrent.TimeUnit")) {	
//			Expression expression = methodInvocation.getExpression();
//			String newName = "";
			
//			if (expression instanceof SimpleName) {
//				SimpleName simpleName = (SimpleName) expression;
//				newName = simpleName.getIdentifier() + "Observable";
//			} else if (expression instanceof ArrayAccess) {
//				ArrayAccess arrayAccess = (ArrayAccess) expression;
//
//				SimpleName simpleName = (SimpleName) arrayAccess.getArray();
//				newName = simpleName.getIdentifier() + "Observables";
//			}
	
			JavaFutureASTUtils.replaceWithBlockingGet(unit, methodInvocation);
			
//			if (!newName.isEmpty())
//				JavaFutureASTUtils.replaceWithBlockingGet(unit, methodInvocation, newName);
//			else if (expression instanceof MethodInvocation) {
//				
//			}
		} else {
			Log.error(getClass(), "Method " + methodInvocation.getName().getIdentifier() + " not supported!");
		}
	}
}
