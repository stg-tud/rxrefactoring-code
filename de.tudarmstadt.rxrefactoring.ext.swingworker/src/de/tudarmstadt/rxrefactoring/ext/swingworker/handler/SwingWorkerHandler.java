package de.tudarmstadt.rxrefactoring.ext.swingworker.handler;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactoringE4Handler;
import de.tudarmstadt.rxrefactoring.ext.swingworker.extensions.SwingWorkerExtension;

/**
 * Description: Handler class for SwingWorker<br>
 * Author: Camila Gonzalez<br>
 * Created: 18/01/2018
 */
public class SwingWorkerHandler extends RefactoringE4Handler {

	@Override
	public @NonNull IRefactorExtension createExtension() {
		return new SwingWorkerExtension();
	}

}
