package de.tudarmstadt.rxrefactoring.core.dependencies;

import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;

public abstract class CursorAnalysis {
	
	public Set<IRewriteCompilationUnit> units;
	public Integer offset;
	public Integer startLine;
	
	public abstract Set<IRewriteCompilationUnit> searchOccurence() throws JavaModelException;
	

}
