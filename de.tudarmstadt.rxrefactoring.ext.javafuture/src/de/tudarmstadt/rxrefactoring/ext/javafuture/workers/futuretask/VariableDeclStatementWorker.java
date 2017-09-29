package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

public class VariableDeclStatementWorker extends AbstractFutureTaskWorker<VariableDeclarationStatement> {
	
	public VariableDeclStatementWorker() {
		super("VariableDeclarationStatement");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<VariableDeclarationStatement>> getNodesMap() {
		return collector.getVarDeclMap("futuretask");
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement) {
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)varDeclStatement.fragments().get(0);

		// Replace type Future with Observable
		replaceType(unit, fragment, varDeclStatement.getType());

		// Replace method invocation
		replaceMethodInvocation(unit, fragment);
	}

	/**
	 * Replaces a Future<> x with an Observable<> xObservable
	 * @param unit
	 * @param fragment
	 * @param type
	 */
	private void replaceType(IRewriteCompilationUnit unit, VariableDeclarationFragment fragment, Type type) {

		if (type instanceof ParameterizedType) {
			type = ((ParameterizedType) type).getType();
		}
		JavaFutureASTUtils.replaceType(unit, type, "SimpleFutureTaskObservable");
	}

	/**
	 * Replaces x = someMethod with x = SimpleFutureTaskObservable.create(someMethod)
	 * But only if we didn't refactor the method ourselves before.
	 * @param unit
	 * @param fragment
	 */
	private void replaceMethodInvocation(IRewriteCompilationUnit unit, VariableDeclarationFragment fragment) {
		// Replace the method invocation only if we didn't refactor the method yet.
		Expression initializer = fragment.getInitializer();
		
		if(initializer == null)
			return;

		// look for a methodinvocation here
	/*	MethodInvocationVisitor visitor = new MethodInvocationVisitor(collector, "futuretask");

		initializer.accept(visitor);

		if(visitor.isExternalMethod().orElse(false)) { */
			// move the initializer expression inside an "SimpleFutureTaskObservable.create(initializer)"
			
			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "SimpleFutureTaskObservable", "create", initializer);
	//	}
	}
}
