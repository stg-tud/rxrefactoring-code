package de.tudarmstadt.rxrefactoring.ext.swingworker;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.internal.execution.NullWorker;
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
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		setupFreemaker();
		IWorkerRef<Void, RxCollector> collector = workerTree.addWorker(new RxCollector());
		IWorkerRef<RxCollector, TypeOutput> typeWorker = workerTree.addWorker(collector, new TypeDeclarationWorker());

		workerTree.addWorker(typeWorker, new VariableDeclStatementWorker());

	}

}
