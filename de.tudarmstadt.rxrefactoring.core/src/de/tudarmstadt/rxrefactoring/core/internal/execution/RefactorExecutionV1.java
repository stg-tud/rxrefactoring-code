package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.osgi.framework.Bundle;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.ProcessDialog;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.ProjectStatus;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.ProjectSummary;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;

/**
 * This class is used to run the refactoring on all workspace projects.
 * 
 * @author mirko
 *
 */
public class RefactorExecution implements Runnable {

	/**
	 * Defines the environment that is used for the refactoring. Usually this is
	 * given by the extension .
	 */
	private final IRefactorExtension extension;
	private RefactorScope scope;

	public RefactorExecution(IRefactorExtension env) {
		Objects.requireNonNull(env);
		this.extension = env;
	}

	private Refactoring createRefactoring() {
		return new Refactoring() {

			@Override
			public String getName() {
				return extension.getName();
			}

			@Override
			public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
					throws CoreException, OperationCanceledException {
				// TODO: Check intial conditions here...
				return new RefactoringStatus();
			}

			@Override
			public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
					throws CoreException, OperationCanceledException {
				// TODO: Check final conditions here...
				return new RefactoringStatus();
			}

			@Override
			public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {

				// Retrieve projects in the workspace
				IProject[] projects = getWorkspaceProjects();

				monitor.beginTask(extension.getDescription(), projects.length);

				preRefactor(projects);

				// Gathers information about the refactoring and presents it to the user.
				RefactorSummary summary = new RefactorSummary(extension.getName());
				CompositeChange[] changes = null;

				// Tracks the changes that have been done by this refactoring
				/*CompositeChange change1 = new CompositeChange(extension.getName());
				CompositeChange change2 = new CompositeChange(extension.getName() + "!");
				changesOfAll[0] = change1;
				changesOfAll[1] = change2;*/
				

				// Reports that the refactoring is starting
				summary.reportStarted();

				// Iterate over all projects
				for (IProject project : projects) {

					ProjectSummary projectSummary = summary.reportProject(project);
					// Try to refactor the project
					try {
						Objects.requireNonNull(project);

						// Check whether the project is open and if it is a Java project
						if (considerProject(project)) {
							// Reports the project as completed. In case of an error this status can get
							// changed during execution.
							projectSummary.reportStatus(ProjectStatus.COMPLETED);

							Log.info(RefactorExecution.class, ">>> Refactor project: " + project.getName());
							// Reports the project as being refactored

							@SuppressWarnings("null")
							@NonNull
							IJavaProject javaProject = JavaCore.create(project);

							// Adds the additional resource files to the project.
							Log.info(RefactorExecution.class, "Copy library resources...");
							addResourceFiles(project, javaProject);

							// Finds all java files in the project and produces the according bundled unit.
							Log.info(RefactorExecution.class, "Parse compilation units...");
							ProjectUnits units = parseCompilationUnits(javaProject);
							
							// Performs the refactoring by applying the workers of the extension.
							Log.info(RefactorExecution.class, "Refactor units...");
							changes = doRefactorProject(units, projectSummary, project);
							
							
							// Call template method
							onProjectFinished(project, javaProject, units);

							Log.info(RefactorExecution.class, "<<< Refactor project");

						} else {
							projectSummary.reportStatus(ProjectStatus.SKIPPED);
							Log.info(RefactorExecution.class, "Skipping project: " + project.getName());
						}
					} catch (InterruptedException e) {
						throw new CoreException(new Status(IStatus.CANCEL, extension.getPlugInId(), IStatus.CANCEL,
								"The execution has been interrupted.", e));

					} catch (Exception e) {
						projectSummary.reportStatus(ProjectStatus.ERROR);
						Log.error(RefactorExecution.class, "Error during refactoring of " + project.getName(), e);
						throw new CoreException(new Status(IStatus.ERROR, extension.getPlugInId(), IStatus.ERROR,
								"Error during refactoring of " + project.getName(), e));
					}

					monitor.worked(1);
				}

				// Reports that the refactoring is finished.
				Log.info(RefactorExecution.class, "Finished.");
				summary.reportFinished();
				monitor.done();

				Log.info(RefactorExecution.class, "Print summary...\n" + summary.toString());
				
				CompositeChange resultChange = new CompositeChange("Testtt", changes);

				return resultChange;

			}
		};
	}

