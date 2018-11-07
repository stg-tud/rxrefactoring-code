package de.tudarmstadt.rxrefactoring.ext.future;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.ext.future.workers.FutureSubmitCollector;
import de.tudarmstadt.rxrefactoring.ext.future.workers.FutureSubmitTransformer;

/**
 * Future extension
 */
public class FutureRefactoringExtension implements IRefactorExtension {
		

	@Override
	public @NonNull String getDescription() {
		return "Refactor Future and FutureTask to Observables...";
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		IWorkerRef<Void, Multimap<IRewriteCompilationUnit, MethodInvocation>> node = workerTree.addWorker(new FutureSubmitCollector());
		workerTree.addWorker(node, new FutureSubmitTransformer());
	}

	@Override
	public @NonNull String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.future";
	}


	@Override
	public @NonNull String getName() {
		return "scala.concurrent.Future to rx.Observable";
	}
}
