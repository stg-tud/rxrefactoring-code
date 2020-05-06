package de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies;

import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.dependencies.DependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;

public class DependencyCheckerSwingWorker extends DependencyBetweenWorkerCheck {

	public ProjectUnits units;
	private MethodScanner scanner;
	private int startLine;


	public DependencyCheckerSwingWorker(ProjectUnits units, MethodScanner scanner, int startLine) {
		this.scanner = scanner;
		this.units = units;
		this.startLine = startLine;

	}
	
	public ProjectUnits runDependendencyCheck(boolean onlyVarDecl) throws JavaModelException {
		
		if (onlyVarDecl) {
			DependencyCheckVarDecl dependencyCheckVarDecl = new DependencyCheckVarDecl(units);
			units = dependencyCheckVarDecl.checkVariableDeclarationsWithInMethod("Cursor Selection");
			DependencyCheckMethodDecl dependencyCheckMethodDecl = new DependencyCheckMethodDecl(units, scanner, startLine);
			units = dependencyCheckMethodDecl.regroupBecauseOfMethodDependencies("Cursor Selection");
			return units;
		} else {
			DependencyCheckVarDecl dependencyCheckVarDecl = new DependencyCheckVarDecl(units);
			units = dependencyCheckVarDecl.checkVariableDeclarationsWithInMethod(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER.getName());
			DependencyCheckMethodDecl dependencyCheckMethodDecl = new DependencyCheckMethodDecl(units, scanner, -1);
			units = dependencyCheckMethodDecl.regroupBecauseOfMethodDependencies(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER.getName());
			DependencyCheckFieldDecl dependencyCheckFieldDecl = new DependencyCheckFieldDecl(units);
			units = dependencyCheckFieldDecl.searchForFieldDependencies();
		}

		return units;
	}
			
}
