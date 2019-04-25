package de.tudarmstadt.rxrefactoring.core;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Shell;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RefactorExecution;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RefactorExecutionWithTesting;

/**
 * Starts the refactoring when the button is clicked by invoking a
 * {@link RefactorExecution}.
 * 
 * @author Grebiel Jose Ifill Brito, Mirko Köhler
 */
public abstract class RefactoringE4Handler {

	/*
	 * Callback for the handler.
	 */
	@Execute
	public final void execute(Shell shell) {
		RefactorExecution app = new RefactorExecutionWithTesting(createExtension());
		app.run();
	}

	/**
	 * Creates a new refactoring that is used to run the refactoring
	 * execution. <br>
	 * <br>
	 * The same class is used to refactor the whole workspace. However, there
	 * will be a call to {@link IRefactorExtension#addWorkersTo(de.tudarmstadt.rxrefactoring.core.workers.WorkerTree)}
	 * for each project in the workspace.
	 * 
	 * @return A non-null refactoring.
	 */
	public abstract @NonNull IRefactorExtension createExtension();
}
