package de.tudarmstadt.rxrefactoring.ext.akkafuture;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.AwaitWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.FutureCreationWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.ListTypeWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.MethodUsageWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.UnrefactorableReferencesWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.VariableTypeToObservableWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.VariableTypeToSubjectWorker;

/**
 * Future extension
 */
public class AkkaFutureRefactoringExtension implements IRefactorExtension {
	
	

	public AkkaFutureRefactoringExtension() {
		
	}
	

	@Override
	public @NonNull String getDescription() {
		return "Refactor Future and FutureTask to Observables...";
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		IWorkerRef<Void, AkkaFutureCollector> futureCollectorRef = workerTree.addWorker(new AkkaFutureCollector());		
			
		//Future workers
		//workerTree.addWorker(futureCollectorRef, new VariableFragmentWorker());
		
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
		return "de.tudarmstadt.rxrefactoring.ext.akkafuture";
	}


	@Override
	public @NonNull String getName() {
		return "scala.concurrent.Future to rx.Observable";
	}
}
