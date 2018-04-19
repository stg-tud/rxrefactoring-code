package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.MethodInvocationVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class VariableDeclStatementWorker extends AbstractFutureWorker<VariableDeclarationStatement> {

	public VariableDeclStatementWorker() {
		super("VariableDeclarationStatement");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<VariableDeclarationStatement>> getNodesMap() {
		return collector.getVarDeclMap("future");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement) {
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDeclStatement.fragments().get(0);

		// Replace type Future with Observable
		replaceType(unit, fragment, varDeclStatement.getType());

		// Replace method invocation
		replaceMethodInvocation(unit, fragment);
	}

	/**
	 * Replaces a Future<> x with an Observable<> xObservable
	 * 
	 * @param unit
	 * @param fragment
	 * @param type
	 */
	private void replaceType(IRewriteCompilationUnit unit, VariableDeclarationFragment fragment, Type type) {

		if (type instanceof ParameterizedType) {
			type = ((ParameterizedType) type).getType();
		}

		JavaFutureASTUtils.replaceType(unit, type, "Flowable");
	}

	/**
	 * Replaces x = someMethod with x = Observable.from(someMethod) But only if we
	 * didn't refactor the method ourselves before.
	 * 
	 * @param unit
	 * @param fragment
	 */
	private void replaceMethodInvocation(IRewriteCompilationUnit unit, VariableDeclarationFragment fragment) {
		// Replace the method invocation only if we didn't refactor the method yet.
		Expression initializer = fragment.getInitializer();

		if (initializer == null)
			return;

		// look for a methodinvocation here
		MethodInvocationVisitor visitor = new MethodInvocationVisitor(collector, "future");

		initializer.accept(visitor);

		if (visitor.isExternalMethod().orElse(false)) {
			// move the initializer expression inside an "Observable.from(initializer)"

			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Flowable", "fromFuture", initializer);
			summary.addCorrect("futureCreation");
		}
	}
}
