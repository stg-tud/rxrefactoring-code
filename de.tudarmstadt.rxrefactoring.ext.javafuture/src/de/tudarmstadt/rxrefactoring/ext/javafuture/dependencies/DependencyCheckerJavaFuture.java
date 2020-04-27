package de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.dependencies.DependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RefactorExecution;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.WorkerUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

public class DependencyCheckerJavaFuture extends DependencyBetweenWorkerCheck {

	public ProjectUnits units;
	MethodScanner scanner;
	private FutureCollector collector;
	private CollectorGroup group = new CollectorGroup();
	private String classBinary = "java.util.concurrent.Future";
	Map<MethodDeclaration, IRewriteCompilationUnit> entriesRefactored;
	Map<MethodDeclaration, IRewriteCompilationUnit> entriesCalling;
	Map<String, SingleVariableDeclaration> singleVarDeclsToCheck = new HashMap<>();
	int counterVar = 0;

	public DependencyCheckerJavaFuture(ProjectUnits units, MethodScanner scanner, FutureCollector collector) {
		this.scanner = scanner;
		this.units = units;
		this.collector = collector;

	}

	public ProjectUnits runDependendencyCheck() throws JavaModelException {
		scanner.scan(units);

		for (Entry<String, CollectorGroup> entry : collector.groups.entrySet()) {
			group.addElementsCollectorGroup(entry.getValue());

		}

		regroupBecauseOfMethodDependencies();
		searchForFieldDependencies();
		checkVariableDeclarationsWithInMethod();

		return units;
	}

	private ProjectUnits regroupBecauseOfMethodDependencies() throws JavaModelException {

		for (Entry<MethodDeclaration, IRewriteCompilationUnit> entry : scanner.refactoredMethods.entrySet()) {

			// for method declaration change
			if (checkIfReturnTypeIsFutureOrExtendsFromIt(entry.getKey())) {
				changeDependentUnitsOfChangedMethodDeclaration(entry);
			}

			// searchForVarDeclAndMethodInvocationsOfThat();

		}

		scanner.clearMaps();

		return units;

	}

	private boolean checkIfReturnTypeIsFutureOrExtendsFromIt(MethodDeclaration method) {
		Type returnType = method.getReturnType2();
		ITypeBinding binding = returnType.resolveBinding();
		if (Types.isTypeOf(binding, classBinary))
			return true;

		return false;
	}

	/*
	 * private void searchForVarDeclAndMethodInvocationsOfThat() throws
	 * JavaModelException {
	 * 
	 * sortAndFilterMaps();
	 * 
	 * for (Entry<MethodDeclaration, IRewriteCompilationUnit> entryRefactor :
	 * entriesRefactored.entrySet()) { // if //
	 * (entryRefactor.getValue().getWorkerIdentifier().equals(NamingUtils.
	 * METHOD_INVOCATION_IDENTIFIER)) // { Collection<MethodInvocation>
	 * methodInvocations = group.getMethodInvocationsMap()
	 * .get(entryRefactor.getValue()); for (MethodInvocation methodInv :
	 * methodInvocations) { Expression expr = methodInv.getExpression(); SimpleName
	 * varInvoking = null; if (expr instanceof SimpleName) { varInvoking =
	 * (SimpleName) expr; }
	 * 
	 * for (Entry<MethodDeclaration, IRewriteCompilationUnit> entryCalling :
	 * entriesCalling.entrySet()) { Collection<VariableDeclarationStatement>
	 * varDecls = group.getVarDeclMap() .get(entryCalling.getValue()); for
	 * (VariableDeclarationStatement varDecl : varDecls) {
	 * VariableDeclarationFragment fragment = (VariableDeclarationFragment)
	 * varDecl.fragments().get(0); SimpleName varName = fragment.getName();
	 * 
	 * if (varInvoking.getIdentifier().equals(varName.getIdentifier()) &&
	 * checkForSameMethodDeclaration(varDecl, methodInv)) {
	 * units.getUnits().stream().filter(x -> x.equals(entryCalling.getValue()))
	 * .forEach(x -> x.setWorkerIdentifier(new WorkerIdentifier(
	 * "Change according to Variable: " + varName.getIdentifier())));
	 * 
	 * units.getUnits().stream().filter(x -> x.equals(entryRefactor.getValue()))
	 * .forEach(x -> x.setWorkerIdentifier(new WorkerIdentifier(
	 * "Change according to Variable: " + varName.getIdentifier())));
	 * 
	 * } } } } } }
	 */

