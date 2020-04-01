package de.tudarmstadt.rxrefactoring.ext.swingworker;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.DependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.DependencyBetweenWorkerCheckSwingWorker;

public class DependencyCheckSwingWorker {
	
	ProjectUnits units;
	MethodScanner scanner;
	Integer offset;
	Integer length;
	
	DependencyCheckSwingWorker(ProjectUnits units, MethodScanner methodScanner, int offset, int length){
		this.units = units;
		this.scanner = methodScanner;
		this.offset = offset;
		this.length = length;
	}
	
	public ProjectUnits runDependendencyCheck() throws JavaModelException {
		
		DependencyBetweenWorkerCheckSwingWorker checker = new DependencyBetweenWorkerCheckSwingWorker(units, scanner, offset, length);
		checker.regroupBecauseOfMethodDependencies();
		checker.searchForFieldDependencies();
		
		return checker.units;
	}


}
