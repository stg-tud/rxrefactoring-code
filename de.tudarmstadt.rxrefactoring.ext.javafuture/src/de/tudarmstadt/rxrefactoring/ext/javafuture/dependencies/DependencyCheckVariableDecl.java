package de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.CollectorGroup;

public class DependencyCheckVariableDecl {

	CollectorGroup group;
	ProjectUnits units;
	int counterVar = 0;
	Map<String, SingleVariableDeclaration> singleVarDeclsToCheck = new HashMap<>();
	String nameWorker;

	public DependencyCheckVariableDecl(ProjectUnits units, CollectorGroup group) {
		this.group = group;
		this.units = units;

	}

	protected ProjectUnits checkVariableDeclarationsWithInMethod(String name) {
		nameWorker = name;

		List<IRewriteCompilationUnit> filteredUnits = units.getUnits().stream()
				.filter(elem -> elem.getWorkerIdentifier().name.equals(name)).collect(Collectors.toList());

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

						unitToCheck.setWorkerIdentifier(
								new WorkerIdentifier(namingHelper() + varName.getIdentifier()));

					} else {
						unitToCheck.setWorkerIdentifier(
								new WorkerIdentifier(namingHelper() + varName.getIdentifier()));
					}
				}
			}

			for (Entry<String, SingleVariableDeclaration> singleVar : singleVarDeclsToCheck.entrySet()) {
				SimpleName varName = singleVar.getValue().getName();

				for (IRewriteCompilationUnit unitToCheck : units) {
					if (!unit.equals(unitToCheck)) {
						if (!checkUnitForSameResourceAndBlock(unit, singleVar.getValue(), unitToCheck))
							continue;
						if (!checkForSameVariable(varName.getIdentifier(), unitToCheck))
							continue;

						unitToCheck.setWorkerIdentifier(new WorkerIdentifier(singleVar.getKey()));
					}

				}
			}

		}

		return units;
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
					singleVarDeclsToCheck.put(namingHelper() + varName, singleVarDecl);
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

	private String namingHelper() {
		if (nameWorker.equals("Cursor Selection")) {
			return "Cursor Selection Variable: ";

		} else {
			return "Change #" + String.valueOf(counterVar) + " according to Variable: ";
		}

	}
}
