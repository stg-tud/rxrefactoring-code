package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
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
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnitFactory;
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
			
		
			Set<IRewriteCompilationUnit> allWorkerUnits = addWorkerUnitsToMaps(unit.getPrimary());
			
			units.addAll(allWorkerUnits);

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
		System.out.println(varDeclMap.entries());
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
	
	private Set<IRewriteCompilationUnit> addWorkerUnitsToMaps(ICompilationUnit compilationUnit) {
		DiscoveringVisitor discoveringVisitor1 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor2 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor3 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor4 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor5 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor6 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor7 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor8 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor9 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		DiscoveringVisitor discoveringVisitor10 = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		// Collect information using visitor
		Set<IRewriteCompilationUnit> allWorkerUnits = Sets.newConcurrentHashSet();
		RewriteCompilationUnitFactory factory = new RewriteCompilationUnitFactory();
		
		IRewriteCompilationUnit unitTypeDeclarationWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitFieldDeclarationWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitAssignmentsWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitVariableDeclWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitSimpleNamesWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitClassInstanceWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitSingleVariableDeclWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitMethodInvocationWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitMethodDeclarationWorker = factory.from(compilationUnit);
		IRewriteCompilationUnit unitRelevantInvocationsWorker = factory.from(compilationUnit);
		
		unitTypeDeclarationWorker.accept(discoveringVisitor1);
		typeDeclMap.putAll(unitTypeDeclarationWorker, discoveringVisitor1.getTypeDeclarations());
		
		unitFieldDeclarationWorker.accept(discoveringVisitor2);
		fieldDeclMap.putAll(unitFieldDeclarationWorker, discoveringVisitor2.getFieldDeclarations());
		
		unitAssignmentsWorker.accept(discoveringVisitor3);
		assigmentsMap.putAll(unitAssignmentsWorker, discoveringVisitor3.getAssignments());
		
		unitVariableDeclWorker.accept(discoveringVisitor4);
		varDeclMap.putAll(unitVariableDeclWorker, discoveringVisitor4.getVarDeclStatements());
		
		unitSimpleNamesWorker.accept(discoveringVisitor5);
		simpleNamesMap.putAll(unitSimpleNamesWorker, discoveringVisitor5.getSimpleNames());
		
		unitClassInstanceWorker.accept(discoveringVisitor6);
		classInstanceMap.putAll(unitClassInstanceWorker, discoveringVisitor6.getClassInstanceCreations());
		
		unitSingleVariableDeclWorker.accept(discoveringVisitor7);
		singleVarDeclMap.putAll(unitSingleVariableDeclWorker, discoveringVisitor7.getSingleVarDeclarations());
		
		unitMethodInvocationWorker.setWorker("MethodInvocationWorker"); //TODO THA für alle Name setzen
		unitMethodInvocationWorker.accept(discoveringVisitor8);
		methodInvocationsMap.putAll(unitMethodInvocationWorker, discoveringVisitor8.getMethodInvocations());
		
		unitMethodDeclarationWorker.accept(discoveringVisitor9);
		methodDeclarationsMap.putAll(unitMethodDeclarationWorker, discoveringVisitor9.getMethodDeclarations());		

		unitRelevantInvocationsWorker.accept(discoveringVisitor10);
		relevantInvocationsMap.putAll(unitRelevantInvocationsWorker, discoveringVisitor10.getRelevantInvocations());
		
		allWorkerUnits.add(unitTypeDeclarationWorker);
		allWorkerUnits.add(unitFieldDeclarationWorker);
		allWorkerUnits.add(unitAssignmentsWorker);
		allWorkerUnits.add(unitVariableDeclWorker);
		allWorkerUnits.add(unitSimpleNamesWorker);
		allWorkerUnits.add(unitClassInstanceWorker);
		allWorkerUnits.add(unitSingleVariableDeclWorker);
		allWorkerUnits.add(unitMethodInvocationWorker);
		allWorkerUnits.add(unitMethodDeclarationWorker);
		allWorkerUnits.add(unitRelevantInvocationsWorker);
		return allWorkerUnits;
				
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
