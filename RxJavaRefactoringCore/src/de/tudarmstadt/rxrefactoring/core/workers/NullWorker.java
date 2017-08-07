package de.tudarmstadt.rxrefactoring.core.workers;

import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;

public class NullWorker implements IWorker<Void, Void> {

	@Override
	public Void refactor(Void input, ProjectUnits units, WorkerSummary summary) throws Exception {		
		return null;
	}

}
