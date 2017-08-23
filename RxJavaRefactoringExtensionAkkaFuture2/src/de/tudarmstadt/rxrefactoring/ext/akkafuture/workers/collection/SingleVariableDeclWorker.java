package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractFutureWorker;

/**
 * Refactores method arguments.
 *
 */
public class SingleVariableDeclWorker extends AbstractFutureWorker<SingleVariableDeclaration>
{
	public SingleVariableDeclWorker() {
		super("SingleVariableDeclaration");
	}
	
	@Override
	protected Map<RewriteCompilationUnit, List<SingleVariableDeclaration>> getNodesMap() {
		return collector.getSingleVarDeclMap("collection");
	}
	
	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, SingleVariableDeclaration singleVarDecl) {
		

	}
}
