package de.tudarmstadt.rxrefactoring.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.osgi.framework.Bundle;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.handler.IUIIntegration;
import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.parser.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.RewriteCompilationUnitFactory;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ConstantStrings;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.ProjectStatus;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.ProjectSummary;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;

/**
 * This class is used to run the refactoring on all
 * workspace projects.
 * 
 * @author mirko
 *
 */
public class RefactorApplication {
	
	/**
	 * Defines the environment that is used for the refactoring.
	 * Usually this is given by the extension .
	 */
	private final Refactoring env;
	
	/**
	 * Provides utility to communicate with the user.
	 */
	private final IUIIntegration ui;
	

	
	public RefactorApplication(IUIIntegration log, Refactoring env) {
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
		
		
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);		
		try {
			dialog.run(true, false, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					
					String description = env.getDescription();
					Objects.requireNonNull(description, "The description of the environment may not be null.");
					
					IProject[] projects = workspaceRoot.getProjects();
					
					monitor.beginTask(env.getDescription(), projects.length);
						
					
					
					//Reports that the refactoring is starting
					summary.reportStarted();
						
					//Iterate over all projects
					for (IProject project : projects) {
						
						ProjectSummary projectSummary = summary.reportProject(project);
						
						//Try to refactor the project
						try {
							//Check whether the project is open and if it is a Java project
							if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
								Log.info(getClass(), ">>> Refactor project: " + project.getName());
								//Reports the project as being refactored
								
								
								IJavaProject javaProject = JavaCore.create(project);
								
								//Adds the additional resource files to the project.
								Log.info(getClass(), "Copy library resources...");	
								addResourceFiles(project, javaProject);
								
								//Finds all java files in the project and produces the according bundled unit.	
								Log.info(getClass(), "Parse compilation units...");	
								ProjectUnits units = parseCompilationUnits(javaProject);
								
								//Performs the refactoring by applying the workers of the extension.
								Log.info(getClass(), "Refactor units...");	
								doRefactorProject(units, projectSummary);
								
								//Reports a successful refactoring
								projectSummary.reportStatus(ProjectStatus.COMPLETED);
								Log.info(getClass(), "<<< Refactor project");
								
							} else {								
								projectSummary.reportStatus(ProjectStatus.SKIPPED);
								Log.info(getClass(), "Skipping project: " + project.getName());
							}
						} catch (Exception e) {
							projectSummary.reportStatus(ProjectStatus.ERROR);
							Log.handleException(getClass(), "refactoring of " + project.getName() + " ###", e);
						}
						
						monitor.worked(1);
					}
					
					//Reports that the refactoring is finished.
					Log.info(getClass(), "Finished.");
					summary.reportFinished();		
					monitor.done();
				}				
			});
		} catch (InterruptedException e) {
			//TODO: Handle case if the refactoring is cancelled.
			Log.handleException(getClass(), "Execution interrupted", e);
			
		} catch (InvocationTargetException e) {
			Log.handleException(getClass(), "Something happened", e);
		} finally {
			dialog.close();
		}
		
		Log.info(getClass(), "Print summary...\n" + summary.toString());
	}
	

	//TODO: What about exceptions?
	private void addResourceFiles(IProject project, IJavaProject javaProject) throws IOException, CoreException, URISyntaxException {
				
		IPath localResourcePath = env.getResourceDir();
		IPath localDestPath = env.getDestinationDir();
		
		//Do not continue if there are no jars to add.
		if (localResourcePath == null || localDestPath == null) {
			return;
		}
		
		//Produce the complete resource path
		//Retrieve the location of the plugin eclipse project.
		Bundle bundle = Platform.getBundle(env.getPlugInId());
		URL url = FileLocator.resolve(bundle.getEntry("/"));				
		File sourceDir = Paths.get(url.toURI()).resolve(localResourcePath.toOSString()).toFile(); //This is pretty ugly. Is there a better way to do it?
		
		//Produces the library path inside the project
		IPath destPath = project.getLocation().append(localDestPath);
		File destDir = destPath.toFile();

		//IPath s = ResourcesPlugin.getWorkspace().getPathVariableManager().resolveURI(resourcePath);
		String s1 = sourceDir.getAbsolutePath();
		
		//Copy resource jars to library 
		//TODO: Fix resource path, currently: /home/mirko/resources
		FileUtils.copyDirectory(sourceDir, destDir);
		
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
		for (IClasspathEntry oldEntry : oldEntries) {
			classPathEntries.add(0, oldEntry);
		}
		
		for (File destFile : destFiles) {
					
			//TODO: Include source files in the class path
			if (destFile.getPath().endsWith("-sources.jar")) {
				continue;
			} else {
				//TODO: Why is the library already added to the class path?
				//classPathEntries.add(0, JavaCore.newLibraryEntry(destPath.append(destFile.getName()), null, null));
			}
			
//			String ap = libs.getAbsolutePath() + File.separator + lib;
//			IPath sourcesPath = null;
//			if (sourceLibs.contains(lib)) {
//				sourcesPath = Path.fromOSString(ap.replace(".jar", "-sources.jar"));
//			}					
			
		}
				
		
		
		javaProject.setRawClasspath(classPathEntries.toArray(new IClasspathEntry[classPathEntries.size()]), null);
	}
	
	
