package de.tudarmstadt.rxrefactoring.core.workers;

import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;

public interface IWorker {
	
	default public String getName() {
		return getClass().getName();
	}

	public void refactor(ProjectUnits units, WorkerSummary summary) throws Exception;
	
}
