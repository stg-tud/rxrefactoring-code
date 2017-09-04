package de.tudarmstadt.rxrefactoring.ext.cfg;

import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;
import de.tudarmstadt.rxrefactoring.ext.cfg.workers.CFGCollector;

/**
 * Future extension
 */
public class CFGRefactoring implements RefactorExtension {
	
	@Override
	public String getDescription() {
		return "Generating CFG...";
	}

	@Override
	public void addWorkersTo(WorkerTree workerTree) {
		workerTree.addWorker(new CFGCollector());	
	}


	@Override
	public String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.cfg";
	}

	@Override
	public String getName() {
		return "Create CFG";
	}
}
