package de.tudarmstadt.rxrefactoring.ext.swingworker.extensions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies.DependencyCheckerSwingWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.TypeDeclarationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.VariableDeclStatementWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;

public class SwingWorkerOnlyVarDeclExtension extends SwingWorkerExtension{
	
	@Override
	public @NonNull String getName() {
		return "SwingWorker to Observable only Variable Declarations";
	}
	
	@Override
	public boolean hasInteractiveRefactorScope() {
		return false;
	}
	@Override
	public boolean onlyScanOpenFile() {
		return true;
	}
	
	@Override
	public String getDescription() {
		return "Only Variable Declaration";
	}
	
	@Override
	public RefactorScope getRefactorScope() {
		return RefactorScope.ONLY_ONE_OCCURENCE;
	}
	
	@Override
	public ProjectUnits runDependencyBetweenWorkerCheck(ProjectUnits units, MethodScanner scanner) throws JavaModelException{
		DependencyCheckerSwingWorker dependencyCheck = new DependencyCheckerSwingWorker(units, scanner);
		return dependencyCheck.runDependendencyCheck(true);
	}
	
	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		setupFreemaker();
		IWorkerRef<Void, RxCollector> collector = workerTree.addWorker(new RxCollector(getRefactorScope()));
		IWorkerRef<RxCollector, TypeOutput> typeWorker = workerTree.addWorker(collector, new TypeDeclarationWorker());

		workerTree.addWorker(typeWorker, new VariableDeclStatementWorker());

	}

}