	private void checkVariableDeclarationsWithInMethod() {

		List<IRewriteCompilationUnit> filteredUnits = units.getUnits().stream()
				.filter(elem -> elem.getWorkerIdentifier() == NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER)
				.collect(Collectors.toList());
		for (IRewriteCompilationUnit unit : filteredUnits) {
			Collection<VariableDeclarationStatement> statements = group.getVarDeclMap().get(unit);
			
			counterVar++;

			for (VariableDeclarationStatement varDecl : statements) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
				SimpleName varName = fragment.getName();

				for (IRewriteCompilationUnit unitToCheck : units) {
					if (!unit.equals(unitToCheck)) {
						if (!checkUnitForSameResourceAndBlock(unit, varDecl, unitToCheck))
							continue;
						else if (!checkForSameVariable(varName.getIdentifier(), unitToCheck))
							continue;

						unitToCheck.setWorkerIdentifier(new WorkerIdentifier("Change #" + String.valueOf(counterVar)
								+ " according to Variable: " + varName.getIdentifier()));
						unit.setWorkerIdentifier(new WorkerIdentifier("Change #" + String.valueOf(counterVar)
								+ " according to Variable: " + varName.getIdentifier()));

					}
				}
			}

			for (Entry<String, SingleVariableDeclaration> singleVar : singleVarDeclsToCheck.entrySet()) {
				SimpleName varName = singleVar.getValue().getName();

				for (IRewriteCompilationUnit unitToCheck : units) {
					if (!checkUnitForSameResourceAndBlock(unit, singleVar.getValue(), unitToCheck))
						continue;
					if (!checkForSameVariable(varName.getIdentifier(), unitToCheck))
						continue;

					unitToCheck.setWorkerIdentifier(
							new WorkerIdentifier(singleVar.getKey()));
					unit.setWorkerIdentifier(
							new WorkerIdentifier(singleVar.getKey()));

				}
			}

		}
	}

	private boolean checkUnitForSameResourceAndBlock(IRewriteCompilationUnit varDecl, ASTNode statement,
			IRewriteCompilationUnit toCheckUnit) {

		boolean hasRighWorkerIdentifier = toCheckUnit.getWorkerIdentifier()
				.equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER)
				|| toCheckUnit.getWorkerIdentifier().equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER)
				|| toCheckUnit.getWorkerIdentifier().equals(NamingUtils.ASSIGNMENTS_IDENTIFIER)
				|| toCheckUnit.getWorkerIdentifier().equals(NamingUtils.SIMPLE_NAME_IDENTIFIER)
				|| toCheckUnit.getWorkerIdentifier().equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER);

		if (!hasRighWorkerIdentifier)
			return false;

		try {
			if (!(toCheckUnit.hasChanges()
					&& toCheckUnit.getCorrespondingResource().equals(varDecl.getCorrespondingResource()))) {
				return false;
			}
		} catch (JavaModelException e) {
			Log.error(DependencyCheckerJavaFuture.class,
					"There are problems with the java model.Two resources of units couldn't be compared, reason: "
							+ e.getMessage());
			e.printStackTrace();
		}

		if (!checkForSameStatement(statement, toCheckUnit))
			return false;

		return true;
	}

	private boolean checkForSameVariable(String varName, IRewriteCompilationUnit unit) {
		if (unit.getWorkerIdentifier().equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER)) {
			Collection<MethodInvocation> methodInvocs = group.getMethodInvocationsMap().get(unit);
			return methodInvocs.stream().map(methodInvoc -> {
				Expression expr = methodInvoc.getExpression();
				SimpleName varInvoking = null;
				if (expr instanceof SimpleName) {
					varInvoking = (SimpleName) expr;
				}
				return varInvoking.getIdentifier().equals(varName);
			}).anyMatch(bool -> bool == true);
		}

		if (unit.getWorkerIdentifier().equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER)) {
			Collection<SingleVariableDeclaration> singleVarDecls = group.getSingleVarDeclMap().get(unit);
			return singleVarDecls.stream().map(singleVarDecl -> {
				boolean res = handleSingleVarDeclInLoop(singleVarDecl, varName);
				if (res)
					singleVarDeclsToCheck.put(
							"Change #" + String.valueOf(counterVar) + " according to Variable: " + varName,
							singleVarDecl);
				return res;
			}).anyMatch(bool -> bool == true);
		}

		if (unit.getWorkerIdentifier().equals(NamingUtils.ASSIGNMENTS_IDENTIFIER)) {
			Collection<Assignment> assignments = group.getAssigmentsMap().get(unit);
			return assignments.stream().map(assignment -> {
				Expression expr = assignment.getRightHandSide();
				SimpleName name = null;
				if (expr instanceof SimpleName) {
					name = (SimpleName) expr;
					return name.getIdentifier().equals(varName);
				}
				return null;
			}).anyMatch(bool -> bool == true);
		}

		if (unit.getWorkerIdentifier().equals(NamingUtils.SIMPLE_NAME_IDENTIFIER)) {
			Collection<SimpleName> simpleNames = group.getSimpleNamesMap().get(unit);
			return simpleNames.stream().map(simpleName -> {
				return simpleName.getIdentifier().equals(varName);
			}).anyMatch(bool -> bool == true);
		}

		if (unit.getWorkerIdentifier().equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER)) {
			Collection<ClassInstanceCreation> classInstances = group.getClassInstanceMap().get(unit);
			return classInstances.stream().map(classInstance -> {
				VariableDeclarationStatement varDecl = ASTNodes
						.findParent(classInstance, VariableDeclarationStatement.class).get();
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
				SimpleName name = fragment.getName();
				return name.getIdentifier().equals(varName);
			}).anyMatch(bool -> bool == true);
		}

		return false;
	}

	private boolean handleSingleVarDeclInLoop(SingleVariableDeclaration singleVar, String toCheck) {
		Optional<EnhancedForStatement> statement = ASTNodes.findParent(singleVar, EnhancedForStatement.class);

		if (statement.isPresent()) {
			Expression exp = statement.get().getExpression();
			if (exp instanceof SimpleName) {
				SimpleName name = (SimpleName) exp;

				return name.getIdentifier().equals(toCheck);
			}

		}

		return false;

	}

	private boolean checkForMethodInvocOrVarDecl(IRewriteCompilationUnit unit) {

		return unit.getWorkerIdentifier().equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER)
				|| unit.getWorkerIdentifier().equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
	}

	private boolean checkForSameMethodDeclaration(VariableDeclarationStatement varDecl, MethodInvocation calledMethod) {

		return ASTNodes.findParent(varDecl, MethodDeclaration.class).get()
				.equals(ASTNodes.findParent(calledMethod, MethodDeclaration.class).get());

	}

	private boolean checkForSameStatement(ASTNode varDecl, IRewriteCompilationUnit toCheckUnit) {
		Statement statement = ASTNodes.findParentWithoutConsideringNode(varDecl, Statement.class).get();
		Multimap<IRewriteCompilationUnit, ? extends ASTNode> map = group
				.findMapToIdentifier(toCheckUnit.getWorkerIdentifier());

		Collection<? extends ASTNode> astNodes = map.get(toCheckUnit);
		for (ASTNode node : astNodes) {
			Statement statementToCheck = ASTNodes.findParentWithoutConsideringNode(node, Statement.class).get();
			if (ASTNodes.containsNode(statement, x -> x == statementToCheck))
				return true;
		}

		return false;

	}

	private void sortAndFilterMaps() {
		entriesRefactored = scanner.refactoredMethods.entrySet().stream()
				.filter(entry -> checkForMethodInvocOrVarDecl(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		entriesCalling = scanner.callingMethods.entrySet().stream()
				.filter(entryCalling -> checkForMethodInvocOrVarDecl(entryCalling.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Map<MethodDeclaration, IRewriteCompilationUnit> toAddToCalling = new HashMap<>();

		for (Entry<MethodDeclaration, IRewriteCompilationUnit> entryRefactored : entriesRefactored.entrySet()) {

			if (entryRefactored.getValue().getWorkerIdentifier().equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER)) {
				toAddToCalling.put(entryRefactored.getKey(), entryRefactored.getValue());
				entriesRefactored.remove(entryRefactored.getKey());
			}

		}

		for (Entry<MethodDeclaration, IRewriteCompilationUnit> entryCalling : entriesCalling.entrySet()) {

			if (entryCalling.getValue().getWorkerIdentifier().equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER)) {
				entriesRefactored.put(entryCalling.getKey(), entryCalling.getValue());
				entriesCalling.remove(entryCalling.getKey());
			}

		}

		entriesCalling.putAll(toAddToCalling);

	}

	private void changeDependentUnitsOfChangedMethodDeclaration(
			Entry<MethodDeclaration, IRewriteCompilationUnit> entry) {

		String methodName = entry.getKey().getName().getIdentifier();

		// check if we have right unit
		if (!entry.getValue().getWorkerIdentifier().getName().equals("Method Declarations")) {
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
		boolean swingWorkerInParams = listParameters.stream().anyMatch(param -> Types.isTypeOf(param, classBinary));
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

	private ProjectUnits searchForFieldDependencies() throws JavaModelException {

		// Change simpleNames which are fields
		Map<SimpleName, IRewriteCompilationUnit> simpleNames = changeSimpleNamesFields();

		// Change also corresponding FieldDeclaration
		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (unit.getWorkerIdentifier().getName().equals("Field Declarations")) {
				Collection<FieldDeclaration> actFieldDecls = group.getFieldDeclMap().get(unit);
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
				Collection<SimpleName> actSimpleNames = group.getSimpleNamesMap().get(unit);
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
			Collection<VariableDeclarationStatement> varDecls = group.getVarDeclMap().get(unit_Var);

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
}
