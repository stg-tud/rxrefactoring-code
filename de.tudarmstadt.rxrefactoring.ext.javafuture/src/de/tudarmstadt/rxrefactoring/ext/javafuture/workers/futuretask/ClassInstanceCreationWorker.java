package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

public class ClassInstanceCreationWorker extends AbstractFutureTaskWorker<ClassInstanceCreation> {
	public ClassInstanceCreationWorker() {
		super("SimpleName");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, ClassInstanceCreation> getNodesMap() {
		return collector.getClassInstanceMap("futuretask");
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, ClassInstanceCreation classInstanceCreation) {
		/*
		 * unit.replaceType( classInstanceCreation.getType(), "Observable");
		 * 
		 * unit.addImport("rx.Observable");
		 */
	}
}
