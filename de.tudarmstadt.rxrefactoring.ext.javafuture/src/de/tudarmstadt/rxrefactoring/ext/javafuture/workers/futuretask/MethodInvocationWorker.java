package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

public class MethodInvocationWorker extends AbstractFutureTaskWorker<MethodInvocation> {

	public MethodInvocationWorker() {
		super("MethodInvocation");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<MethodInvocation>> getNodesMap() {
		return collector.getMethodInvocationsMap("futuretask");
	}

	/**
	 * Replaces a future.get with an observable.toBlocking().single()
	 * 
	 * @param unit
	 * @param methodInvocation
	 */
	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().getIdentifier();

		/*
		 * switch (methodName) { case "get":
		 * 
		 * Expression expression = methodInvocation.getExpression(); String newName =
		 * "";
		 * 
		 * if (expression instanceof SimpleName) { SimpleName simpleName =
		 * (SimpleName)expression; newName = simpleName.getIdentifier() + "Observable";
		 * } else if (expression instanceof ArrayAccess) { ArrayAccess arrayAccess =
		 * (ArrayAccess)expression;
		 * 
		 * SimpleName simpleName = (SimpleName)arrayAccess.getArray(); newName =
		 * simpleName.getIdentifier() + "Observables"; }
		 * 
		 * if(!newName.isEmpty()) unit.replaceMethodInvocation(newName, "toBlocking",
		 * "single", methodInvocation);
		 * 
		 * break;
		 * 
		 * default: System.err.println("Method " + methodName + " not supported!");
		 * break; }
		 */
	}
}
