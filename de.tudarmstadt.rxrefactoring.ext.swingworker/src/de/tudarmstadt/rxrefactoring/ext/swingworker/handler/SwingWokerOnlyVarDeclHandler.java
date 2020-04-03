package de.tudarmstadt.rxrefactoring.ext.swingworker.handler;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringE4Handler;
import de.tudarmstadt.rxrefactoring.ext.swingworker.extensions.SwingWorkerOnlyVarDeclExtension;

public class SwingWokerOnlyVarDeclHandler extends RefactoringE4Handler {

	@Override
	public @NonNull IRefactorExtension createExtension() {
		return new SwingWorkerOnlyVarDeclExtension();
	}

}
