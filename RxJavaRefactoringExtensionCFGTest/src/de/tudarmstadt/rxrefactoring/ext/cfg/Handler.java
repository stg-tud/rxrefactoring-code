package de.tudarmstadt.rxrefactoring.ext.cfg;

import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.core.handler.RefactoringHandler;

public class Handler extends RefactoringHandler {

	@Override
	public RefactorExtension createRefactoring() {		
		return new CFGRefactoring();
	}
}
