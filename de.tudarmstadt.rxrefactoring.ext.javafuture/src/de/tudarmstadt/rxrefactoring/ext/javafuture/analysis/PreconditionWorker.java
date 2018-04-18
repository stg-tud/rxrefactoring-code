package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;

public class PreconditionWorker implements IWorker<InstantiationUseWorker, InstantiationUseWorker> {

	@Override
	public @Nullable InstantiationUseWorker refactor(@NonNull IProjectUnits units,
			@Nullable InstantiationUseWorker input, @NonNull WorkerSummary summary) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
