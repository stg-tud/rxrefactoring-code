package de.tudarmstadt.rxrefactoring.core.handler;


import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.tudarmstadt.rxrefactoring.core.workers.IWorker;

public interface RefactorEnvironment {

	
	public Set<IWorker> workers();
	
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
