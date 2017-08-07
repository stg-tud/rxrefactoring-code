package de.tudarmstadt.rxrefactoring.core.handler;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.ConstantStrings;
import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnitFactory;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.ProjectStatus;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;

/**
 * This class is used to run the refactoring on all
 * workspace projects.
 * 
 * @author mirko
 *
 */
public class RefactorExecution {
	
	/**
	 * Defines the environment that is used for the refactoring.
	 * Usually this is given by the extension .
	 */
	private final RefactorEnvironment env;
	
	/**
	 * Provides utility to communicate with the user.
	 */
	private final IUIIntegration ui;
	
	public RefactorExecution(IUIIntegration log, RefactorEnvironment env) {
		Objects.requireNonNull(env);
		Objects.requireNonNull(log);
		
		this.ui = log;
		this.env = env;			
	}
	
	
	public void run() {
			
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		
		Shell shell = Display.getCurrent().getActiveShell();		
		
		//Gathers information about the refactoring and presents it to the user.
		RefactorSummary summary = new RefactorSummary();
		
		//Display warning before the refactoring
		if (!ui.showConfirmationDialog(shell, "Confirm", ConstantStrings.DIALOG_CONFIRM_REFACTOR)) {
			Log.info(getClass(), "Aborted by user.");
			return;
		}
		
		//Reports that the refactoring is starting
		summary.reportStarted();
			
		//Iterate over all projects
		for (IProject project : workspaceRoot.getProjects()) {
			
			//Try to refactor the project
			try {
				//Check whether the project is open and if it is a Java project
				if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
					Log.info(getClass(), ">>> Refactor project: " + project.getName());
					IJavaProject javaProject = JavaCore.create(project);
					
					//Adds the additional resource files to the project.
					addResourceFiles(project, javaProject);
					
					//Finds all java files in the project and produces the according bundled unit.	
					ProjectUnits units = findCompilationUnits(javaProject);
					
					//Performs the refactoring by applying the workers of the extension.
					doRefactorProject(units, summary);
					
					//Reports a successful refactoring
					summary.reportProject(project, ProjectStatus.COMPLETED);
					Log.info(getClass(), "<<< Refactor project");
				} else {
					summary.reportProject(project, ProjectStatus.SKIPPED);
					Log.info(getClass(), "Skipping project: " + project.getName());
				}
			} catch (Exception e) {
				summary.reportProject(project, ProjectStatus.ERROR);
				Log.error(getClass(), "### Error during the refactoring of " + project.getName() + " ###");
				e.printStackTrace();
				Log.error(getClass(), "### End ###");
			}			
		}
		
		//Reports that the refactoring is finished.
		summary.reportFinished();
	}
	

	//TODO: What about exceptions?
	private void addResourceFiles(IProject project, IJavaProject javaProject) throws IOException, CoreException {
				
		IPath resourcePath = env.getResourceDir();
		IPath localDestPath = env.getDestinationDir();
		
		//Do not continue if there are no jars to add.
		if (resourcePath == null || localDestPath == null) {
			return;
		}
		
		//Produces the library path inside the project
		IPath destPath = project.getLocation().append(localDestPath);

		
		File sourceJars = resourcePath.toFile();
		File destDir = destPath.toFile();
		
		//Copy resource jars to library 		
		FileUtils.copyDirectory(resourcePath.toFile(), destPath.toFile());
		
		//Check if the destination does exist
		if (!destDir.isDirectory() || !destDir.exists()) {
			Log.info(getClass(), "Destination directory has not been created. Do you have write access? Directory: " + destDir);
			return;
		}
		
		//Refresh project to include new files		
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		
				
		//Add newly moved libraries to the classpath		
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		//TODO: Differentiate between sources and classes
		File[] destFiles = destDir.listFiles();
		
//		List<String> sourceLibs = Observable.from(allLibs).filter(lib -> lib.contains("-sources.jar"))
//					.map(lib -> lib.replace("-sources", "")).toList().toBlocking().single();

		
		
		List<IClasspathEntry> classPathEntries = new LinkedList<>();		
		for (File destFile : destFiles) {
			
			classPathEntries.add(0, JavaCore.newLibraryEntry(destPath.append(destFile.getName()), null, null));
			
			//if
			
//			if (destFile.contains("-sources.jar")) {
//				continue;
//			}
//						
//			
//			String ap = libs.getAbsolutePath() + File.separator + lib;
//			IPath sourcesPath = null;
//			if (sourceLibs.contains(lib)) {
//				sourcesPath = Path.fromOSString(ap.replace(".jar", "-sources.jar"));
//			}
			
					
			
		}
				
		for (IClasspathEntry oldEntry : oldEntries) {
			classPathEntries.add(oldEntry);
		}
		
		javaProject.setRawClasspath(classPathEntries.toArray(new IClasspathEntry[classPathEntries.size()]), null);
	}
		
	
	private ProjectUnits findCompilationUnits(IJavaProject javaProject) throws JavaModelException {
		
		IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
		
		Log.info(getClass(), "Read compilation units...");		
		
		Set<BundledCompilationUnit> result = Sets.newHashSet();
		BundledCompilationUnitFactory factory = new BundledCompilationUnitFactory();
		
		
		for (IPackageFragmentRoot root : roots) {		
			//Check whether the root contains source or class files.
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				
				//Iterate over all java elements found in the sources root
				for (IJavaElement javaElement : root.getChildren()) {
					//Check whether the element found was a package fragment
					if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
						IPackageFragment packageFragment = (IPackageFragment) javaElement;

						//Find the compilation units, i.e. .java source files.
						for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
							result.add(factory.from(unit));
						}
					}
				}			
			}
		}
		
		return new ProjectUnits(result);
	
	}
	
	
	
	private void doRefactorProject(ProjectUnits units, RefactorSummary summary) throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException {
		Set<IWorker> workers = env.workers();
		
		//TODO: Implement the refactoring.
		
		//The workers add their changes to the bundled compilation units
		for (IWorker worker: workers) {			
			WorkerSummary workerSummary = new WorkerSummary(worker);
			try {
				worker.refactor(units, workerSummary);
				workerSummary.setStatus(WorkerStatus.COMPLETED);
			} catch (Exception e) {
				workerSummary.setStatus(WorkerStatus.ERROR);
				Log.error(getClass(), "## Error during the execution of " + worker.getName() + " ##");
				e.printStackTrace();
				Log.error(getClass(), "## End ##");
			}				
		}
		
		//The changes of the compilation units are applied
		units.applyChanges();
		
	}

	
	
}
