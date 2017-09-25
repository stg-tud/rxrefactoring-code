package de.tudarmstadt.rxrefactoring.ext.asynctask;

import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringHandler;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Handler extends RefactoringHandler {

	@Override
	public RefactorExtension createExtension() {
		return new AsyncTaskRefactoring();
	}
	
}
