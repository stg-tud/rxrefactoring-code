package de.tudarmstadt.rxrefactoring.ext.template.workers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorkerV1;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;

public class TemplatePreconditionWorker implements IWorkerV1<Collection<UseDef>, TemplatePreconditionWorker> {

	public Set<ASTNode> instantiations = new HashSet<ASTNode>();
	
	@Override
	public @Nullable TemplatePreconditionWorker refactor(@NonNull IProjectUnits units,
			@Nullable Collection<UseDef> input, @NonNull WorkerSummary summary) throws Exception {
		/**
		 * TODO 7 - Collect instantiations of the constructs that are to be refactored, 
		 * such as subclasses of a certain class or its collections, and exclude 
		 * those that should not be refactored because of how they are used.
		 * 
		 */
		return this;
	}
}