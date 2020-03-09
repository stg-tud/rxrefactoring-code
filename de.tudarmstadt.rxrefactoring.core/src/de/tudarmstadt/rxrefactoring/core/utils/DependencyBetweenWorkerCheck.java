package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Set;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
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
		// Map<Map.Entry<MethodDeclaration, IRewriteCompilationUnit>,
		// Map.Entry<MethodDeclaration, IRewriteCompilationUnit>> mappingMap =
		// scanner.mappingCalledRefactoredMethods;

		for (Entry<MethodDeclaration, IRewriteCompilationUnit> entry : scanner.refactoredMethods.entrySet()) {

			searchForMethodInvocation(entry);

		}

		scanner.clearMaps();

		/*
		 * for(IRewriteCompilationUnit unit: units.getUnits()) {
		 * for(Entry<Map.Entry<MethodDeclaration, IRewriteCompilationUnit>,
		 * Map.Entry<MethodDeclaration, IRewriteCompilationUnit>> entryMap :
		 * mappingMap.entrySet()) { Map.Entry<MethodDeclaration,
		 * IRewriteCompilationUnit> keyRefactored = entryMap.getKey();
		 * IRewriteCompilationUnit unitRefactored = keyRefactored.getValue();
		 * Map.Entry<MethodDeclaration, IRewriteCompilationUnit> valueCalling =
		 * entryMap.getValue(); IRewriteCompilationUnit unitCalling =
		 * valueCalling.getValue(); int i = 1; if(unit.equals(unitRefactored) ||
		 * unit.equals(unitCalling)) { unit.setWorker("test" + i); i++; } } }
		 */

		return units;

	}

	private void searchForMethodInvocation(Map.Entry<MethodDeclaration, IRewriteCompilationUnit> entry) {
		int i = 1;
		Map<IRewriteCompilationUnit, String> toChangeWorker = new HashMap<IRewriteCompilationUnit, String>();
		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (toChangeWorker.keySet().contains(unit)) {
				unit.setWorker(toChangeWorker.get(unit));
			}
			if (unit.getWorker().equals("Method Invocations")) {
				/*
				 * Collection<MethodInvocation> methodInvs =
				 * WorkerUtils.getMethodInvocationsMap().get(unit); for (MethodInvocation m :
				 * methodInvs) { IMethodBinding bindingInv = m.resolveMethodBinding();
				 * MethodDeclaration methodDecl = entry.getKey(); IRewriteCompilationUnit
				 * unitDecl = entry.getValue(); IMethodBinding bindingDecl =
				 * methodDecl.resolveBinding(); if (bindingInv.equals(bindingDecl)) {
				 * unit.setWorker(" same" + i); toChangeWorker.put(unitDecl, "same" + i); } }
				 */
			}

			if (unit.getWorker().equals("Variable Declaration Statements")) {
				Collection<VariableDeclarationStatement> varDecls = WorkerUtils.getVarDeclMap().get(unit);
				for (VariableDeclarationStatement st : varDecls) {

					VariableDeclarationFragment fragment = (VariableDeclarationFragment) st.fragments().get(0);
					Expression initializer = fragment.getInitializer();
					if (initializer instanceof MethodInvocation) {
						MethodInvocation method = (MethodInvocation) initializer;
						IMethodBinding binding = method.resolveMethodBinding();
						if (binding.equals(entry.getKey().resolveBinding()) && entry.getValue().getResource().equals(unit.getResource())) {
							unit.setWorker("Change of MethodDeclaration: " + i);
							toChangeWorker.put(entry.getValue(), "Change of MethodDeclaration: " + i);

						}
					}
				}
			}

			if (unit.getWorker().equals("Method Declarations")) {
				Collection<MethodDeclaration> methodDecls = WorkerUtils.getMethodDeclarationsMap().get(unit);
				for (MethodDeclaration decl : methodDecls) {
					Type type = decl.getReturnType2();
					if (Types.isTypeOf(type.resolveBinding(), "javax.swing.SwingWorker")) {
						unit.setWorker("Change of MethodDeclaration: " + i);
					}
				}
			}

			if (unit.getWorker().equals("Class Instances")) {
				Collection<ClassInstanceCreation> classInstances = WorkerUtils.getClassInstanceMap().get(unit);
				for (ClassInstanceCreation instance : classInstances) {
					Optional<MethodDeclaration> methodDecl = ASTNodes.findParent(instance, MethodDeclaration.class);
					if (methodDecl.isPresent()) {
						if (entry.getKey().equals(methodDecl.get()))
							unit.setWorker("Change of MethodDeclaration: " + i);
					}
				}
			}
		}

	}

	public ProjectUnits searchForFieldDependencies() throws JavaModelException {
		Map<SimpleName, IRewriteCompilationUnit> simpleNames = Maps.newHashMap();
		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (unit.getWorker().equals("Simple Names")) {

				Collection<SimpleName> actSimpleNames = WorkerUtils.getSimpleNamesMap().get(unit);
				for (SimpleName name : actSimpleNames) {
					simpleNames.put(name, unit);
					unit.setWorker("Field Declarations "+ name.getIdentifier());
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

}
