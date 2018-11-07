package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;

public abstract class AbstractFutureWorker<NodeType extends ASTNode> extends AbstractGeneralWorker<NodeType> {

	public AbstractFutureWorker(String nodeName) {
		super(nodeName);
	}

	protected void addObservableImport(IRewriteCompilationUnit unit) {
		unit.addImport("io.reactivex.Observable");
	}

	protected void addFutureObservableImport(IRewriteCompilationUnit unit) {
		//unit.addImport("rx.extensions.FutureObservable");
	}
}
