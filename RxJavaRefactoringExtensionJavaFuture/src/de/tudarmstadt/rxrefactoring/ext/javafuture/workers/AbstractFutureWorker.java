package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;

public abstract class AbstractFutureWorker<NodeType extends ASTNode> extends AbstractGeneralWorker<NodeType> {

	public AbstractFutureWorker(String nodeName) {
		super(nodeName);
	}

	protected void addObservableImport(RewriteCompilationUnit unit) {
		unit.addImport("rx.Observable");
	}
	
	protected void addFutureObservableImport(RewriteCompilationUnit unit) {
		unit.addImport("rx.extensions.FutureObservable");
	}
}
