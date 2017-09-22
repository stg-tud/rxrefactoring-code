package de.tudarmstadt.rxrefactoring.core;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.internal.RefactorExecution;

/**
 * Starts the refactoring when the button is clicked by invoking a
 * {@link RefactorExecution}.
 * 
 * @author Grebiel Jose Ifill Brito, Mirko Köhler
 */
public abstract class RefactoringHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		RefactorExecution app = new RefactorExecution(createExtension());
		app.run();

		return null;
	}

	/**
	 * Creates a new refactoring that should be used to run the refactoring
	 * execution. <br>
	 * <br>
	 * The same class will be used to refactor the whole workspace. However, there
	 * will be a call to
	 * {@link RefactorExtension#addWorkersTo(de.tudarmstadt.rxrefactoring.core.workers.WorkerTree)}
	 * for each project in the workspace.
	 * 
	 * @return A non-null refactoring.
	 */
	public abstract @NonNull RefactorExtension createExtension();
}
