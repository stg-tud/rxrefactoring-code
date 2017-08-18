package de.tudarmstadt.rxrefactoring.ext.javafuture;

import java.util.EnumSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.tudarmstadt.rxrefactoring.core.Refactoring;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree.WorkerNode;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

/**
 * Future extension
 */
public class JavaFutureRefactoring implements Refactoring {
	
	private EnumSet<RefactoringOptions> options;

	public JavaFutureRefactoring() {
		options = EnumSet.of(RefactoringOptions.FUTURE_WRAPPER);
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
		WorkerNode<Void, FutureCollector> collector = workerTree.addWorker(new FutureCollector(options));
		
		if(options.contains(RefactoringOptions.FUTURE)) {
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.SimpleNameWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.VariableDeclStatementWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.AssignmentWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.MethodInvocationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.MethodDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.SingleVariableDeclWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.FieldDeclarationWorker());

			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.SimpleNameWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.VariableDeclStatementWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodInvocationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.FieldDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ClassInstanceCreationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ArrayCreationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.AssignmentWorker());
		} else if(options.contains(RefactoringOptions.FUTURE_WRAPPER)) {
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper.SimpleNameWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper.VariableDeclStatementWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper.AssignmentWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper.MethodInvocationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper.MethodDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper.SingleVariableDeclWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futurewrapper.FieldDeclarationWorker());
			
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.SimpleNameWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.VariableDeclStatementWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodInvocationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.FieldDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ClassInstanceCreationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ArrayCreationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.AssignmentWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ReturnStatementWorker());
		}
		
		if(options.contains(RefactoringOptions.FUTURETASK)) {
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.SimpleNameWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.VariableDeclStatementWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.AssignmentWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.MethodInvocationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.MethodDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.SingleVariableDeclWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.FieldDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.ClassInstanceCreationWorker());
		}
		
	}

	@Override
	public String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.javafuture";
	}
}
