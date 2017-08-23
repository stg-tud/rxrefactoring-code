package de.tudarmstadt.rxrefactoring.ext.akkafuture;

import de.tudarmstadt.rxrefactoring.core.Refactoring;
import de.tudarmstadt.rxrefactoring.core.handler.RefactoringHandler;

public class Handler extends RefactoringHandler {

	@Override
	public Refactoring createRefactoring() {		
		return new AkkaFutureRefactoring();
	}
}
