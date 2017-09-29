package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;


public abstract class AbstractFutureTaskWorker<NodeType extends ASTNode> extends AbstractGeneralWorker<NodeType> {

	public AbstractFutureTaskWorker(String nodeName) {
		super(nodeName);
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		unit.addImport("rx.extensions.SimpleFutureTaskObservable");
		
		super.endRefactorNode(unit);
	}
}
