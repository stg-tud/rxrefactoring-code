package de.tudarmstadt.rxrefactoring.ext.asynctask;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringE4Handler;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Handler extends RefactoringE4Handler {

	@Override
	public IRefactorExtension createExtension() {
		return new AsyncTaskRefactoring();
	}
	
}
