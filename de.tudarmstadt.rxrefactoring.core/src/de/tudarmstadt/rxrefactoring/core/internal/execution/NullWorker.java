package de.tudarmstadt.rxrefactoring.core.internal.execution;

import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;

public class NullWorker implements IWorker<Void, Void> {

	@Override
	public Void refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		return null;
	}

}
