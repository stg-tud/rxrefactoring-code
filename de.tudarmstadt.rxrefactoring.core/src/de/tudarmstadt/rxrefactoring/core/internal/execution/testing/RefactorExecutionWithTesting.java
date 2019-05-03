package de.tudarmstadt.rxrefactoring.core.internal.execution.testing;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RefactorExecution;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class RefactorExecutionWithTesting extends RefactorExecution {

	/**
	 * IPL: Mapping of projects to found methods that contains all impacted and
	 * changed methods for each project.
	 */
//	private final Map<IProject, Pair<Set<String>, Set<String>>> foundMethods = new HashMap<>();

	private final RandoopGenerator rgen = new RandoopGenerator();
	private final MethodScanner scanner = new MethodScanner();

	public RefactorExecutionWithTesting(IRefactorExtension env) {
		super(env);
	}

	@Override
	protected void preRefactor(IProject[] projects) {
		super.preRefactor(projects);
	}

	@Override
	protected void onProjectFinished(IProject project, IJavaProject jproject, ProjectUnits units) {
		super.onProjectFinished(project, jproject, units);

		// IPL: Find the changing and calling methods
		Log.info(RefactorExecutionWithTesting.class, "Scan " + project.getName());
		scanner.scan(units);
		// IPL: Copy the pre-refactoring binaries over
		rgen.copyProjectBinariesToPre(project);
	}

	@Override
	protected void postRefactor() {
		super.postRefactor();

		rgen.copyRandoopLibraries();

		// IPL: OK has been pressed, time to continue post-refactoring
		IProject[] projects = getWorkspaceProjects();

		for (IProject project : projects) {

			if (considerProject(project)) {
				Log.info(getClass(), "Create Randoop specification for " + project.getName() + "...");

				// IPL: Copy over post-refactoring binaries
				rgen.copyProjectBinariesToPost(project);

				// IPL: Parse the refactored compilation units to obtain the ASTs
//				try {
//					ProjectUnits units = parseCompilationUnits(JavaCore.create(project));
//					scanner.addRefactoredUnit(units);
//				} catch (JavaModelException e) {
//					throw new RuntimeException(e);
//				}
			}
		}

		MethodScanner.ScanResult result = scanner.getResult();
		rgen.writeFiles(result.getTestClasses(), result.getOmmittedMethods());
		
		Log.info(RefactorExecutionWithTesting.class, "Created Randoop tests in " + rgen.getTempDir());
	}

}
