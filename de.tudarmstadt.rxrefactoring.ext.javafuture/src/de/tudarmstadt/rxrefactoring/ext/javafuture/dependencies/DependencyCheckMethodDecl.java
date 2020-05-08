package de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;

public class DependencyCheckMethodDecl {

	ProjectUnits units;
	MethodScanner scanner;
	private CollectorGroup group = new CollectorGroup();
	private String classBinary = "java.util.concurrent.Future";
	String nameWorker = "";
	String varName = "";
	int startLine;

	public DependencyCheckMethodDecl(ProjectUnits units, MethodScanner scanner, int startLine) {
		this.scanner = scanner;
		this.units = units;
		this.startLine = startLine;

	}

	protected ProjectUnits regroupBecauseOfMethodDependencies(String name) throws JavaModelException {
		nameWorker = name;
		scanner.scan(units);

		for (Entry<MethodDeclaration, IRewriteCompilationUnit> entry : scanner.refactoredMethods.entrySet()) {

			// for method declaration change
			if (checkIfReturnTypeIsFutureOrExtendsFromIt(entry.getKey())) {
				changeDependentUnitsOfChangedMethodDeclaration(entry);
			}
		}

		scanner.clearMaps();

		return units;

	}

	private boolean checkCursorSelection(VariableDeclarationStatement varDecl) {
		Optional<CompilationUnit> compUnit = ASTNodes.findParent(varDecl, CompilationUnit.class);
		int lineNumber = compUnit.get().getLineNumber(varDecl.getStartPosition()) - 1;

		if (startLine == -1)
			return false;

		return lineNumber == startLine;
	}

	private boolean checkIfReturnTypeIsFutureOrExtendsFromIt(MethodDeclaration method) {
		Type returnType = method.getReturnType2();
		ITypeBinding binding = returnType.resolveBinding();
		if (Types.isTypeOf(binding, classBinary))
			return true;

		return false;
	}

	private void changeDependentUnitsOfChangedMethodDeclaration(
			Entry<MethodDeclaration, IRewriteCompilationUnit> entry) {

		if (nameWorker.equals("Cursor Selection")) {
			boolean shouldBeChecked = setVariableNameAndCheckCursorSelection(entry.getKey());
			if (!shouldBeChecked)
				return;
		} else {
			varName = entry.getKey().getName().getIdentifier();
		}

		// check if we have right unit
		if (!entry.getValue().getWorkerIdentifier().getName().equals("Method Declarations")) {
			entry.setValue(searchForMethodDeclUnit(entry.getKey()));
		}

		// first change of methodInvocations result in different VaraiableDeclarations
		Map<IRewriteCompilationUnit, String> units_VariableDecl = getVariableDeclarationsTo(entry.getKey(),
				entry.getValue());
		if (!units_VariableDecl.isEmpty()) {
			for (IRewriteCompilationUnit key : units_VariableDecl.keySet()) {
				units.getUnits().stream().filter(unit -> unit.equals(key)).forEach(
						unit -> unit.setWorkerIdentifier(new WorkerIdentifier(units_VariableDecl.get(key) + varName)));
			}
		}

		// second change methodDeclUnit
		Optional<IRewriteCompilationUnit> unit_methodDecl = units.getUnits().stream()
				.filter(unit -> unit.equals(entry.getValue())).findFirst();
		if (unit_methodDecl.isPresent()) {
			units.getUnits().stream().filter(unit -> unit.equals(unit_methodDecl.get()))
					.forEach(unit -> unit.setWorkerIdentifier(new WorkerIdentifier(namingHelper(false) + varName)));
		}

		// third change class instance creation in returnType
		Set<IRewriteCompilationUnit> unit_classInstances = getClassInstanceCreationUnit(entry.getKey());
		if (!unit_classInstances.isEmpty()) {
			for (IRewriteCompilationUnit unit_act : unit_classInstances) {
				units.getUnits().stream().filter(unit -> unit.equals(unit_act))
						.forEach(unit -> unit.setWorkerIdentifier(new WorkerIdentifier(namingHelper(false) + varName)));
			}
		}

		// fourth change of parameters in MethodDeclaration
		List<ITypeBinding> listParameters = Arrays.asList(entry.getKey().resolveBinding().getParameterTypes());
		boolean swingWorkerInParams = listParameters.stream().anyMatch(param -> Types.isTypeOf(param, classBinary));
		if (swingWorkerInParams) {
			// check for SingleVariableDeclarations
			Set<IRewriteCompilationUnit> unit_singleVarDecl = getSingleVariableDeclationsForMethodParams(
					entry.getKey());
			if (!unit_singleVarDecl.isEmpty()) {
				for (IRewriteCompilationUnit unit_act : unit_singleVarDecl) {
					units.getUnits().stream().filter(unit -> unit.equals(unit_act)).forEach(
							unit -> unit.setWorkerIdentifier(new WorkerIdentifier(namingHelper(false) + varName)));
				}
			}

		}

	}

	private Map<IRewriteCompilationUnit, String> getVariableDeclarationsTo(MethodDeclaration methodDecl,
			IRewriteCompilationUnit unit_MethodDecl) {
		Map<IRewriteCompilationUnit, String> unitsToChange = new HashMap<IRewriteCompilationUnit, String>();

		Set<IRewriteCompilationUnit> units_MethodInvoc = units.stream()
				.filter(unit -> checkWorkerIdentifier(unit.getWorkerIdentifier()))
				.filter(unit -> unit.getResource().equals(unit_MethodDecl.getResource())).collect(Collectors.toSet());

		for (IRewriteCompilationUnit unit_Var : units_MethodInvoc) {
			Collection<VariableDeclarationStatement> varDecls = group.getVarDeclMap().get(unit_Var);

			for (VariableDeclarationStatement st : varDecls) {

				VariableDeclarationFragment fragment = (VariableDeclarationFragment) st.fragments().get(0);
				Expression initializer = fragment.getInitializer();

				if (initializer instanceof MethodInvocation) {
					if (((MethodInvocation) initializer).resolveMethodBinding().equals(methodDecl.resolveBinding())) {
						if (checkCursorSelection(st)) {
							unitsToChange.put(unit_Var, namingHelper(true));
						} else {
							unitsToChange.put(unit_Var, namingHelper(false));
							unitsToChange.putAll(checkForSimpleNameChanges(unit_Var));

						}
					}
				}
			}
		}

		return unitsToChange;

	}

