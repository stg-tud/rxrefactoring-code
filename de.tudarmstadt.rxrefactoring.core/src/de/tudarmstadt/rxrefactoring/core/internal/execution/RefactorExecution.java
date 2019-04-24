package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.osgi.framework.Bundle;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.ProjectStatus;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.ProjectSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.RandoopGenerator;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.Pair;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

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
				//TODO: Check intial conditions here...
				return new RefactoringStatus();
			}

			@Override
			public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
					throws CoreException, OperationCanceledException {
				//TODO: Check final conditions here...
				return new RefactoringStatus();
			}

			@Override
			public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				
				//Retrieve projects in the workspace
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IProject[] projects = workspaceRoot.getProjects();
				
				
				monitor.beginTask(extension.getDescription(), projects.length);
				

				// Gathers information about the refactoring and presents it to the user.
				RefactorSummary summary = new RefactorSummary(extension.getName());
				
				//Tracks the changes that have been done by this refactoring
				CompositeChange changes = new CompositeChange(extension.getName());
				
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
							// Reports the project as completed. In case of an error this status can get changed during execution.
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
							doRefactorProject(units, changes, projectSummary, project);

							
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
						Log.error(RefactorExecution.class, "Error during refactoring of " +project.getName() , e);
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
				
				return changes;				
			}
		};
	}
	
	
	
	public void run() {
		Refactoring refactoring = createRefactoring();
		RefactoringWizard wizard = new RefactoringWizard(refactoring, RefactoringWizard.WIZARD_BASED_USER_INTERFACE) {			
			@Override
			protected void addUserInputPages() {
				//TODO: Add user input pages here!				
			}
		};		
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
		
		Shell shell = Display.getCurrent().getActiveShell();

		int result = IDialogConstants.CANCEL_ID;
		try {
			result = op.run(shell, "This is an dialog title!");
		} catch (InterruptedException e) {
			//operation was cancelled 
			Log.info(RefactorExecution.class, "Operation was cancelled.");
		}

		if(result == IDialogConstants.OK_ID) {
		    postRefactor();
		}
	}
	
	
	protected void postRefactor() {
		//This method is to be overwritten by subclasses.
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
		Objects.requireNonNull(bundle, "OSGI Bundle could not be found. Is " + extension.getPlugInId() + " the correct plugin id?");
		
		URL url = FileLocator.resolve(bundle.getEntry("/"));
		File sourceDir = Paths.get(url.toURI()).resolve(localResourcePath.toOSString()).toFile(); // This is pretty
																									// ugly. Is there a
																									// better way to do
																									// it?

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
				// classPathEntries.add(0,
				// JavaCore.newLibraryEntry(destPath.append(destFile.getName()), null, null));
			}

			// String ap = libs.getAbsolutePath() + File.separator + lib;
			// IPath sourcesPath = null;
			// if (sourceLibs.contains(lib)) {
			// sourcesPath = Path.fromOSString(ap.replace(".jar", "-sources.jar"));
			// }

		}

		javaProject.setRawClasspath(classPathEntries.toArray(new IClasspathEntry[classPathEntries.size()]), null);
	}


	@NonNull
	protected ProjectUnits parseCompilationUnits(@NonNull IJavaProject javaProject) throws JavaModelException {

		IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();

		@SuppressWarnings("null")
		@NonNull Set<RewriteCompilationUnit> result = Sets.newConcurrentHashSet();

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
						@NonNull ICompilationUnit[] units = packageFragment.getCompilationUnits();

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
	

	protected void doRefactorProject(@NonNull ProjectUnits units, @NonNull CompositeChange changes, @NonNull ProjectSummary projectSummary, IProject project)
			throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException, InterruptedException {

		// Produce the worker tree
		WorkerTree workerTree = new WorkerTree(units, projectSummary);
		extension.addWorkersTo(workerTree);

		// The workers add their changes to the bundled compilation units
		workerTree.run(extension.createExecutorService());

		// The changes of the compilation units are applied
		Log.info(getClass(), "Write changes...");
		units.addChangesTo(changes);		
	}
	
	
	protected static boolean considerProject(IProject project) {
    	try {
    		return project.isOpen() && project.hasNature(JavaCore.NATURE_ID);            		
    	} catch (CoreException e) {
    		return false;
    	}
	}
}