	public void run() {
		Refactoring refactoring = createRefactoring();
		RefactoringWizard wizard = new RefactoringWizard(refactoring, RefactoringWizard.WIZARD_BASED_USER_INTERFACE) {
			@Override
			protected void addUserInputPages() {
				   UserInputWizardPage page = new UserInputWizardPage("input") {

						@Override
						public void createControl(Composite parent) {
							Composite container = new Composite(parent, SWT.NULL);
				            GridLayout layout = new GridLayout();
				            container.setLayout(layout);
				            layout.numColumns = 1;
				            layout.verticalSpacing = 3;
				            Label label = new Label(container, SWT.NULL);
				            label.setText("Choose if yout want to refactor the whole project or not!");
				            Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
				            combo.add("Refactor the whole project");
				            combo.add("Refactor every occurence separately");
				            combo.addSelectionListener(new SelectionAdapter(){
				                public void widgetSelected(SelectionEvent e) {
				                    if (combo.getText().equals("Refactor the whole project")) {
				                    	scope = RefactorScope.WHOLE_PROJECT;
				                    } else {
				                    	scope = RefactorScope.SEPARATE_OCCURENCES;
				                    }
				                }});
				            
				            setControl(container);
							
						}
				    };
				    addPage(page);
			}
			
		};
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
		
		Shell shell = Display.getCurrent().getActiveShell();
		
		ProcessDialog dialog = new ProcessDialog(shell);
		dialog.create();
		dialog.open();
		
		int result = IDialogConstants.CANCEL_ID;

		try {
			result = op.run(shell, "This is an dialog title!");
			
		} catch (InterruptedException e) {
			// operation was cancelled
			Log.info(RefactorExecution.class, "Operation was cancelled.");
		}

		if (result == IDialogConstants.OK_ID) {
			postRefactor();
		}

		Log.info(RefactorExecution.class, "Done.");
	}

