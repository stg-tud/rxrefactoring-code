package de.tudarmstadt.rxrefactoring.core.workers;

import java.util.Formattable;

import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;

public interface IWorker<Input, Output> {
	
	default public String getName() {
		return getClass().getName();
	}
	
	/**
	 * The worker executes its refactoring on the given compilation
	 * units and outputs of parent workers. 
	 * The refactoring is defined by the AST-modifying methods
	 * of the provided {@link BundledCompilationUnit}.
	 * @param units The units that are refactored.
	 * @param input The result of the parent worker.
	 * @param summary The worker summary to report the refactoring.
	 * 
	 * @return An output that gets passed to child workers.
	 * 
	 * @throws Exception if there is a problem with the refactoring.
	 */
	public Output refactor(ProjectUnits units, Input input, WorkerSummary summary) throws Exception;
	
}
