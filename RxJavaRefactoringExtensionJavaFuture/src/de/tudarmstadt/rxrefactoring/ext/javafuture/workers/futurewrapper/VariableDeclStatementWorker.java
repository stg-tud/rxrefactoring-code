package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.MethodInvocationVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class VariableDeclStatementWorker extends AbstractFutureWorker<VariableDeclarationStatement> {
	
	public VariableDeclStatementWorker() {
		super("VariableDeclarationStatement");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<VariableDeclarationStatement>> getNodesMap() {
		return collector.getVarDeclMap("future");
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement) {
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)varDeclStatement.fragments().get(0);

		// Replace type Future with Observable
		replaceType(unit, varDeclStatement, fragment);

		// Replace method invocation
		replaceMethodInvocation(unit, varDeclStatement, fragment);
	}

	/**
	 * Replaces a Future<> x with an Observable<> xObservable
	 * @param unit
	 * @param fragment
	 * @param type
	 */
	private void replaceType(RewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement, VariableDeclarationFragment fragment) {

		Type type = varDeclStatement.getType();
		
		if (type instanceof ParameterizedType) {
			type = ((ParameterizedType) type).getType();
		}
		
		if(collector.isPure(unit, varDeclStatement)) {
			JavaFutureASTUtils.replaceType(unit, type, "Observable");
			addObservableImport(unit);
		} else {
			JavaFutureASTUtils.replaceType(unit, type, "FutureObservable");
			addFutureObservableImport(unit);
		}
	}

	/**
	 * Replaces x = someMethod with x = Observable.from(someMethod)
	 * But only if we didn't refactor the method ourselves before.
	 * @param unit
	 * @param fragment
	 */
	private void replaceMethodInvocation(RewriteCompilationUnit unit, VariableDeclarationStatement varDeclStatement, VariableDeclarationFragment fragment) {
		// Replace the method invocation only if we didn't refactor the method yet.
		Expression initializer = fragment.getInitializer();
		
		if(initializer == null)
			return;
		
		boolean isPure = collector.isPure(unit, varDeclStatement);
		
		// check whether the value is already what we want
		/*
		IVariableBinding binding = fragment.resolveBinding();
		ITypeBinding type = binding.getType();
		
		
		 // TODO
		  * Doesn't work because
		if((ASTUtil.isClassOf(type, "Future"))
				|| (!isPure && ASTUtil.isClassOf(type, "FutureObservable"))) {
			return;
		} */

		// look for a methodinvocation here
		MethodInvocationVisitor visitor = new MethodInvocationVisitor(collector, "future");

		initializer.accept(visitor);

		if(visitor.isExternalMethod().orElse(false)) {
			// move the initializer expression inside an "Observable.from(initializer)"
			
			if(isPure) {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Observable", "from", initializer);
			} else {
				JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "create", initializer);
			}
			summary.addCorrect("futureCreation");
		}
	}
}
