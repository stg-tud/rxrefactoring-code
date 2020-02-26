package de.tudarmstadt.rxrefactoring.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This interface is for defining refactorings.
 * 
 * @author mirko
 *
 */
public interface IRefactorExtension {

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
	default public @Nullable boolean isRefactorScopeAvailable() {
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
