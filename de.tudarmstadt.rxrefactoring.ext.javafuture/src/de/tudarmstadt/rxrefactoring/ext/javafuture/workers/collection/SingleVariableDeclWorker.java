package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

/**
 * Refactores method arguments.
 *
 */
public class SingleVariableDeclWorker extends AbstractFutureWorker<SingleVariableDeclaration> {
	public SingleVariableDeclWorker() {
		super("SingleVariableDeclaration");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> getNodesMap() {
		return collector.getSingleVarDeclMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, SingleVariableDeclaration singleVarDecl) {

	}
}
