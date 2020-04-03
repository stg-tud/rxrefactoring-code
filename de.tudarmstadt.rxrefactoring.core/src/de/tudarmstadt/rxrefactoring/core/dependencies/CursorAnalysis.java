package de.tudarmstadt.rxrefactoring.core.dependencies;

import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;

public abstract class CursorAnalysis {
	
	public ProjectUnits units;
	public Integer offset;
	public Integer startLine;
	
	public abstract ProjectUnits searchOccurence() throws JavaModelException;
	

}
