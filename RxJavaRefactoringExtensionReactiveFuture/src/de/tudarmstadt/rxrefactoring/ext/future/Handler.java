package de.tudarmstadt.rxrefactoring.ext.future;

import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringHandler;

public class Handler extends RefactoringHandler {

	@Override
	public RefactorExtension createExtension() {		
		return new FutureRefactoringExtension();
	}
}
