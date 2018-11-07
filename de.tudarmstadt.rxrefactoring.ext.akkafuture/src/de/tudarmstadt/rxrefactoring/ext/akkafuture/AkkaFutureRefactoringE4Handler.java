package de.tudarmstadt.rxrefactoring.ext.akkafuture;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Shell;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringE4Handler;

public class AkkaFutureRefactoringE4Handler extends RefactoringE4Handler {

		
	@Override
	public @NonNull IRefactorExtension createExtension() {
		return new AkkaFutureRefactoringExtension();
	}

}
