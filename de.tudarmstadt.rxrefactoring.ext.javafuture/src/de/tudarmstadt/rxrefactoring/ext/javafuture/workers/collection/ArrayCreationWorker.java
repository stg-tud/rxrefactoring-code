package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ArrayCreation;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

public class ArrayCreationWorker extends AbstractFutureWorker<ArrayCreation> {

	public ArrayCreationWorker() {
		super("ArrayCreation");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<ArrayCreation>> getNodesMap() {
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
