package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

/**
 * Renames SimpleNames
 * Changes methods
 * @author Steve
 *
 */
public class SimpleNameWorker extends AbstractFutureWorker<SimpleName> {
	
	public SimpleNameWorker() {
		super("SimpleName");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<SimpleName>> getNodesMap() {
		return collector.getSimpleNamesMap("future");
	}

	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, SimpleName simpleName) {
		unit.replace(simpleName, unit.getAST().newSimpleName(simpleName.getIdentifier() + "Observable"));
	}
}
