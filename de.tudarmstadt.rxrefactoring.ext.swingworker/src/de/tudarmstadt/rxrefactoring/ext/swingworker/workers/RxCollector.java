package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016<br>
 * Adapted to new core by Camila Gonzalez on 19/01/2018
 */
public class RxCollector implements IWorker<Void, RxCollector> {
	private final Multimap<IRewriteCompilationUnit, TypeDeclaration> typeDeclMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, FieldDeclaration> fieldDeclMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, Assignment> assigmentsMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> varDeclMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, SimpleName> simpleNamesMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, ClassInstanceCreation> classInstanceMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> singleVarDeclMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, MethodInvocation> methodInvocationsMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, MethodDeclaration> methodDeclarationsMap = HashMultimap.create();
	private final Multimap<IRewriteCompilationUnit, MethodInvocation> relevantInvocationsMap = HashMultimap.create();

	@Override
	public @Nullable RxCollector refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary) throws Exception {
		String className = SwingWorkerInfo.getBinaryName();
		for (IRewriteCompilationUnit unit : units) {
			// Initialize Visitor
			DiscoveringVisitor discoveringVisitor = new DiscoveringVisitor(className);

			// Collect information using visitor
			unit.accept(discoveringVisitor);
			typeDeclMap.putAll(unit, discoveringVisitor.getTypeDeclarations());
			fieldDeclMap.putAll(unit, discoveringVisitor.getFieldDeclarations());
			assigmentsMap.putAll(unit, discoveringVisitor.getAssignments());
			varDeclMap.putAll(unit, discoveringVisitor.getVarDeclStatements());
			simpleNamesMap.putAll(unit, discoveringVisitor.getSimpleNames());
			classInstanceMap.putAll(unit, discoveringVisitor.getClassInstanceCreations());
			singleVarDeclMap.putAll(unit, discoveringVisitor.getSingleVarDeclarations());
			methodInvocationsMap.putAll(unit, discoveringVisitor.getMethodInvocations());
			methodDeclarationsMap.putAll(unit, discoveringVisitor.getMethodDeclarations());
			relevantInvocationsMap.putAll(unit, discoveringVisitor.getRelevantInvocations());

		}
		summary.setCorrect("numberOfCompilationUnits", getNumberOfCompilationUnits());
		return this;
	}

	public Multimap<IRewriteCompilationUnit, TypeDeclaration> getTypeDeclMap() {
		return typeDeclMap;
	}

	public Multimap<IRewriteCompilationUnit, FieldDeclaration> getFieldDeclMap() {
		return fieldDeclMap;
	}

	public Multimap<IRewriteCompilationUnit, Assignment> getAssigmentsMap() {
		return assigmentsMap;
	}

	public Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> getVarDeclMap() {
		return varDeclMap;
	}

	public Multimap<IRewriteCompilationUnit, SimpleName> getSimpleNamesMap() {
		return simpleNamesMap;
	}

	public Multimap<IRewriteCompilationUnit, ClassInstanceCreation> getClassInstanceMap() {
		return classInstanceMap;
	}

	public Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> getSingleVarDeclMap() {
		return singleVarDeclMap;
	}

	public Multimap<IRewriteCompilationUnit, MethodInvocation> getMethodInvocationsMap() {
		return methodInvocationsMap;
	}

	public Multimap<IRewriteCompilationUnit, MethodDeclaration> getMethodDeclarationsMap() {
		return methodDeclarationsMap;
	}

	public Multimap<IRewriteCompilationUnit, MethodInvocation> getRelevantInvocations() {
		return relevantInvocationsMap;
	}

	public int getNumberOfCompilationUnits() {
		Set<IRewriteCompilationUnit> allCompilationUnits = new HashSet<>();
		allCompilationUnits.addAll(typeDeclMap.keySet());
		allCompilationUnits.addAll(fieldDeclMap.keySet());
		allCompilationUnits.addAll(assigmentsMap.keySet());
		allCompilationUnits.addAll(varDeclMap.keySet());
		allCompilationUnits.addAll(simpleNamesMap.keySet());
		allCompilationUnits.addAll(classInstanceMap.keySet());
		allCompilationUnits.addAll(singleVarDeclMap.keySet());
		allCompilationUnits.addAll(methodInvocationsMap.keySet());
		allCompilationUnits.addAll(methodDeclarationsMap.keySet());
		return allCompilationUnits.size();
	}

	public String getDetails() {
		return "Nr. files: " + getNumberOfCompilationUnits() + "\n" + "TypeDeclarations = "
				+ typeDeclMap.values().size() + "\n" + "FieldDeclarations = " + fieldDeclMap.values().size() + "\n"
				+ "Assignments = " + assigmentsMap.values().size() + "\n" + "VariableDeclarationStatements = "
				+ varDeclMap.values().size() + "\n" + "SimpleNames = " + simpleNamesMap.values().size() + "\n"
				+ "ClassInstanceCreations = " + classInstanceMap.values().size() + "\n"
				+ "SingleVariableDeclarations = " + singleVarDeclMap.values().size() + "\n" + "MethodInvocations = "
				+ methodInvocationsMap.values().size() + "\n" + "MethodDeclarations = "
				+ methodDeclarationsMap.values().size();
	}

}
