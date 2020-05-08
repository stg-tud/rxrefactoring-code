package de.tudarmstadt.rxrefactoring.ext.javafuture;

import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.PreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.UseDefWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies.CursorRefactorOccurenceSearcher;
import de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies.DependencyCheckerJavaFuture;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.InstantiationCollector;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.WorkerUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

/**
 * Future extension
 */
public class JavaFutureRefactoring implements IRefactorExtension {

	private EnumSet<RefactoringOptions> options;
	private FutureCollector futureCollector;

	public JavaFutureRefactoring() {
		options = EnumSet.of(RefactoringOptions.FUTURE);// , RefactoringOptions.FUTURETASK);
		futureCollector = new FutureCollector(options);
	}

	@Override
	public void setRefactorScope(RefactorScope scope) {
		if(scope.equals(RefactorScope.SEPARATE_OCCURENCES))
				options.add(RefactoringOptions.SEPARATE_OCCURENCIES);
		
	}
	
	@Override
	public RefactorScope getRefactorScope() {
		if(options.contains(RefactoringOptions.SEPARATE_OCCURENCIES))
			return RefactorScope.SEPARATE_OCCURENCES;
		
		return RefactorScope.NO_SCOPE;
	}
	
	@Override
	public ProjectUnits runDependencyBetweenWorkerCheck(ProjectUnits units, MethodScanner scanner, int startLine) throws JavaModelException{
		DependencyCheckerJavaFuture dependencyCheck = new DependencyCheckerJavaFuture(units, scanner, futureCollector, startLine);
		return dependencyCheck.runDependendencyCheck(false);
	}


	@Override
	public void clearAllMaps() {
		for (Entry<String, CollectorGroup> groupEntry : futureCollector.groups.entrySet()) {
		groupEntry.getValue().clearAllMaps();
		}
	};
	
	@Override
	public @NonNull String getName() {
		return "Future and FutureTask to Observable";
	}

	@Override
	public @NonNull String getDescription() {
		return "Refactor Future and FutureTask to Observables...";
	}

	@Override
	public IPath getResourceDir() {
		return new Path("resources/");
	}

	@Override
	public IPath getDestinationDir() {
		return new Path("./libs/");
	}
	
	@Override
	public boolean hasInteractiveRefactorScope() {
		return true; 
	}
	

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		IWorkerRef<Void, Map<ASTNode, UseDef>> analysisRef = workerTree.addWorker(new UseDefWorker());

		IWorkerRef<Map<ASTNode, UseDef>, InstantiationCollector> instRef = workerTree.addWorker(analysisRef,
				new InstantiationCollector(ClassInfos.Future));
		IWorkerRef<InstantiationCollector, SubclassInstantiationCollector> subclassInstRef = workerTree
				.addWorker(instRef, new SubclassInstantiationCollector());
		IWorkerRef<SubclassInstantiationCollector, PreconditionWorker> instUseRef = workerTree
				.addWorker(subclassInstRef, new PreconditionWorker());

		IWorkerRef<PreconditionWorker, FutureCollector> collector = workerTree.addWorker(instUseRef,
				futureCollector);

		if (options.contains(RefactoringOptions.FUTURE)) {
//			workerTree.addWorker(collector,
//					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.SimpleNameWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.VariableDeclStatementWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.AssignmentWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.MethodInvocationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.MethodDeclarationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.SingleVariableDeclWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.FieldDeclarationWorker());

//			workerTree.addWorker(collector, new
//					de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.SimpleNameWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.VariableDeclStatementWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodInvocationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodDeclarationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.FieldDeclarationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ClassInstanceCreationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ArrayCreationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.AssignmentWorker());
			
			WorkerUtils.fillAllWorkerIdentifierForFuture();
		}

		if (options.contains(RefactoringOptions.FUTURETASK)) {
			//workerTree.addWorker(collector,
			//		new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.SimpleNameWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.VariableDeclStatementWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.AssignmentWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.MethodInvocationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.MethodDeclarationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.SingleVariableDeclWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.FieldDeclarationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.ClassInstanceCreationWorker());
			WorkerUtils.fillAllWorkerIdentifierForFuture();
		}

	}

	@Override
	public @NonNull String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.javafuture";
	}

	@Override
	public @NonNull ExecutorService createExecutorService() {
		return Executors.newSingleThreadExecutor();
	}

}
