package de.tudarmstadt.rxrefactoring.core.internal.execution;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorkerV1;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

class NullWorkerV1 implements IWorkerV1<Void, Void> {

	@Override
	public Void refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		return null;
	}

}
