package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.CursorAnalysis;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;

public class CursorRefactorOccurenceSearcher extends CursorAnalysis {

	ProjectUnits units;
	Integer offset;
	Integer startLine;

	public CursorRefactorOccurenceSearcher(ProjectUnits units, int offset, int startLine) {
		this.units = units;
		this.offset = offset;
		this.startLine = startLine;
	}

	public ProjectUnits searchOccurence() {
		searchForCursorVarDecl();

		return units;
	}

	private void searchForCursorVarDecl() {
		Set<IRewriteCompilationUnit> units_VarDecls = units.stream()
				.filter(unit -> unit.getWorkerIdentifier().getName().equals("Variable Declarations"))
				.collect(Collectors.toSet());

		for (IRewriteCompilationUnit unit : units_VarDecls) {
			Collection<VariableDeclarationStatement> varDecls = WorkerUtils.getVarDeclMap().get(unit);
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
					unit.setWorkerIdentifier(new WorkerIdentifier("Cursor Method"));
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
