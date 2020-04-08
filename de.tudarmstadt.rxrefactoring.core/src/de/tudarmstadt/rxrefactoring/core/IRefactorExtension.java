package de.tudarmstadt.rxrefactoring.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

/**
 * This interface is for defining refactorings.
 * 
 * @author mirko
 *
 */
public interface IRefactorExtension {
	
	
	default public RefactorScope getRefactorScope() {
		return RefactorScope.NO_SCOPE;
	}
	
	default void setRefactorScope(RefactorScope scope) {};
		

	/**
	 * Provides the name of the extension.
	 * 
	 * @return A non-null name.
	 */
	public @NonNull String getName();

	/**
	 * Provides a comprehensible description of the kind of refactoring that is done
	 * by this environment.
	 * 
	 * @return A non-null string with the description of the environment.
	 */
	public @NonNull String getDescription();

	/**
	 * Includes all workers that are used for this refactoring in the given
	 * worker tree. The worker tree models the dependency between the workers, i.e.
	 * a worker can depend on the result of another worker.
	 * 
	 * @param workerTree
	 *            A non-null worker tree where the workers should be added to.
	 */
	public void addWorkersTo(@NonNull IWorkerTree workerTree);

	/**
	 * Gets the symbolic name of the bundle of this plugin.
	 * 
	 * @return The symbolic bundle name defined as Bundle-SymbolicName in the
	 *         MANIFEST.MF.
	 */
	// TODO: Can this method be removed without changing the functionality?
	public @NonNull String getPlugInId();

	/**
	 * Specifies which jars should be included to the refactored project.
	 * 
	 * @return Path with all resource jars relative to the plugin root, or null if
	 *         no jars should be included.
	 */
	default public @Nullable IPath getResourceDir() {
		return null;
	}
	
	
	/**
	 * Runs the extension specific dependency check between units
	 * 
	 * @param units actual Project units
	 * @param scanner MethodScanner
	 * @return return new grouped units because of dependencies between changes
	 * @throws JavaModelException
	 */
	default public ProjectUnits runDependencyBetweenWorkerCheck(ProjectUnits units, MethodScanner scanner) throws JavaModelException{
		return null;
	};
	
	/**
	 * Runs the check where the cursor is and what expression should be refactored
	 * @param units Project units
	 * @param offset cursor offset
	 * @param startLine start of line of cursor
	 * @return
	 */
	default public ProjectUnits analyseCursorPosition(ProjectUnits units, int offset, int startLine) {
		return null;
	}
	
	/**
	 * Checks if the all projects in the workspace or only open file should be refactored
	 * @return false for the whole workspace and true if only the open file should be considered
	 */
	default boolean onlyScanOpenFile() {
		return false;
	}
	
	/**
	 * Clears all Maps that are used in the extension
	 */
	default public void clearAllMaps() {};
	

	/**
	 * Specifies where the jars should be included to the refactored project.
	 * 
	 * @return Path where the jars should be included relative to the refactored
	 *         project root, or null if no jars should be included.
	 */
	default public @Nullable IPath getDestinationDir() {
		return null; // new Path("./libs");
	}
	
	/**
	 * Specifies if the extension can handle also different RefactorScope of changes.
	 * 
	 * @return true if it is available
	 */
	default public @Nullable boolean hasInteractiveRefactorScope() {
		return false; 
	}

	/**
	 * Defines a thread pool that is used for parallel computations, i.e. parsing
	 * the source files and building the ASTs as well as executing the workers.
	 * 
	 * @return A non-null executor service.
	 */
	@SuppressWarnings("null")
	default public @NonNull ExecutorService createExecutorService() {
		return Executors.newFixedThreadPool(4);
	}
	
}
