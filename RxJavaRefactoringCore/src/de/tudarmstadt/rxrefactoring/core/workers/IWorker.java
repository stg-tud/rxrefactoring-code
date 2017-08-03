package de.tudarmstadt.rxrefactoring.core.workers;

import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;

public interface IWorker {

	public void refactor(ProjectUnits units) throws Exception;
	
}
