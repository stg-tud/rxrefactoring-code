package de.tudarmstadt.rxrefactoring.core.internal.execution;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;

class NullWorker implements IWorker<Void, Void> {

	@Override
	public Void refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		return null;
	}

}
