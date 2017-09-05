package de.tudarmstadt.rxrefactoring.ext.akkafuture;

import java.util.EnumSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree.WorkerNode;
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
public class AkkaFutureRefactoringExtension implements RefactorExtension {
	
	private EnumSet<RefactoringOptions> options;

	public AkkaFutureRefactoringExtension() {
		options = EnumSet.of(RefactoringOptions.AKKA_FUTURE);
	}
	

	@Override
	public IPath getResourceDir() {
		return new Path("./resources/");
	}

	@Override
	public IPath getDestinationDir() {
		return new Path("./libs/");
	}
	

	@Override
	public String getDescription() {
		return "Refactor Future and FutureTask to Observables...";
	}

	@Override
	public void addWorkersTo(WorkerTree workerTree) {
		WorkerNode<Void, AkkaFutureCollector> futureCollectorRef = workerTree.addWorker(new AkkaFutureCollector());

		
		if(options.contains(RefactoringOptions.AKKA_FUTURE)) {
			
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
			
		} else {
			throw new IllegalStateException("No valid options: " + options);
		}		
	}

	@Override
	public String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.akkafuture";
	}


	@Override
	public String getName() {
		return "scala.concurrent.Future to rx.Observable";
	}
}
