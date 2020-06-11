package de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.experimental.theories.internal.Assignments;

import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerUtils;

public class DependencyCheckFieldDecl {

	ProjectUnits units;
	Map<String, IRewriteCompilationUnit> simpleNamesAndAssignments = Maps.newHashMap();

	public DependencyCheckFieldDecl(ProjectUnits units) {
		this.units = units;

	}

	protected ProjectUnits searchForFieldDependencies() throws JavaModelException {
		changeSimpleNamesFields();
		changeAssignments();
		// Change also corresponding FieldDeclaration
		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (unit.getWorkerIdentifier().getName().equals("Field Declarations")) {
				Collection<FieldDeclaration> actFieldDecls = WorkerUtils.getFieldDeclMap().get(unit);
				for (FieldDeclaration fieldDecl : actFieldDecls) {
					VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDecl.fragments()
							.get(0);
					String identifier = varDeclFrag.getName().getIdentifier();

					if (simpleNamesAndAssignments.keySet().stream().anyMatch(x -> x.equals(identifier))
							&& unit.getCorrespondingResource().equals(simpleNamesAndAssignments
									.get(varDeclFrag.getName().getIdentifier()).getCorrespondingResource())) {
						unit.setWorkerIdentifier(
								new WorkerIdentifier(unit.getWorkerIdentifier().getName() + " " + identifier));
					}
				}
			}
		}
		return units;

	}

	private void changeSimpleNamesFields() {

		for (IRewriteCompilationUnit unit : units.getUnits()) {

			if (unit.getWorkerIdentifier().equals(NamingUtils.SIMPLE_NAME_IDENTIFIER)) {

				Collection<SimpleName> actSimpleNames = WorkerUtils.getSimpleNamesMap().get(unit);
				for (SimpleName name : actSimpleNames) {
					
					IBinding binding = name.resolveBinding();
					boolean isField = false;
					if (binding.getKind() == IBinding.VARIABLE) {
						IVariableBinding curr = (IVariableBinding) binding;
						isField = curr.isField();
					}

					if (isField && !simpleNamesAndAssignments.containsKey(name)) {
						simpleNamesAndAssignments.put(name.getIdentifier(), unit);
						unit.setWorkerIdentifier(new WorkerIdentifier("Field Declarations " + name.getIdentifier()));
						
					}
				}
		}

	}
	}

	private void changeAssignments() {

		for (IRewriteCompilationUnit unit : units.getUnits()) {

			if (unit.getWorkerIdentifier().equals(NamingUtils.ASSIGNMENTS_IDENTIFIER)) {

				Collection<Assignment> actAssignments = WorkerUtils.getAssigmentsMap().get(unit);
				for (Assignment assignment : actAssignments) {

					Expression leftHandSide = assignment.getLeftHandSide();
					if (leftHandSide instanceof SimpleName) {
						SimpleName name = (SimpleName) leftHandSide;
						IBinding binding = name.resolveBinding();

						boolean isField = false;
						if (binding.getKind() == IBinding.VARIABLE) {
							IVariableBinding curr = (IVariableBinding) binding;
							isField = curr.isField();
						}

						if (isField && !simpleNamesAndAssignments.containsKey(name)) {
							simpleNamesAndAssignments.put(name.getIdentifier(), unit);
							unit.setWorkerIdentifier(
									new WorkerIdentifier("Field Declarations " + name.getIdentifier()));
						}
					}
				}

			}
		}

	}

}
