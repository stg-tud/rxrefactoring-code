package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

/**
 * Renames SimpleNames Changes methods
 * 
 * @author Steve
 *
 */
public class SimpleNameWorker extends AbstractFutureTaskWorker<SimpleName> {

	public SimpleNameWorker() {
		super("SimpleName");
	}

	@Override
	protected Map<IRewriteCompilationUnit, List<SimpleName>> getNodesMap() {
		return collector.getSimpleNamesMap("futuretask");
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, SimpleName simpleName) {
		JavaFutureASTUtils.appendSimpleName(unit, simpleName, "Observable");
	}
}
