package de.tudarmstadt.rxrefactoring.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tudarmstadt.rxrefactoring.core.RefactorEnvironment;
import de.tudarmstadt.rxrefactoring.core.RefactorExecution;


/**
 * Description: Starts {@link RefactoringApp} when the RxJ icon is
 * clicked.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public abstract class RefactoringHandler extends AbstractHandler {

		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		RefactorExecution execution = new RefactorExecution(
				new DefaultUI(), 
				createEnvironment()
			);		
		execution.run();
		
		return null;
	}
	
	
	public abstract RefactorEnvironment createEnvironment();
}
