package de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerUtils;

public class DependencyCheckFieldDecl {
	
	ProjectUnits units;
	
	public DependencyCheckFieldDecl(ProjectUnits units) {
		this.units = units;
		
	}
	
	protected ProjectUnits searchForFieldDependencies() throws JavaModelException {

		// Change simpleNames which are fields
		Map<String, IRewriteCompilationUnit> simpleNames = changeSimpleNamesFields();

		// Change also corresponding FieldDeclaration
		for (IRewriteCompilationUnit unit : units.getUnits()) {
			if (unit.getWorkerIdentifier().getName().equals("Field Declarations")) {
				Collection<FieldDeclaration> actFieldDecls = WorkerUtils.getFieldDeclMap().get(unit);
				for (FieldDeclaration fieldDecl : actFieldDecls) {
					VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDecl.fragments()
							.get(0);
					String identifier = varDeclFrag.getName().getIdentifier();

					if (simpleNames.keySet().stream().anyMatch(x -> x.equals(identifier))
							&& unit.getCorrespondingResource()
									.equals(simpleNames.get(varDeclFrag.getName().getIdentifier()).getCorrespondingResource())) {
						unit.setWorkerIdentifier(
								new WorkerIdentifier(unit.getWorkerIdentifier().getName() + " " + identifier));
					}
				}
			}
		}
		return units;

	}

	private Map<String, IRewriteCompilationUnit> changeSimpleNamesFields() {

		Map<String, IRewriteCompilationUnit> simpleNames = Maps.newHashMap();
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
						simpleNames.put(name.getIdentifier(), unit);
						unit.setWorkerIdentifier(new WorkerIdentifier("Field Declarations " + name.getIdentifier()));
					}
				}

			}
		}

		return simpleNames;

	}


}
