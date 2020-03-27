package de.tudarmstadt.rxrefactoring.ext.swingworker;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.TypeDeclarationWorker;


public class SwingWorkerOnlyTypeDeclExtension extends SwingWorkerExtension {
	
	@Override
	public @NonNull String getName() {
		return "SwingWorker to Observable only Type Declarations";
	}

	
	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		setupFreemaker();
		IWorkerRef<Void, RxCollector> collector = workerTree.addWorker(new RxCollector());

		workerTree.addWorker(collector, new TypeDeclarationWorker());

	}

}
