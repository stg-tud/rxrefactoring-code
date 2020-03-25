package de.tudarmstadt.rxrefactoring.core;

import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;

public abstract class DependencyBetweenWorkerCheck {
	
	protected MethodScanner scanner;
	public ProjectUnits units;
	
	public void DepenedencyBetweenWorkerCheck(ProjectUnits units, MethodScanner scanner) {
		this.scanner = scanner;
		this.units = units;
	}
	
	public abstract ProjectUnits regroupBecauseOfMethodDependencies();
	public abstract ProjectUnits searchForFieldDependencies()  throws JavaModelException;

}
