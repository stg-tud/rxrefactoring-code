package de.tudarmstadt.rxrefactoring.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tudarmstadt.rxrefactoring.core.Refactoring;
import de.tudarmstadt.rxrefactoring.core.RefactorApplication;


/**
 * Description: Starts {@link RefactoringApp} when the RxJ icon is
 * clicked.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public abstract class RefactoringHandler extends AbstractHandler {

		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
				
		RefactorApplication app = new RefactorApplication(
				new DefaultUI(), 
				createEnvironment()
			);		
		app.run();
		
		return null;
	}
	
	
	public abstract Refactoring createEnvironment();
}