//	public static String getPluginDir( String pluginId )
//	{
//		// get bundle with the specified id
//		Bundle bundle = Platform.getBundle( pluginId );
//		if ( bundle == null )
//			throw new RuntimeException( "Could not resolve plugin: " + pluginId + "\r\n" +
//					"Probably the plugin has not been correctly installed.\r\n" +
//					"Running eclipse from shell with -clean option may rectify installation." );
//
//		// resolve Bundle::getEntry to local URL
//		URL pluginURL = null;
//		try
//		{
//			pluginURL = Platform.resolve( bundle.getEntry( "/" ) );
//		}
//		catch ( IOException e )
//		{
//			throw new RuntimeException( "Could not get installation directory of the plugin: " + pluginId );
//		}
//		String pluginInstallDir = pluginURL.getPath().trim();
//		if ( pluginInstallDir.length() == 0 )
//			throw new RuntimeException( "Could not get installation directory of the plugin: " + pluginId );
//
//		/*
//		 * since path returned by URL::getPath starts with a forward slash, that
//		 * is not suitable to run commandlines on Windows-OS, but for Unix-based
//		 * OSes it is needed. So strip one character for windows. There seems
//		 * to be no other clean way of doing this.
//		 */
//		if ( Platform.getOS().compareTo( Platform.OS_WIN32 ) == 0 )
//			pluginInstallDir = pluginInstallDir.substring( 1 );
//
//		return pluginInstallDir;
//	}
		
	
	private ProjectUnits parseCompilationUnits(IJavaProject javaProject) throws JavaModelException {
		
		IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
				
		Set<RewriteCompilationUnit> result = Sets.newConcurrentHashSet();		
		
		//Initializes a new thread pool.
		ExecutorService executor = env.createExecutorService();
		Objects.requireNonNull(executor, "The environments executor service can not be null.");
		
		
		for (IPackageFragmentRoot root : roots) {		
			//Check whether the root contains source or class files.
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				
				//Iterate over all java elements found in the sources root
				for (IJavaElement javaElement : root.getChildren()) {
					//Check whether the element found was a package fragment
					if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
						IPackageFragment packageFragment = (IPackageFragment) javaElement;
						ICompilationUnit[] units = packageFragment.getCompilationUnits();
											
						if (units.length > 0) {							
							//Asynchronously parse units
							executor.submit(() -> {								
								//Produces a new compilation unit factory.
								RewriteCompilationUnitFactory factory = new RewriteCompilationUnitFactory();
								
								//Find the compilation units, i.e. .java source files.
								for (ICompilationUnit unit : units) {
									result.add(factory.from(unit));
								}
							});		
						}
											
					}
				}			
			}
		}
		
		//Wait for asynchronous operations to finish.
		try {
			executor.shutdown();		
			executor.awaitTermination(3, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			//It should not be possible that the execution is interrupted.
			throw new IllegalStateException(e);		
		}		
		
		return new ProjectUnits(result);
	
	}
	
	
	
	private void doRefactorProject(ProjectUnits units, ProjectSummary projectSummary) throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException {
				
		//Produce the worker tree 
		WorkerTree workerTree = new WorkerTree(units, projectSummary);
		env.addWorkersTo(workerTree);		
		
		//The workers add their changes to the bundled compilation units
		workerTree.run();
		
		//The changes of the compilation units are applied
		Log.info(getClass(), "Write changes...");
		units.applyChanges();
		
	}

	
	
}
