package de.tudarmstadt.rxrefactoring.ext.cfg;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringE4Handler;

public class Handler extends RefactoringE4Handler {

	@Override
	public @NonNull IRefactorExtension createExtension() {		
		return new CFGExtension();
	}
}
