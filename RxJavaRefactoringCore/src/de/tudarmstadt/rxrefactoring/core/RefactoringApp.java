package de.tudarmstadt.rxrefactoring.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.tudarmstadt.rxrefactoring.core.collect.Collector;
import de.tudarmstadt.rxrefactoring.core.processors.AbstractRefactoringProcessor;
import de.tudarmstadt.rxrefactoring.core.processors.RefactoringProcessor;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import rx.Observable;

/**
 * Description: Refactoring application. This class assumes that the Rx
 * dependencies are already in the classpath or that the .jar files are in
 * directory in root called "/all-deps".<br>
 * This class contains code from the tool
 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a> Author:
 * Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class RefactoringApp extends AbstractRefactoringApp {
	// private static final String DEPENDENCIES_DIRECTORY = "lib";
	private Set<String> targetClasses;

	@Override
	protected String getDependenciesDirectoryName() {
		return extension.getLibPath().toString();
	}

	@Override
	public void refactorCompilationUnits(IJavaProject project, Map<String, ICompilationUnit> units) {
		Log.info(getClass(), "METHOD=refactorCompilationUnits - # units: " + units.size());

		Collector collector = this.extension.createCollector(project);
		if (collector == null) {
			Log.errorInClient(getClass(),
					new IllegalArgumentException("getASTNodesCollectorInstance must return not null"));
		}

		CountDownLatch latch = new CountDownLatch(1);
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(activeShell);
		try {
			dialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					beginTask(monitor);
					Observable.from(units.values())
							// Filter using the boolean formula "runningForTest -> validateName"
							.filter(unit -> !runningForTests || validateUnitName(unit)).doOnNext(unit -> {
								Log.info(RefactoringApp.class, "Refactor " + unit.getElementName());
								processUnitFromExtension(project, unit, RefactoringApp.this.extension, collector);
								monitor.worked(1);
							}).doOnCompleted(() -> refactorUnits(collector))
							.doOnError(t -> Log.error(getClass(), "METHOD=refactorCompilationUnits", t)).subscribe();
					latch.countDown();
					monitor.done();
				}

				private void beginTask(IProgressMonitor monitor) {
					int numberOfFiles = units.values().size();
					String pluralForm = numberOfFiles == 1 ? "" : "s";
					String message = "Project: " + project.getPath() + ". Analyzing " + numberOfFiles + " java file"
							+ pluralForm + ".";
					monitor.beginTask(message, numberOfFiles);
				}
			});
		} catch (Exception e) {
			Log.error(getClass(), "METHOD=refactorCompilationUnits, Project:" + project.getPath() + " - FAILED", e);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			String message = "The refactoring action has been interrupted.";
			Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
			ErrorDialog.openError(activeShell, dialogTitle, message, status);
		}
	}

	/**
	 * Specify the binary name of the classes that should be refactored. This method
	 * sets the flag runningForTests to true, which means that the files will not be
	 * changed. Therefore this method can only be used for unit tests.
	 * 
	 * @param classNames
	 *            binary names of the target classes
	 */
	public void refactorOnly(String... classNames) {
		this.targetClasses = new HashSet<>();
		this.targetClasses.addAll(Arrays.asList(classNames));
		runningForTests = true;
	}

	/**
	 * @return true if the app is being ran only for tests purposes
	 */
	public static boolean isRunningForTests() {
		return runningForTests;
	}

	// ### Private Methods ###

	private boolean validateUnitName(ICompilationUnit unit) {
		return this.targetClasses != null && this.targetClasses.contains(unit.getElementName());
	}

	private void refactorUnits(Collector collector) {
		Log.info(getClass(), "METHOD=refactorUnits - " + collector.getName() + " Refactoring starting...");
		AbstractRefactoringProcessor processor = new RefactoringProcessor(extension, collector);
		runProcessor(processor);
	}

	private void runProcessor(AbstractRefactoringProcessor processor) {
		try {
			NullProgressMonitor progressMonitor = new NullProgressMonitor();
			processor.createChange(progressMonitor);
		} catch (Exception e) {
			Log.error(getClass(), "METHOD=runProcessor, Processor:" + processor.getName() + " - FAILED", e);
		}
	}
}
