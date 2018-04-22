package de.tudarmstadt.rxrefactoring.ext.springasync;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.SpringAsyncCollector;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.springasync.AwaitWorker;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.springasync.FutureCreationWorker;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.springasync.ListTypeWorker;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.springasync.MethodUsageWorker;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.springasync.UnrefactorableReferencesWorker;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.springasync.VariableTypeToObservableWorker;
import de.tudarmstadt.rxrefactoring.ext.springasync.workers.springasync.VariableTypeToSubjectWorker;

/**
 * Future extension
 */
public class SpringAsyncRefactoringExtension implements RefactorExtension {
	
	

	public SpringAsyncRefactoringExtension() {
		
	}
	

	@Override
	public @NonNull String getDescription() {
		return "Refactor Future and FutureTask to Observables...";
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		IWorkerRef<Void, SpringAsyncCollector> futureCollectorRef = workerTree.addWorker(new SpringAsyncCollector());		
			
		//Future workers
		workerTree.addWorker(futureCollectorRef, new MethodUsageWorker());
		workerTree.addWorker(futureCollectorRef, new UnrefactorableReferencesWorker());
		workerTree.addWorker(futureCollectorRef, new VariableTypeToObservableWorker());
		workerTree.addWorker(futureCollectorRef, new VariableTypeToSubjectWorker());
		workerTree.addWorker(futureCollectorRef, new AwaitWorker());
					
		//Collection workers
		workerTree.addWorker(futureCollectorRef, new ListTypeWorker());
		workerTree.addWorker(futureCollectorRef, new FutureCreationWorker());
			
	
	}

	@Override
	public @NonNull String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.springAsync";
	}


	@Override
	public @NonNull String getName() {
		return "java.util.concurrent.Future to rx.Observable";
	}
}
