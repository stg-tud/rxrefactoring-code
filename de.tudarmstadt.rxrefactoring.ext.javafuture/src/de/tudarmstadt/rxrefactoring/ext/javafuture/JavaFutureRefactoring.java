package de.tudarmstadt.rxrefactoring.ext.javafuture;

import java.util.EnumSet;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.ReachingDefinition;
import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.ReachingDefinitionsWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.PreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

/**
 * Future extension
 */
public class JavaFutureRefactoring implements IRefactorExtension {
	
	private EnumSet<RefactoringOptions> options;

	public JavaFutureRefactoring() {
		options = EnumSet.of(RefactoringOptions.FUTURE, RefactoringOptions.FUTURETASK);
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
	public @NonNull String getName() {
		return "Future and FutureTask to Observable";
	}

	@Override
	public @NonNull String getDescription() {
		return "Refactor Future and FutureTask to Observables...";
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		IWorkerRef<Void, Map<ASTNode, ReachingDefinition>> analysisRef = workerTree.addWorker(new ReachingDefinitionsWorker());
		IWorkerRef<Map<ASTNode, ReachingDefinition>, Void> precondRef = workerTree.addWorker(analysisRef, new PreconditionWorker());
		IWorkerRef<Void, FutureCollector> collector = workerTree.addWorker(precondRef, new FutureCollector(options));
		
		if(options.contains(RefactoringOptions.FUTURE)) {
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.SimpleNameWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.VariableDeclStatementWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.AssignmentWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.MethodInvocationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.MethodDeclarationWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.SingleVariableDeclWorker());
			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.FieldDeclarationWorker());

//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.SimpleNameWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.VariableDeclStatementWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodInvocationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodDeclarationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.FieldDeclarationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ClassInstanceCreationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ArrayCreationWorker());
//			workerTree.addWorker(collector, new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.AssignmentWorker());
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
	public @NonNull String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.javafuture";
	}


	
}
