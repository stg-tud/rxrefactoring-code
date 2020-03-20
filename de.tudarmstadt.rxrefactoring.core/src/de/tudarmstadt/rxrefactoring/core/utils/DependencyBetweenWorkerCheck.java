package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IDependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;

public class DependencyBetweenWorkerCheck implements IDependencyBetweenWorkerCheck {

	private ProjectUnits units;

	public DependencyBetweenWorkerCheck(ProjectUnits units) {
		this.units = units;

	}

	@Override
	public ProjectUnits regroupBecauseOfMethodDependencies() {
		MethodScanner scanner = new MethodScanner();
		scanner.scan(units);

		int i = 1;
		for (Entry<MethodDeclaration, IRewriteCompilationUnit> entry : scanner.refactoredMethods.entrySet()) {

			if (checkIfReturnTypeIsSwingWorkerOrExtendsFromIt(entry.getKey())) {
				changeDependentUnitsOfChangedMethodDeclaration(entry, i);
				i++;
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

	private void changeDependentUnitsOfChangedMethodDeclaration(Entry<MethodDeclaration, IRewriteCompilationUnit> entry,
			Integer i) {

		// first change methodDeclUnit

		Optional<IRewriteCompilationUnit> unit_methodDecl = units.getUnits().stream()
				.filter(unit -> unit.equals(entry.getValue())).findFirst();
		if (unit_methodDecl.isPresent()) {
			units.getUnits().stream().filter(unit -> unit.equals(unit_methodDecl.get()))
					.forEach(unit -> unit.setWorker("Change of MethodDeclaration: " + i));
		}

		// second change of methodInvocations result in different VaraiableDeclarations

		Set<IRewriteCompilationUnit> units_VariableDecl = getVariableDeclarationsTo(entry.getKey(), entry.getValue());
		if (!units_VariableDecl.isEmpty()) {
			for (IRewriteCompilationUnit unit_act : units_VariableDecl) {
				units.getUnits().stream().filter(unit -> unit.equals(unit_act))
						.forEach(unit -> unit.setWorker("Change of MethodDeclaration: " + i));
			}
		}

		// third change class instance creation in returnType

		Set<IRewriteCompilationUnit> unit_classInstances = getClassInstanceCreationUnit(entry.getKey());
		if (!unit_classInstances.isEmpty()) {
			for (IRewriteCompilationUnit unit_act : unit_classInstances) {
				units.getUnits().stream().filter(unit -> unit.equals(unit_act))
						.forEach(unit -> unit.setWorker("Change of MethodDeclaration: " + i));
			}
		}

	}

	/*
	 * private void searchForMethodInvocation(Map.Entry<MethodDeclaration,
	 * IRewriteCompilationUnit> entry, Integer i) { Map<IRewriteCompilationUnit,
	 * String> toChangeWorker = new HashMap<IRewriteCompilationUnit, String>();
	 * 
	 * for (IRewriteCompilationUnit unit : units.getUnits()) { if
	 * (toChangeWorker.keySet().contains(unit)) {
	 * unit.setWorker(toChangeWorker.get(unit)); } if
	 * (unit.getWorker().equals("Method Invocations")) { /*
	 * Collection<MethodInvocation> methodInvs =
	 * WorkerUtils.getMethodInvocationsMap().get(unit); for (MethodInvocation m :
	 * methodInvs) { IMethodBinding bindingInv = m.resolveMethodBinding();
	 * MethodDeclaration methodDecl = entry.getKey(); IRewriteCompilationUnit
	 * unitDecl = entry.getValue(); IMethodBinding bindingDecl =
	 * methodDecl.resolveBinding(); if (bindingInv.equals(bindingDecl)) {
	 * unit.setWorker(" same" + i); toChangeWorker.put(unitDecl, "same" + i); } }
	 */
	/*
	 * }
	 * 
	 * if (unit.getWorker().equals("Variable Declarations")) {
	 * Collection<VariableDeclarationStatement> varDecls =
	 * WorkerUtils.getVarDeclMap().get(unit); for (VariableDeclarationStatement st :
	 * varDecls) {
	 * 
	 * VariableDeclarationFragment fragment = (VariableDeclarationFragment)
	 * st.fragments().get(0); Expression initializer = fragment.getInitializer(); if
	 * (initializer instanceof MethodInvocation) { MethodInvocation method =
	 * (MethodInvocation) initializer; IMethodBinding binding =
	 * method.resolveMethodBinding(); if
	 * (binding.equals(entry.getKey().resolveBinding()) &&
	 * entry.getValue().getResource().equals(unit.getResource())) {
	 * unit.setWorker("Change of MethodDeclaration: " + i);
	 * toChangeWorker.put(entry.getValue(), "Change of MethodDeclaration: " + i);
	 * 
	 * } } } }
	 * 
	 * if (unit.getWorker().equals("Method Declarations") &&
	 * entry.getValue().getResource().equals(unit.getResource())) {
	 * Collection<MethodDeclaration> methodDecls =
	 * WorkerUtils.getMethodDeclarationsMap().get(unit); for (MethodDeclaration decl
	 * : methodDecls) { Type type = decl.getReturnType2(); if
	 * (Types.isTypeOf(type.resolveBinding(), "javax.swing.SwingWorker")) {
	 * unit.setWorker("Change of MethodDeclaration: " + i); } } }
	 * 
	 * if (unit.getWorker().equals("Class Instances") &&
	 * entry.getValue().getResource().equals(unit.getResource())) {
	 * Collection<ClassInstanceCreation> classInstances =
	 * WorkerUtils.getClassInstanceMap().get(unit); for (ClassInstanceCreation
	 * instance : classInstances) { Optional<MethodDeclaration> methodDecl =
	 * ASTNodes.findParent(instance, MethodDeclaration.class); if
	 * (methodDecl.isPresent()) { if (entry.getKey().equals(methodDecl.get()))
	 * unit.setWorker("Change of MethodDeclaration: " + i); } } } }
	 * 
	 * }
	 */

	public ProjectUnits searchForFieldDependencies() throws JavaModelException {
		Map<SimpleName, IRewriteCompilationUnit> simpleNames = Maps.newHashMap();
		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (unit.getWorker().equals("Simple Names")) {

				Collection<SimpleName> actSimpleNames = WorkerUtils.getSimpleNamesMap().get(unit);
				for (SimpleName name : actSimpleNames) {
					// simpleNames.put(name, unit);
					// unit.setWorker("Field Declarations " + name.getIdentifier());

					IBinding binding = name.resolveBinding();
					boolean isField = false;
					if (binding.getKind() == IBinding.VARIABLE) {
						IVariableBinding curr = (IVariableBinding) binding;
						isField = curr.isField();
					}

					if (isField) {
						simpleNames.put(name, unit);
						unit.setWorker("Field Declarations " + name.getIdentifier());
					}
				}

			}
		}

		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (unit.getWorker().equals("Field Declarations")) {
				Collection<FieldDeclaration> actFieldDecls = WorkerUtils.getFieldDeclMap().get(unit);
				for (FieldDeclaration fieldDecl : actFieldDecls) {
					VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDecl.fragments()
							.get(0);
					String identifier = varDeclFrag.getName().getIdentifier();

					if (simpleNames.keySet().stream().anyMatch(x -> x.getIdentifier().equals(identifier))) {
						IRewriteCompilationUnit u = simpleNames.get(varDeclFrag.getName());
						if (unit.getCorrespondingResource().equals(u.getCorrespondingResource()))
							unit.setWorker(unit.getWorker() + " " + identifier);
					}
				}
			}
		}
		return units;

	}

	private Set<IRewriteCompilationUnit> getVariableDeclarationsTo(MethodDeclaration methodDecl,
			IRewriteCompilationUnit unit_MethodDecl) {
		Set<IRewriteCompilationUnit> unitsToChange = new HashSet<IRewriteCompilationUnit>();

		Set<IRewriteCompilationUnit> units_MethodInvoc = units.stream()
				.filter(unit -> unit.getWorker().equals("Variable Declarations"))
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

	private Set<IRewriteCompilationUnit> getMethodInvocationsTo(MethodDeclaration methodDecl,
			IRewriteCompilationUnit unit_MethodDecl) {

		Set<IRewriteCompilationUnit> unitsToChange = new HashSet<IRewriteCompilationUnit>();

		Set<IRewriteCompilationUnit> units_MethodInvoc = units.stream()
				.filter(unit -> unit.getWorker().equals("Method Invocations"))
				.filter(unit -> unit.getResource().equals(unit_MethodDecl.getResource())).collect(Collectors.toSet());

		for (IRewriteCompilationUnit unit : units_MethodInvoc) {
			Collection<MethodInvocation> invocs = WorkerUtils.getMethodInvocationsMap().get(unit);
			for (MethodInvocation invoc : invocs) {
				if (invoc.resolveMethodBinding().equals(methodDecl.resolveBinding())) {

					Optional<VariableDeclarationStatement> varDeclSt = ASTNodes.findParent(invoc,
							VariableDeclarationStatement.class);
					if (varDeclSt.isPresent()) {
						Optional<Entry<IRewriteCompilationUnit, VariableDeclarationStatement>> matchedEntry = WorkerUtils
								.getVarDeclMap().entries().stream().filter(e -> e.getValue().equals(varDeclSt.get()))
								.findFirst();

						if (matchedEntry.isPresent()) {
							IRewriteCompilationUnit unitVarDecl = matchedEntry.get().getKey();
							// unit change name of worker
							unitsToChange.add(unitVarDecl);

						}
					}
				}

			}

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

}
