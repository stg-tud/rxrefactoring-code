package de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerUtils;

public class DependencyCheckMethodDecl {
	
	ProjectUnits units;
	MethodScanner scanner;
	
	public DependencyCheckMethodDecl(ProjectUnits units, MethodScanner scanner) {
		this.scanner = scanner;
		this.units = units;
		
	}
	
	protected ProjectUnits regroupBecauseOfMethodDependencies() {
		scanner.scan(units);

			for (Entry<MethodDeclaration, IRewriteCompilationUnit> entry : scanner.refactoredMethods.entrySet()) {

				if (checkIfReturnTypeIsSwingWorkerOrExtendsFromIt(entry.getKey())) {
					changeDependentUnitsOfChangedMethodDeclaration(entry);
				}

			}

		scanner.clearMaps();

		return units;

	}

	private boolean checkIfReturnTypeIsSwingWorkerOrExtendsFromIt(MethodDeclaration method) {
		Type returnType = method.getReturnType2();
		ITypeBinding binding = returnType.resolveBinding();
		if (Types.isTypeOf(binding, "javax.swing.SwingWorker"))
			return true;

		return false;
	}
	





	private void changeDependentUnitsOfChangedMethodDeclaration(
			Entry<MethodDeclaration, IRewriteCompilationUnit> entry) {

		String methodName = entry.getKey().getName().getIdentifier();

		// check if we have right unit
		if (entry.getValue().getWorkerIdentifier().getName() != "Method Declarations") {
			entry.setValue(searchForMethodDeclUnit(entry.getKey()));
		}

		// first change methodDeclUnit
		Optional<IRewriteCompilationUnit> unit_methodDecl = units.getUnits().stream()
				.filter(unit -> unit.equals(entry.getValue())).findFirst();
		if (unit_methodDecl.isPresent()) {
			units.getUnits().stream().filter(unit -> unit.equals(unit_methodDecl.get())).forEach(unit -> unit
					.setWorkerIdentifier(new WorkerIdentifier("Change of MethodDeclaration: " + methodName)));
		}

		// second change of methodInvocations result in different VaraiableDeclarations
		Set<IRewriteCompilationUnit> units_VariableDecl = getVariableDeclarationsTo(entry.getKey(), entry.getValue());
		if (!units_VariableDecl.isEmpty()) {
			for (IRewriteCompilationUnit unit_act : units_VariableDecl) {
				units.getUnits().stream().filter(unit -> unit.equals(unit_act)).forEach(unit -> unit
						.setWorkerIdentifier(new WorkerIdentifier("Change of MethodDeclaration: " + methodName)));
			}
		}

		// third change class instance creation in returnType
		Set<IRewriteCompilationUnit> unit_classInstances = getClassInstanceCreationUnit(entry.getKey());
		if (!unit_classInstances.isEmpty()) {
			for (IRewriteCompilationUnit unit_act : unit_classInstances) {
				units.getUnits().stream().filter(unit -> unit.equals(unit_act)).forEach(unit -> unit
						.setWorkerIdentifier(new WorkerIdentifier("Change of MethodDeclaration: " + methodName)));
			}
		}

		// fourth change of parameters in MethodDeclaration
		List<ITypeBinding> listParameters = Arrays.asList(entry.getKey().resolveBinding().getParameterTypes());
		boolean swingWorkerInParams = listParameters.stream()
				.anyMatch(param -> Types.isTypeOf(param, "javax.swing.SwingWorker"));
		if (swingWorkerInParams) {
			// check for SingleVariableDeclarations
			Set<IRewriteCompilationUnit> unit_singleVarDecl = getSingleVariableDeclationsForMethodParams(
					entry.getKey());
			if (!unit_singleVarDecl.isEmpty()) {
				for (IRewriteCompilationUnit unit_act : unit_singleVarDecl) {
					units.getUnits().stream().filter(unit -> unit.equals(unit_act)).forEach(unit -> unit
							.setWorkerIdentifier(new WorkerIdentifier("Change of MethodDeclaration: " + methodName)));
				}
			}

		}

	}

	
	private Set<IRewriteCompilationUnit> getVariableDeclarationsTo(MethodDeclaration methodDecl,
			IRewriteCompilationUnit unit_MethodDecl) {
		Set<IRewriteCompilationUnit> unitsToChange = new HashSet<IRewriteCompilationUnit>();

		Set<IRewriteCompilationUnit> units_MethodInvoc = units.stream()
				.filter(unit -> unit.getWorkerIdentifier().getName().equals("Variable Declarations"))
				.filter(unit -> unit.getResource().equals(unit_MethodDecl.getResource())).collect(Collectors.toSet());

		for (IRewriteCompilationUnit unit_Var : units_MethodInvoc) {
			Collection<VariableDeclarationStatement> varDecls = WorkerUtils.getVarDeclMap().get(unit_Var);

			for (VariableDeclarationStatement st : varDecls) {

				VariableDeclarationFragment fragment = (VariableDeclarationFragment) st.fragments().get(0);
				Expression initializer = fragment.getInitializer();

				if (initializer instanceof MethodInvocation) {
					if (((MethodInvocation) initializer).resolveMethodBinding().equals(methodDecl.resolveBinding())) {
						unitsToChange.add(unit_Var);
					}
				}
			}
		}

		return unitsToChange;

	}

	@SuppressWarnings("unchecked")
	private Set<IRewriteCompilationUnit> getSingleVariableDeclationsForMethodParams(MethodDeclaration methodDecl) {
		Set<IRewriteCompilationUnit> unitsToChange = new HashSet<IRewriteCompilationUnit>();
		List<SingleVariableDeclaration> listParams = (List<SingleVariableDeclaration>) methodDecl.parameters();
		listParams.stream().filter(param -> Types.isTypeOf(param.resolveBinding().getType(), "javax.swing.SwingWorker"))
				.collect(Collectors.toList());

		for (SingleVariableDeclaration decl : listParams) {
			Optional<Entry<IRewriteCompilationUnit, SingleVariableDeclaration>> matchedEntry = WorkerUtils
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
						&& Types.isTypeOf(binding, "javax.swing.SwingWorker")) {
					ClassInstanceCreation classInstance = (ClassInstanceCreation) rStatement.getExpression();

					Optional<Entry<IRewriteCompilationUnit, ClassInstanceCreation>> matchedEntry = WorkerUtils
							.getClassInstanceMap().entries().stream().filter(e -> e.getValue().equals(classInstance))
							.findFirst();
					if (matchedEntry.isPresent())
						unitsToChange.add(matchedEntry.get().getKey());
				}
			}
			if (st instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement varDeclStatement = (VariableDeclarationStatement) st;
				if (Types.isTypeOf(varDeclStatement.getType().resolveBinding(), "javax.swing.SwingWorker")) {
					Optional<Entry<IRewriteCompilationUnit, VariableDeclarationStatement>> matchedEntryVar = WorkerUtils
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
		Optional<Entry<IRewriteCompilationUnit, MethodDeclaration>> matchedEntry = WorkerUtils
				.getMethodDeclarationsMap().entries().stream().filter(e -> e.getValue().equals(methodDecl)).findFirst();

		return matchedEntry.get().getKey();
	}

}
