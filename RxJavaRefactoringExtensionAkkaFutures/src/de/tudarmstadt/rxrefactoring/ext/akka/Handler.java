package de.tudarmstadt.rxrefactoring.ext.akka;

import de.tudarmstadt.rxrefactoring.core.RefactoringExtension;
import de.tudarmstadt.rxrefactoring.core.handler.RefactoringHandler;

/**
 * Description: Passes the event to the Rx Refactoring Core Tool<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class Handler extends RefactoringHandler {

	@Override
	public RefactoringExtension createEnvironment() {		
		return new Extension();
	} 
}
