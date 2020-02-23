package de.tudarmstadt.rxrefactoring.core.internal.execution;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

public class NullWorker implements IWorker<Void, Void>{

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary,
			RefactorScope scope) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