	protected IProject[] getWorkspaceProjects() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return workspaceRoot.getProjects();
	}

	protected void preRefactor(IProject[] projects) {
		// This method is to be overwritten by subclasses.
	}

	protected void postRefactor() {
		// This method is to be overwritten by subclasses.
	}

	protected void onProjectFinished(IProject project, IJavaProject jproject, ProjectUnits units) {
		// This method is to be overwritten by subclasses.
	}

	// TODO: What about exceptions?
	private void addResourceFiles(IProject project, IJavaProject javaProject)
			throws IOException, CoreException, URISyntaxException {

		IPath localResourcePath = extension.getResourceDir();
		IPath localDestPath = extension.getDestinationDir();

		// Do not continue if there are no jars to add.
		if (localResourcePath == null || localDestPath == null) {
			return;
		}

		// Produce the complete resource path
		// Retrieve the location of the plugin eclipse project.
		Bundle bundle = Platform.getBundle(extension.getPlugInId());
		Objects.requireNonNull(bundle,
				"OSGI Bundle can not be found. Is " + extension.getPlugInId() + " the correct plugin id?");

		URL url = FileLocator.resolve(bundle.getEntry("/"));
		File sourceDir = Paths.get(url.toURI()).resolve(localResourcePath.toOSString()).toFile();

		// Produces the library path inside the project
		IPath destPath = project.getLocation().append(localDestPath);
		File destDir = destPath.toFile();

		// Copy resource jars to library
		FileUtils.copyDirectory(sourceDir, destDir);

		// Check if the destination does exist
		if (!destDir.isDirectory() || !destDir.exists()) {
			Log.info(RefactorExecution.class,
					"Destination directory has not been created. Do you have write access? Directory: " + destDir);
			return;
		}

		// Refresh project to include new files
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		// Add newly moved libraries to the classpath
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		// TODO: Differentiate between sources and classes
		File[] destFiles = destDir.listFiles();

		// List<String> sourceLibs = Observable.from(allLibs).filter(lib ->
		// lib.contains("-sources.jar"))
		// .map(lib -> lib.replace("-sources", "")).toList().toBlocking().single();

		List<IClasspathEntry> classPathEntries = new LinkedList<>();
		for (IClasspathEntry oldEntry : oldEntries) {
			classPathEntries.add(0, oldEntry);
		}

		for (File destFile : destFiles) {

			// TODO: Include source files in the class path
			if (destFile.getPath().endsWith("-sources.jar")) {
				continue;
			} else {
				// TODO: Why is the library already added to the class path?
				classPathEntries.add(0, JavaCore.newLibraryEntry(destPath.append(destFile.getName()), null, null));
			}

			// String ap = libs.getAbsolutePath() + File.separator + lib;
			// IPath sourcesPath = null;
			// if (sourceLibs.contains(lib)) {
			// sourcesPath = Path.fromOSString(ap.replace(".jar", "-sources.jar"));
			// }

		}

		try {
			javaProject.setRawClasspath(classPathEntries.toArray(new IClasspathEntry[classPathEntries.size()]), null);
		} catch (JavaModelException e) {
			Log.error(RefactorExecution.class, "Classpath was not set correctly, reason: " + e.getMessage());
		}

	}

	@NonNull
	protected ProjectUnits parseCompilationUnits(@NonNull IJavaProject javaProject) throws JavaModelException {

		IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();

		@SuppressWarnings("null")
		@NonNull
		Set<RewriteCompilationUnit> result = Sets.newConcurrentHashSet();

		// Initializes a new thread pool.
		ExecutorService executor = extension.createExecutorService();
		Objects.requireNonNull(executor, "The environments executor service can not be null.");

		for (IPackageFragmentRoot root : roots) {
			// Check whether the root contains source or class files.
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {

				// Iterate over all java elements found in the sources root
				for (IJavaElement javaElement : root.getChildren()) {
					// Check whether the element found was a package fragment
					if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
						IPackageFragment packageFragment = (IPackageFragment) javaElement;

						@SuppressWarnings("null")
						@NonNull
						ICompilationUnit[] units = packageFragment.getCompilationUnits();						

						if (units.length > 0) {
							// Asynchronously parse units
							executor.submit(() -> {
								// Produces a new compilation unit factory.
								RewriteCompilationUnitFactory factory = new RewriteCompilationUnitFactory();

								// Find the compilation units, i.e. .java source files.
								for (ICompilationUnit unit : units) {
									result.add(factory.from(unit));
								}
							});
						}
					}
				}
			}
		}

		// Wait for asynchronous operations to finish.
		try {
			executor.shutdown();
			executor.awaitTermination(300, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// It should not be possible that the execution is interrupted.
			throw new IllegalStateException(e);
		}

		return new ProjectUnits(javaProject, result);

	}

	private CompositeChange[] doRefactorProject(@NonNull ProjectUnits units,
			@NonNull ProjectSummary projectSummary, IProject project) throws IllegalArgumentException,
			MalformedTreeException, BadLocationException, CoreException, InterruptedException {

		// Produce the worker tree
		WorkerTree workerTree = new WorkerTree(units, projectSummary);
		extension.addWorkersTo(workerTree);

		// The workers add their changes to the bundled compilation units
		workerTree.run(extension.createExecutorService(), scope);
		
		Map<String, List<IRewriteCompilationUnit>> grouped = getUnitToChangeMapping(units);
		List<CompositeChange> changeList = new ArrayList<CompositeChange>();
		int i = 0;
		// The changes of the compilation units are applied
		Log.info(getClass(), "Write changes...");
		for(Map.Entry<String, List<IRewriteCompilationUnit>> entry: grouped.entrySet()) {
			CompositeChange change = new CompositeChange(entry.getKey());
			Set<RewriteCompilationUnit> set = entry.getValue().stream().map(e-> (RewriteCompilationUnit) e).collect(Collectors.toSet());
			ProjectUnits pu = new ProjectUnits(units.getJavaProject(), set);
			pu.addChangesTo(change);
			changeList.add(change);
		}
		
		CompositeChange[] array = changeList.toArray(new CompositeChange[changeList.size()]);
		
		return array;
		
	}

	private Map<String, List<IRewriteCompilationUnit>> getUnitToChangeMapping(ProjectUnits units) {
		Map<String, List<IRewriteCompilationUnit>> groupedByWorker = units.getUnits().stream()
				  .filter(unit-> unit.getWorker() != null)
				  .collect(Collectors.groupingBy(IRewriteCompilationUnit::getWorker));
				  
		
		return groupedByWorker;	
	}
		
	protected static boolean considerProject(IProject project) {
		try {
			return project.isOpen() && project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}
}