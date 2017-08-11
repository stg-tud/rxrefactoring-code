package de.tudarmstadt.rxrefactoring.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tudarmstadt.rxrefactoring.core.Refactoring;
import de.tudarmstadt.rxrefactoring.core.ui.DefaultUI;
import de.tudarmstadt.rxrefactoring.core.RefactorExecution;


/**
 * Starts the refactoring when the button is clicked by invoking a {@link RefactorExecution}.
 * 
 * @author Grebiel Jose Ifill Brito, Mirko Köhler
 */
public abstract class RefactoringHandler extends AbstractHandler {

		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
				
		RefactorExecution app = new RefactorExecution(
				new DefaultUI(), 
				createRefactoring()
			);		
		app.run();
		
		return null;
	}
	
	/**
	 * Creates a new refactoring that should be used
	 * to run the refactoring execution.
	 * 
	 * @return A non-null refactoring.
	 */
	public abstract Refactoring createRefactoring();
}
