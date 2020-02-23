package de.tudarmstadt.rxrefactoring.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

@FunctionalInterface
public interface IWorker<Input, Output> {

	@SuppressWarnings("null")
	default public @NonNull String getName() {
		return getClass().getName();
	}

	/**
	 * The worker executes its refactoring on the given compilation units and
	 * outputs of parent workers. The refactoring is defined by the AST-modifying
	 * methods of the provided {@link RewriteCompilationUnit}.
	 * 
	 * @param units
	 *            The units that are refactored.
	 * @param input
	 *            The result of the parent worker.
	 * @param summary
	 *            The worker summary to report the refactoring.
	 *            
	 * @param scope
	 * 			  The scope how distinct the refactorings are possible. TODO THA
	 * 
	 * @return An output that gets passed to child workers.
	 * 
	 * @throws Exception
	 *             if there is a problem with the refactoring.
	 */
	public @Nullable Output refactor(@NonNull IProjectUnits units, @Nullable Input input,
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception;

}