package de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.dependencies.CursorAnalysis;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

public class CursorRefactorOccurenceSearcher extends CursorAnalysis {

	ProjectUnits units;
	Integer offset;
	Integer startLine;
	FutureCollector collector;
	private CollectorGroup group = new CollectorGroup();

	public CursorRefactorOccurenceSearcher(ProjectUnits units, int offset, int startLine, FutureCollector collector) {
		this.units = units;
		this.offset = offset;
		this.startLine = startLine;
		this.collector = collector;
	}

	public ProjectUnits searchOccurence() {
		
		for (Entry<String, CollectorGroup> entry : collector.groups.entrySet()) {
			group.addElementsCollectorGroup(entry.getValue());

		}
		
		searchForCursorVarDecl();

		return units;
	}

	private void searchForCursorVarDecl() {
		Set<IRewriteCompilationUnit> units_VarDecls = units.stream()
				.filter(unit -> unit.getWorkerIdentifier().getName().equals("Variable Declarations"))
				.collect(Collectors.toSet());

		for (IRewriteCompilationUnit unit : units_VarDecls) {
			Collection<VariableDeclarationStatement> varDecls = group.getVarDeclMap().get(unit);
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

		return  nameElemText;

	}

}
