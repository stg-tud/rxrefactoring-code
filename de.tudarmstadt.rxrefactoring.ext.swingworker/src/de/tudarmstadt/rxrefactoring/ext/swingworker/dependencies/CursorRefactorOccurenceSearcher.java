package de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.dependencies.CursorAnalysis;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerUtils;

public class CursorRefactorOccurenceSearcher extends CursorAnalysis {

	ProjectUnits units;
	Integer startLine;

	public CursorRefactorOccurenceSearcher(ProjectUnits units, int startLine) {
		this.units = units;
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
				Optional<CompilationUnit> compUnit = ASTNodes.findParent(statement, CompilationUnit.class);
				int lineNumber = compUnit.get().getLineNumber(statement.getStartPosition()) - 1;
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
				String name = fragment.getName().getIdentifier();
			
				String varName = resolveCursorPosition(unit);
				if (varName.contains(name) && lineNumber == startLine)
					unit.setWorkerIdentifier(new WorkerIdentifier("Cursor Selection"));
			}

		}
	}

	private String resolveCursorPosition(IRewriteCompilationUnit unit) {
		String nameElemText = null;
		try {
			String text = unit.getSource();
			String[] lines = text.split("\n");
			nameElemText = lines[startLine];
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nameElemText;

	}
}
