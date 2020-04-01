package de.tudarmstadt.rxrefactoring.ext.swingworker;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringE4Handler;

public class SwingWokerOnlyVarDeclHandler extends RefactoringE4Handler {

	@Override
	public @NonNull IRefactorExtension createExtension() {
		return new SwingWorkerOnlyVarDeclExtension();
	}

}
