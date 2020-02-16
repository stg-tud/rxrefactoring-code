package de.tudarmstadt.rxrefactoring.core.internal.execution;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

class NullWorker implements IWorker<Void, Void> {

	@Override
	public Void refactor(IProjectUnits units, Void input, WorkerSummary summary, RefactorScope scope) throws Exception {
		return null;
	}

}
