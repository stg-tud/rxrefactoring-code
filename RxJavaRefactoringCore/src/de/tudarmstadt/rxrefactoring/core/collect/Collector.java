package de.tudarmstadt.rxrefactoring.core.collect;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

public interface Collector {
		
	public String getName();
	
	public void processCompilationUnit(ICompilationUnit unit);
}
