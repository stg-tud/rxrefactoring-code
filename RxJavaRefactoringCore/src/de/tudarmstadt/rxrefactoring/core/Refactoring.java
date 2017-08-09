package de.tudarmstadt.rxrefactoring.core;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IPath;

import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;

public interface Refactoring {
		
	/**
	 * Provides a comprehensible description of the
	 * kind of refactoring that is done by this
	 * environment.
	 * 
	 * @return A non-null string with the description
	 * of the environment.
	 */
	public String getDescription();
	
	/**
	 * Includes all workers that
	 * should be used for this refactoring in the given worker
	 * tree.
	 * The worker tree models the dependency between the workers,
	 * i.e. a worker can depend on the result of another worker. 
	 * 
	 * @param workerTree A worker tree where the workers
	 * should be added to. 
	 */
	public void addWorkersTo(WorkerTree workerTree);
	
	/**
	 * Gets the symbolic name of the bundle of this
	 * plugin.
	 * 
	 * @return The symbolic bundle name defined as Bundle-SymbolicName
	 * in the MANIFEST.MF.
	 */
	//TODO: Can this method be removed without changing the functionality?
	public String getPlugInId();
	
	/**
	 * Specifies which jars should be included to the refactored project.
	 * 
	 * @return Path with all resource jars relative to the plugin root, 
	 * or null if no jars should be included.
	 */
	public default IPath getResourceDir() {
		return null;
	}

	/**
	 * Specifies where the jars should be included to the refactored project.
	 * 
	 * @return Path where the jars should be included relative to the 
	 * refactored project root, or null if no jars should be included.
	 */
	public default IPath getDestinationDir() {
		return null; //new Path("./libs");
	}
	
	/**
	 * Defines a thread pool that is used for parallel computations
	 * such as parsing the source files.
	 * 
	 * @return A non-null executor service.
	 */
	public default ExecutorService createExecutorService() {
		return Executors.newFixedThreadPool(4);
	}
}
