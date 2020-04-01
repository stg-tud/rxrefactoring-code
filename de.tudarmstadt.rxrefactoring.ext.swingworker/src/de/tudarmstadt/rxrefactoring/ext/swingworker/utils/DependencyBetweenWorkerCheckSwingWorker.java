package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.DependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;

public class DependencyBetweenWorkerCheckSwingWorker extends DependencyBetweenWorkerCheck {

	public ProjectUnits units;
	MethodScanner scanner;
	Integer offset;
	Integer length;

	public DependencyBetweenWorkerCheckSwingWorker(ProjectUnits units, MethodScanner scanner, int offset,
			int length) {
		this.scanner = scanner;
		this.units = units;
		this.offset = offset;
		this.length = length;

	}

	public ProjectUnits regroupBecauseOfMethodDependencies() {
		scanner.scan(units);

		if (offset == null && length == null) {
			for (Entry<MethodDeclaration, IRewriteCompilationUnit> entry : scanner.refactoredMethods.entrySet()) {

				if (checkIfReturnTypeIsSwingWorkerOrExtendsFromIt(entry.getKey())) {
					changeDependentUnitsOfChangedMethodDeclaration(entry);
				}

			}
		}else {
			
			searchForCursorVarDecl();
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

	public ProjectUnits searchForFieldDependencies() throws JavaModelException {

		// Change simpleNames which are fields
		Map<SimpleName, IRewriteCompilationUnit> simpleNames = changeSimpleNamesFields();

		// Change also corresponding FieldDeclaration
		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (unit.getWorkerIdentifier().getName().equals("Field Declarations")) {
				Collection<FieldDeclaration> actFieldDecls = WorkerUtils.getFieldDeclMap().get(unit);
				for (FieldDeclaration fieldDecl : actFieldDecls) {
					VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDecl.fragments()
							.get(0);
					String identifier = varDeclFrag.getName().getIdentifier();

					if (simpleNames.keySet().stream().anyMatch(x -> x.getIdentifier().equals(identifier))
							&& unit.getCorrespondingResource()
									.equals(simpleNames.get(varDeclFrag.getName()).getCorrespondingResource())) {
						unit.setWorkerIdentifier(
								new WorkerIdentifier(unit.getWorkerIdentifier().getName() + " " + identifier));
					}
				}
			}
		}
		return units;

	}

	private Map<SimpleName, IRewriteCompilationUnit> changeSimpleNamesFields() {

		Map<SimpleName, IRewriteCompilationUnit> simpleNames = Maps.newHashMap();
		for (IRewriteCompilationUnit unit : units.getUnits()) {

			if (unit.getWorkerIdentifier().getName().equals("Simple Names")) {

				Collection<SimpleName> actSimpleNames = WorkerUtils.getSimpleNamesMap().get(unit);
				for (SimpleName name : actSimpleNames) {

					IBinding binding = name.resolveBinding();
					boolean isField = false;
					if (binding.getKind() == IBinding.VARIABLE) {
						IVariableBinding curr = (IVariableBinding) binding;
						isField = curr.isField();
					}

					if (isField) {
						simpleNames.put(name, unit);
						unit.setWorkerIdentifier(new WorkerIdentifier("Field Declarations " + name.getIdentifier()));
					}
				}

			}
		}

		return simpleNames;

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
	
	private String[] resolveCursorPosition(IRewriteCompilationUnit unit){
		ICompilationUnit compUnit = (ICompilationUnit) unit;
		IJavaElement elemMethod = null;
		IJavaElement[] elemText = null;
		try {
			elemMethod = compUnit.getElementAt(offset);
			elemText = compUnit.codeSelect(offset, length);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String nameElemMethod = elemMethod.getElementName();
		String nameElemText = elemText[0].getElementName();
			
		return new String[] {nameElemMethod, nameElemText};
		
	}

}
