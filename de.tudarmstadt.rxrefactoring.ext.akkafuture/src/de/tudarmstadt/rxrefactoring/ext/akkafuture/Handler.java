package de.tudarmstadt.rxrefactoring.ext.akkafuture;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringHandler;

public class Handler extends RefactoringHandler {

	@Override
	public @NonNull RefactorExtension createExtension() {		
		return new AkkaFutureRefactoringExtension();
	}
}
