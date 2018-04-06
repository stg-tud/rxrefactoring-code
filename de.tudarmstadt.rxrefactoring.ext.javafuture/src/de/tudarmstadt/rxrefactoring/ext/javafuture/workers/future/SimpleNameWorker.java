package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

/**
 * Renames SimpleNames Changes methods
 * 
 * @author Steve
 *
 */
public class SimpleNameWorker extends AbstractFutureWorker<SimpleName> {

	public SimpleNameWorker() {
		super("SimpleName");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<SimpleName>> getNodesMap() {
		return collector.getSimpleNamesMap("future");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, SimpleName simpleName) {
		unit.replace(simpleName, unit.getAST().newSimpleName(simpleName.getIdentifier() + "Flowable"));
	}
}
