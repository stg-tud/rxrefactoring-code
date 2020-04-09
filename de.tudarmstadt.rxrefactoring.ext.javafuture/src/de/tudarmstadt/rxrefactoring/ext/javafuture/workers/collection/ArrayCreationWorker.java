package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import org.eclipse.jdt.core.dom.ArrayCreation;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class ArrayCreationWorker extends AbstractFutureWorker<ArrayCreation> {

	public ArrayCreationWorker() {
		super("ArrayCreation");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, ArrayCreation> getNodesMap() {
		return collector.getArrayCreationsMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, ArrayCreation arrayCreation) {
		if (collector.isPure(unit, arrayCreation)) {
			JavaFutureASTUtils.replaceType(unit, arrayCreation.getType().getElementType(), "Observable");
		} else {
			JavaFutureASTUtils.replaceType(unit, arrayCreation.getType().getElementType(), "FutureObservable");
		}
	}
}
