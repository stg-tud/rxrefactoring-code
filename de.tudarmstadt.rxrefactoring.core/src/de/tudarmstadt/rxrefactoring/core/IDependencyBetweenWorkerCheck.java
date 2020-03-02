package de.tudarmstadt.rxrefactoring.core;

import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;

public interface IDependencyBetweenWorkerCheck {
	
	public ProjectUnits regroupBecauseOfMethodDependencies();

}
