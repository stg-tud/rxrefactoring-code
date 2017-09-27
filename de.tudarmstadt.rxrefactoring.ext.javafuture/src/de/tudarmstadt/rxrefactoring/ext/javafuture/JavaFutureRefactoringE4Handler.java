package de.tudarmstadt.rxrefactoring.ext.javafuture;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IRefactoringE4Handler;

public class JavaFutureRefactoringE4Handler implements IRefactoringE4Handler {

	@Override
	public @NonNull IRefactorExtension createExtension() {
		return new JavaFutureRefactoring();
	}
	
}
