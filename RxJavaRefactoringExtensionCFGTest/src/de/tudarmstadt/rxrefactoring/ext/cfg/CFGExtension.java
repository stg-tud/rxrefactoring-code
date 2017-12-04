package de.tudarmstadt.rxrefactoring.ext.cfg;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.ext.cfg.workers.CFGCollector;

/**
 * Future extension
 */
public class CFGExtension implements IRefactorExtension {
	
	@Override
	public @NonNull String getDescription() {
		return "Generates the control flow graphs for each method.";
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		workerTree.addWorker(new CFGCollector());	
	}


	@Override
	public @NonNull String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.cfg";
	}

	@Override
	public @NonNull String getName() {
		return "Create CFG";
	}
}