	private Map<IRewriteCompilationUnit, String> checkForSimpleNameChanges(IRewriteCompilationUnit unitVarDecl) {
		return units.getUnits().stream()
				.filter(unit -> unitVarDecl.getWorkerIdentifier().name.equals(unit.getWorkerIdentifier().name)
						&& !unit.equals(unitVarDecl))
				.collect(Collectors.toMap(key -> key, value -> namingHelper(false)));

	}

	@SuppressWarnings("unchecked")
	private Set<IRewriteCompilationUnit> getSingleVariableDeclationsForMethodParams(MethodDeclaration methodDecl) {
		Set<IRewriteCompilationUnit> unitsToChange = new HashSet<IRewriteCompilationUnit>();
		List<SingleVariableDeclaration> listParams = (List<SingleVariableDeclaration>) methodDecl.parameters();
		listParams.stream().filter(param -> Types.isTypeOf(param.resolveBinding().getType(), classBinary))
				.collect(Collectors.toList());

		for (SingleVariableDeclaration decl : listParams) {
			Optional<Entry<IRewriteCompilationUnit, SingleVariableDeclaration>> matchedEntry = group
					.getSingleVarDeclMap().entries().stream().filter(e -> e.getValue().equals(decl)).findFirst();
			if (matchedEntry.isPresent())
				unitsToChange.add(matchedEntry.get().getKey());

		}

		return unitsToChange;
	}

	private Set<IRewriteCompilationUnit> getClassInstanceCreationUnit(MethodDeclaration methodDecl) {
		Set<IRewriteCompilationUnit> unitsToChange = new HashSet<IRewriteCompilationUnit>();
		ITypeBinding binding = methodDecl.getReturnType2().resolveBinding();
		Block block = methodDecl.getBody();

		for (Object st : block.statements()) {
			if (st instanceof ReturnStatement) {
				ReturnStatement rStatement = (ReturnStatement) st;
				if (rStatement.getExpression() instanceof ClassInstanceCreation
						&& Types.isTypeOf(binding, classBinary)) {
					ClassInstanceCreation classInstance = (ClassInstanceCreation) rStatement.getExpression();

					Optional<Entry<IRewriteCompilationUnit, ClassInstanceCreation>> matchedEntry = group
							.getClassInstanceMap().entries().stream().filter(e -> e.getValue().equals(classInstance))
							.findFirst();
					if (matchedEntry.isPresent())
						unitsToChange.add(matchedEntry.get().getKey());
				}
			}
			if (st instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement varDeclStatement = (VariableDeclarationStatement) st;
				if (Types.isTypeOf(varDeclStatement.getType().resolveBinding(), classBinary)) {
					Optional<Entry<IRewriteCompilationUnit, VariableDeclarationStatement>> matchedEntryVar = group
							.getVarDeclMap().entries().stream().filter(e -> e.getValue().equals(varDeclStatement))
							.findFirst();
					if (matchedEntryVar.isPresent())
						unitsToChange.add(matchedEntryVar.get().getKey());
				}
			}
		}

		return unitsToChange;
	}

	private IRewriteCompilationUnit searchForMethodDeclUnit(MethodDeclaration methodDecl) {
		Optional<Entry<IRewriteCompilationUnit, MethodDeclaration>> matchedEntry = group.getMethodDeclarationsMap()
				.entries().stream().filter(e -> e.getValue().equals(methodDecl)).findFirst();

		if (matchedEntry.isPresent())
			return matchedEntry.get().getKey();
		return null;
	}

	private String namingHelper(boolean isMainVarChange) {
		if (nameWorker.equals("Cursor Selection")) {
			if (isMainVarChange)
				return "Cursor Selection Variable: ";
			else {
				return "Changes also needed for Cursor Selection of Variable: ";
			}
		} else {
			return "Change of MethodDeclaration: ";
		}
	}

	private boolean setVariableNameAndCheckCursorSelection(MethodDeclaration methodDecl) {
		Set<IRewriteCompilationUnit> unitCursors = units.getUnits().stream()
				.filter(unit -> unit.getWorkerIdentifier().name.equals("Cursor Selection")
						|| unit.getWorkerIdentifier().name.contains("Cursor Selection Variable: "))
				.collect(Collectors.toSet());
		for (IRewriteCompilationUnit unitCursor : unitCursors) {
			Collection<VariableDeclarationStatement> statements = group.getVarDeclMap().get(unitCursor);
			for (VariableDeclarationStatement st : statements) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) st.fragments().get(0);
				Expression expr = fragment.getInitializer();
				if (expr instanceof MethodInvocation) {
					if (((MethodInvocation) expr).resolveMethodBinding().equals(methodDecl.resolveBinding())) {
						varName = fragment.getName().getIdentifier();
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean checkWorkerIdentifier(WorkerIdentifier identifier) {

		if (nameWorker.equals("Cursor Selection")) {
			return identifier.getName().equals(nameWorker)
					|| identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
		} else if (nameWorker.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER.getName())) {
			return identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER)
					|| (identifier.getName().contains("Variable") && !identifier.getName().contains("Single"));
		}
		return identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
	}

}
