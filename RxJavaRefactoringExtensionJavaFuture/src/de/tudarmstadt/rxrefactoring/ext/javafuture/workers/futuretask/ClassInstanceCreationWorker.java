package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

public class ClassInstanceCreationWorker extends AbstractFutureTaskWorker<ClassInstanceCreation> {
	public ClassInstanceCreationWorker() {
		super("SimpleName");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<ClassInstanceCreation>> getNodesMap() {
		return collector.getClassInstanceMap("futuretask");
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, ClassInstanceCreation classInstanceCreation) {
		/* unit.replaceType(
				classInstanceCreation.getType(), 
				"Observable");
		
		unit.addImport("rx.Observable"); */
	}
}
