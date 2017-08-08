package de.tudarmstadt.rxrefactoring.ext.asynctask;

import de.tudarmstadt.rxrefactoring.core.Refactoring;
import de.tudarmstadt.rxrefactoring.core.handler.RefactoringHandler;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Handler extends RefactoringHandler {

	@Override
	public Refactoring createEnvironment() {
		return new AsyncTaskRefactoring();
	}
	
}
