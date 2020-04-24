package de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.dependencies.CursorAnalysis;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.WorkerUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

public class CursorRefactorOccurenceSearcher extends CursorAnalysis {

	ProjectUnits units;
	Integer offset;
	Integer startLine;
	FutureCollector collector;
	private CollectorGroup group;

	public CursorRefactorOccurenceSearcher(ProjectUnits units, int offset, int startLine, FutureCollector collector) {
		this.units = units;
		this.offset = offset;
		this.startLine = startLine;
		this.collector = collector;
	}

	public ProjectUnits searchOccurence() {
		for (Entry<String, CollectorGroup> groupEntry : collector.groups.entrySet()) {
			group = groupEntry.getValue();
			searchForCursorVarDecl();
		}

		return units;
	}

	private void searchForCursorVarDecl() {
		Set<IRewriteCompilationUnit> units_VarDecls = units.stream()
				.filter(unit -> unit.getWorkerIdentifier().getName().equals("Variable Declarations"))
				.collect(Collectors.toSet());

		for (IRewriteCompilationUnit unit : units_VarDecls) {
			Collection<VariableDeclarationStatement> varDecls = group.getVarDeclMap().get(unit);
			for (VariableDeclarationStatement statement : varDecls) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
				String name = fragment.getName().getIdentifier();
				Optional<MethodDeclaration> methodDeclaration = ASTNodes.findParent(statement, MethodDeclaration.class);
				String methodNameUnit = "";
				if (methodDeclaration.isPresent()) {
					methodNameUnit = methodDeclaration.get().getName().getIdentifier();
				}
				String varName = resolveCursorPosition(unit)[1];
				String methodName = resolveCursorPosition(unit)[0];
				if (varName.contains(name) && methodNameUnit.equals(methodName))
					unit.setWorkerIdentifier(new WorkerIdentifier("Cursor Selection"));
			}

		}
	}

	private String[] resolveCursorPosition(IRewriteCompilationUnit unit) {
		IJavaElement elemMethod = null;
		String nameElemText = null;
		try {
			elemMethod = unit.getElementAt(offset);
			String text = unit.getSource();
			String[] lines = text.split("\n");
			nameElemText = lines[startLine];
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String nameElemMethod = elemMethod.getElementName();

		return new String[] { nameElemMethod, nameElemText };

	}
}
