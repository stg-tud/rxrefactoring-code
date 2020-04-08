package de.tudarmstadt.rxrefactoring.core.internal.execution;

import org.eclipse.jdt.annotation.Nullable;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;

public class NullWorker implements IWorker<Void, Void>{

	@Override
	public @Nullable Void refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
