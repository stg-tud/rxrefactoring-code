package de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies;

import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.dependencies.DependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;

public class DependencyCheckerSwingWorker extends DependencyBetweenWorkerCheck {

	public ProjectUnits units;
	private MethodScanner scanner;


	public DependencyCheckerSwingWorker(ProjectUnits units, MethodScanner scanner) {
		this.scanner = scanner;
		this.units = units;

	}
	
	public ProjectUnits runDependendencyCheck(boolean onlyVarDecl) throws JavaModelException {
		
		if (onlyVarDecl) {
			DependencyCheckVarDecl dependencyCheckVarDecl = new DependencyCheckVarDecl(units);
			units = dependencyCheckVarDecl.checkVariableDeclarationsWithInMethod("Cursor Selection");
			return units;
		} else {
			DependencyCheckVarDecl dependencyCheckVarDecl = new DependencyCheckVarDecl(units);
			units = dependencyCheckVarDecl.checkVariableDeclarationsWithInMethod(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER.name);
			DependencyCheckMethodDecl dependencyCheckMethodDecl = new DependencyCheckMethodDecl(units, scanner);
			units = dependencyCheckMethodDecl.regroupBecauseOfMethodDependencies();
			DependencyCheckFieldDecl dependencyCheckFieldDecl = new DependencyCheckFieldDecl(units);
			units = dependencyCheckFieldDecl.searchForFieldDependencies();
		}

		return units;
	}
			
}
