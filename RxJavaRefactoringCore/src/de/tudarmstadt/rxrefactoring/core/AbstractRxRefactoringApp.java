package de.tudarmstadt.rxrefactoring.core;

import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.tudarmstadt.rxrefactoring.core.collect.Collector;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import rx.Observable;
import rx.Subscription;

/**
 * Description: Abstract refactoring application. This class updates the
 * classpath given a directory name that contain the jar files and selects the
 * compilation units of all open Java Projects in the workspaces.<br>
 * This class contains code from the tool
 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a> Author:
 * Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
abstract class AbstractRxRefactoringApp implements IApplication {
	private static Subscription subscription;

	protected static final String PLUGIN_ID = "de.tudarmstadt.stg.rxjava.refactoring.core";

	protected Map<String, ICompilationUnit> compilationUnitsMap;

	protected RxRefactoringExtension extension;

	protected Shell activeShell;
	protected static boolean runningForTests = false;
	protected final String dialogTitle = "RxJavaRefactoring";
	private String commandId;
	private Set<String> errorProjects;

	private static final String PACKAGE_SEPARATOR = ".";

	public AbstractRxRefactoringApp() {
		compilationUnitsMap = new HashMap<>();
		errorProjects = new HashSet<>();
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public void setExtension(RxRefactoringExtension extension) {
		this.extension = extension;
	}

	@Override
	public Object start(IApplicationContext iApplicationContext) throws Exception {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		long startTime = System.currentTimeMillis();
		activeShell = Display.getCurrent().getActiveShell();
		if (subscription == null || subscription.isUnsubscribed()) {
			boolean confirmed = showConfirmationDialog();
			if (confirmed) {
				subscription = Observable.from(workspaceRoot.getProjects())
						.doOnSubscribe(() -> Log.info(AbstractRxRefactoringApp.this.getClass(),
								"Rx Refactoring Plugin Starting..."))
						.filter(AbstractRxRefactoringApp.this::isJavaProjectOpen)
						.doOnNext(AbstractRxRefactoringApp.this::refactorProject).doOnError(t -> showErrorDialog(t))
						.doOnCompleted(() -> showOnCompletedDialogAndLogTotalTime(startTime)).subscribe();
			}
		} else {
			showAlreadyRunningDialog();
		}

		return null;
	}

	@Override
	public void stop() {

	}

	/**
	 * @return Map containing the binary name of a compilation unit and the
	 *         compilation unit object.
	 */
	public Map<String, ICompilationUnit> getCompilationUnitsMap() {
		return compilationUnitsMap;
	}

	/**
	 * refactor each compilation unit separately
	 *
	 * @param project
	 * @param units
	 */
	protected abstract void refactorCompilationUnits(IJavaProject project, Map<String, ICompilationUnit> units);

	/**
	 * returns the directory name that contains all necessary dependencies to
	 * perform the refactorings. i.e: "all-deps"
	 * 
	 * @return directory name
	 */
	protected abstract String getDependenciesDirectoryName();

	protected void processUnitFromExtension(IJavaProject project, final ICompilationUnit unit,
			final RxRefactoringExtension<Collector> extension, final Collector collector) {
		ISafeRunnable runnable = new ISafeRunnable() {
			@Override
			public void handleException(Throwable throwable) {
				Log.errorInClient(getClass(), throwable);
				errorProjects.add(project.getPath().toString());
			}

			@Override
			public void run() throws Exception {
				if (commandId.equals(extension.getId())) {
					collector.processCompilationUnit(unit);
				}
			}
		};
		SafeRunner.run(runnable);
	}

	// ### Private Methods ###

	private void showAlreadyRunningDialog() {
		String message = "Another refactoring is currently being performed. Please wait " + "until it has completed.";
		MessageDialog.openInformation(activeShell, dialogTitle, message);
	}

	private void showOnCompletedDialogAndLogTotalTime(long startTime) {
		Log.info(AbstractRxRefactoringApp.this.getClass(), "Rx Refactoring Plugin Done!");
		String projects = String.join(", ", errorProjects);
		String warningMessage = "Check the following projects carefully. Exceptions were thrown"
				+ " during refactoring:\n\n" + projects + ".";
		if (!errorProjects.isEmpty()) {
			Log.showInConsole(AbstractRxRefactoringApp.this, warningMessage);
		}

		DecimalFormat df = new DecimalFormat("#,##0.00");
		Log.showInConsole(AbstractRxRefactoringApp.this,
				"Total Time: " + df.format(getTotalTimeInMinutes(startTime)) + " min.");
		if (!runningForTests) {
			if (!errorProjects.isEmpty()) {
				Display.getDefault()
						.asyncExec(() -> MessageDialog.openWarning(activeShell, dialogTitle, warningMessage));
			}

			String message = "The refactoring action has completed.";
			Display.getDefault().asyncExec(() -> MessageDialog.openInformation(activeShell, dialogTitle, message));
		}
	}

	private void showErrorDialog(Throwable t) {
		String message = "The refactoring action for one or more projects has failed";
		Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, t);
		t.printStackTrace();
		Display.getDefault().asyncExec(() -> ErrorDialog.openError(activeShell, dialogTitle, message, status));
	}

	private boolean showConfirmationDialog() {
		boolean confirmed = true;
		if (!runningForTests) {
			String confirmMessage = "Are you sure that you want to perform this refactoring?\n\n"
					+ "All opened java projects in the workspace will be refactored. "
					+ "If you would like to exclude some projects, then click on Cancel, close "
					+ "the corresponding projects and start the refactoring command again.";
			confirmed = MessageDialog.openConfirm(activeShell, dialogTitle, confirmMessage);
		}
		return confirmed;
	}

	private double getTotalTimeInMinutes(long startTime) {
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		return totalTime / 1000.0 / 60.0;
	}

	private void refactorProject(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		try {
			Log.info(getClass(), "METHOD=refactorProject : PROJECT ----> " + project.getName());
			final String location = project.getLocation().toPortableString();
			addJarFiles(location);
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			String dependenciesDir = Paths.get(location, getDependenciesDirectoryName()).toAbsolutePath().toString();
			updateClassPath(dependenciesDir, javaProject);
			compilationUnitsMap = getCompilationUnits(javaProject);
			refactorCompilationUnits(javaProject, compilationUnitsMap);
		} catch (JavaModelException e) {
			Log.error(getClass(), "Project: " + project.getName() + " could not be refactored.", e);
		} catch (CoreException e) {
			Log.error(getClass(), "Project: " + project.getName() + " resources could not be refreshed.", e);
		}
	}

	private void addJarFiles(String location) {
		try {
			java.nio.file.Path jarFilesPath = this.extension.getLibJars();
			if (jarFilesPath == null) {
				return;
			}

			// copy jar files to DEPENDENCIES_DIRECTORY
			String destinationDirectory = Paths.get(location, getDependenciesDirectoryName()).toAbsolutePath()
					.toString();
			FileUtils.copyDirectory(new File(jarFilesPath.toString()), new File(destinationDirectory));
		} catch (Throwable throwable) {
			Log.errorInClient(getClass(), throwable);
			return;
		}
	}

	private void updateClassPath(String location, IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		File libs = new File(location);
		if (libs.isDirectory() && libs.exists()) {
			String[] allLibs = libs.list();
			List<String> sourceLibs = Observable.from(allLibs).filter(lib -> lib.contains("-sources.jar"))
					.map(lib -> lib.replace("-sources", "")).toList().toBlocking().single();

			Set<IClasspathEntry> cps = new HashSet<>();
			for (String lib : allLibs) {
				if (lib.contains("-sources.jar")) {
					continue;
				}
				String ap = libs.getAbsolutePath() + File.separator + lib;
				IPath sourcesPath = null;
				if (sourceLibs.contains(lib)) {
					sourcesPath = Path.fromOSString(ap.replace(".jar", "-sources.jar"));
				}
				cps.add(JavaCore.newLibraryEntry(Path.fromOSString(ap), sourcesPath, null));
			}
			for (IClasspathEntry oldEntry : oldEntries) {
				cps.add(oldEntry);
			}
			javaProject.setRawClasspath(cps.toArray(new IClasspathEntry[0]), null);
		}
	}

	private Map<String, ICompilationUnit> getCompilationUnits(IJavaProject javaProject) throws JavaModelException {

		IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
		IPackageFragment[] packages = getPackageFragmentsInRoots(roots);
		return getCompilationUnitInPackages(packages);
	}

	private boolean isJavaProjectOpen(IProject iProject) {
		try {
			return iProject.isOpen() && iProject.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			return false;
		}

	}

	private IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots) throws JavaModelException {
		ArrayList<IJavaElement> frags = new ArrayList<>();
		for (IPackageFragmentRoot root : roots) {
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IJavaElement[] rootFragments = root.getChildren();
				frags.addAll(Arrays.asList(rootFragments));
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[frags.size()];
		frags.toArray(fragments);
		return fragments;
	}

	private Map<String, ICompilationUnit> getCompilationUnitInPackages(IPackageFragment[] packages)
			throws JavaModelException {
		Map<String, ICompilationUnit> cUnitsMap = new HashMap<>();
		for (IPackageFragment packageFragment : packages) {
			ICompilationUnit[] units = packageFragment.getCompilationUnits();
			for (ICompilationUnit unit : units) {
				String binaryName = packageFragment.getElementName() + PACKAGE_SEPARATOR + unit.getElementName();
				binaryName = binaryName.substring(0, binaryName.lastIndexOf(".java"));
				cUnitsMap.put(binaryName, unit);
			}
		}
		return cUnitsMap;
	}
}
