package de.tudarmstadt.rxrefactoring.ext.akkafuture;

import java.util.EnumSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree.WorkerNode;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future.AwaitWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future.FutureCreationWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future.ListTypeWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future.MethodUsageWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future.UnrefactorableReferencesWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future.VariableTypeToObservableWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future.VariableTypeToSubjectWorker;

/**
 * Future extension
 */
public class AkkaFutureRefactoring implements RefactorExtension {
	
	private EnumSet<RefactoringOptions> options;

	public AkkaFutureRefactoring() {
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
			
			
			
			
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.futurewrapper.VariableDeclStatementWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.futurewrapper.AssignmentWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.futurewrapper.MethodInvocationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.futurewrapper.MethodDeclarationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.futurewrapper.SingleVariableDeclWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.futurewrapper.FieldDeclarationWorker());
//			
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.SimpleNameWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.VariableDeclStatementWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.MethodInvocationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.MethodDeclarationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.FieldDeclarationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.ClassInstanceCreationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.ArrayCreationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.AssignmentWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.collection.ReturnStatementWorker());
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
