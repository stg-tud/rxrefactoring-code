package de.tudarmstadt.rxrefactoring.core;


import org.eclipse.core.runtime.IPath;

import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;

public interface RefactorEnvironment {

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
	 * Returns a set of all workers that should be executed for this
	 * refactoring.
	 * 
	 * @return A non-null set of workers that should be used
	 * for refactoring.
	 */
	public void buildWorkers(WorkerTree workerTree);
	
	/**
	 * Specifies which jars should be included to the refactored project.
	 * 
	 * @return Path with all jars, or null if no jars should be included.
	 */
	public default IPath getResourceDir() {
		return null;
	}

	/**
	 * Specifies where the jars should be included to the refactored project.
	 * 
	 * @return Path where the jars should be included relative to the project root.
	 */
	public default IPath getDestinationDir() {
		return null; //new Path("./libs");
	}
}
