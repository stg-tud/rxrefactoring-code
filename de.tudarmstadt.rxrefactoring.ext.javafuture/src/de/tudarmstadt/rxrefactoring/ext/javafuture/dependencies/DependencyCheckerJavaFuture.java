package de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.dependencies.DependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

public class DependencyCheckerJavaFuture extends DependencyBetweenWorkerCheck {

	public ProjectUnits units;
	MethodScanner scanner;
	private FutureCollector collector;
	private CollectorGroup group = new CollectorGroup();
	Map<MethodDeclaration, IRewriteCompilationUnit> entriesRefactored;
	Map<MethodDeclaration, IRewriteCompilationUnit> entriesCalling;

	public DependencyCheckerJavaFuture(ProjectUnits units, MethodScanner scanner, FutureCollector collector) {
		this.scanner = scanner;
		this.units = units;
		this.collector = collector;

	}

	public ProjectUnits runDependendencyCheck(boolean onlyVarDecl) throws JavaModelException {
		scanner.scan(units);

		for (Entry<String, CollectorGroup> entry : collector.groups.entrySet()) {
			group.addElementsCollectorGroup(entry.getValue());

		}

		if (onlyVarDecl) {
			DependencyCheckVariableDecl dependencyCheckVarDecl = new DependencyCheckVariableDecl(units, group);
			units = dependencyCheckVarDecl.checkVariableDeclarationsWithInMethod("Cursor Selection");
			return units;
		} else {
			DependencyCheckVariableDecl dependencyCheckVarDecl = new DependencyCheckVariableDecl(units, group);
			units = dependencyCheckVarDecl.checkVariableDeclarationsWithInMethod(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER.name);
			DependencyCheckMethodDecl dependencyCheckMethodDecl = new DependencyCheckMethodDecl(units, scanner);
			units = dependencyCheckMethodDecl.regroupBecauseOfMethodDependencies();
			DependencyCheckFieldDecl dependencyCheckFieldDecl = new DependencyCheckFieldDecl(units, group);
			units = dependencyCheckFieldDecl.searchForFieldDependencies();
		}

		return units;
	}

}
