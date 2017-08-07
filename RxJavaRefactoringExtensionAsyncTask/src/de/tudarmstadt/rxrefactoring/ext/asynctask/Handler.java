package de.tudarmstadt.rxrefactoring.ext.asynctask;

import de.tudarmstadt.rxrefactoring.core.RefactorEnvironment;
import de.tudarmstadt.rxrefactoring.core.handler.RefactoringHandler;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Handler extends RefactoringHandler {

	@Override
	public RefactorEnvironment createEnvironment() {
		return new AsyncTaskEnvironment();
	}

	
}
