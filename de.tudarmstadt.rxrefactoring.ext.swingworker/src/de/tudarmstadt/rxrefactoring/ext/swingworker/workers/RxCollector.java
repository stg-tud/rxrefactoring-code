package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.core.utils.RelevantInvocation;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerUtils.Key;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerMapsUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016<br>
 * Adapted to new core by Camila Gonzalez on 19/01/2018
 */
public class RxCollector implements IWorker<Void, RxCollector> {

	@Override
	public @Nullable RxCollector refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception {
		String className = SwingWorkerInfo.getBinaryName();
		Set<IRewriteCompilationUnit> newUnits = Sets.newConcurrentHashSet();

		for (IRewriteCompilationUnit unit : units) {
			// Initialize Visitor
			DiscoveringVisitor discoveringVisitor = new DiscoveringVisitor(className);
			// Collect information using visitor
			unit.setWorker("All");
			unit.accept(discoveringVisitor);

			if (scope.equals(RefactorScope.WHOLE_PROJECT) || scope == null){
				WorkerMapsUtils.fillAllMap();
				for(Entry<String, Multimap<IRewriteCompilationUnit, ?>> m : WorkerMapsUtils.getAllMap().entries()) {		
					m.getValue().putAll(unit, WorkerMapsUtils.getNeededList(m.getKey(), discoveringVisitor));
				}
			} else if (scope.equals(RefactorScope.SEPARATE_OCCURENCES)) {
				WorkerMapsUtils.fillAllKeys();
				Set<IRewriteCompilationUnit> allWorkerUnits = addWorkerUnitsToMaps(discoveringVisitor, unit);
				newUnits.addAll(allWorkerUnits);
				units.remove(unit);
			}
			
			discoveringVisitor.cleanAllLists();

		}

		units.addAll(newUnits);

		return this;
	}

	private Set<IRewriteCompilationUnit> addWorkerUnitsToMaps(DiscoveringVisitor allVisitor,
			IRewriteCompilationUnit unit) {
		Set<IRewriteCompilationUnit> allWorkerUnits = Sets.newConcurrentHashSet();

		for (Key key : WorkerMapsUtils.getAllKeys()) {
				
				if(key.name.equals("Type Declarations") && !allVisitor.getTypeDeclarations().isEmpty())
					loopOverTypeDeclarations(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Field Declarations") && !allVisitor.getFieldDeclarations().isEmpty())
					loopOverFieldDeclarations(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Variable Declaration Statements") && !allVisitor.getVarDeclStatements().isEmpty())
					loopOverVariableDeclarationStatements(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Single Variable Declarations") && !allVisitor.getSingleVarDeclarations().isEmpty())
					loopOverSingleVariableDeclarations(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Simple Names") && !allVisitor.getSimpleNames().isEmpty())
					loopOverSimpleNames(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Assignments") && !allVisitor.getAssignments().isEmpty())
					loopOverAssignments(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Class Instances") && !allVisitor.getClassInstanceCreations().isEmpty())
					loopOverClassInstanceCreations(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Method Declarations") && !allVisitor.getMethodDeclarations().isEmpty())
					loopOverMethodDeclarations(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Method Invocations") && !allVisitor.getMethodInvocations().isEmpty())
					loopOverMethodInvocations(unit, key.name, allWorkerUnits, allVisitor);
				
				if(key.name.equals("Relevant Invocations") && !allVisitor.getRelevantInvocations().isEmpty())
					loopOverRelevantInvocations(unit, key.name, allWorkerUnits, allVisitor);
							
		}

		return allWorkerUnits;

	}
	
	private void loopOverTypeDeclarations(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (TypeDeclaration m : visitor.getTypeDeclarations()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getTypeDeclMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverFieldDeclarations(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (FieldDeclaration m : visitor.getFieldDeclarations()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getFieldDeclMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverAssignments(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (Assignment m : visitor.getAssignments()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getAssigmentsMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverSimpleNames(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (SimpleName m : visitor.getSimpleNames()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getSimpleNamesMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverClassInstanceCreations(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (ClassInstanceCreation m : visitor.getClassInstanceCreations()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getClassInstanceMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverVariableDeclarationStatements(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (VariableDeclarationStatement m : visitor.getVarDeclStatements()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getVarDeclMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverSingleVariableDeclarations(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (SingleVariableDeclaration m : visitor.getSingleVarDeclarations()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getSingleVarDeclMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}

	private void loopOverMethodInvocations(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (MethodInvocation m : visitor.getMethodInvocations()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getMethodInvocationsMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverMethodDeclarations(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (MethodDeclaration m : visitor.getMethodDeclarations()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getMethodDeclarationsMap().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}
	
	private void loopOverRelevantInvocations(IRewriteCompilationUnit unit, String keyName,
			Set<IRewriteCompilationUnit> allWorkerUnits, DiscoveringVisitor visitor) {
		
		for (RelevantInvocation m : visitor.getRelevantInvocations()) {
			ASTNode newNode = unit.copyNode(unit.getRoot());
			RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
			newUnit.setWorker(keyName);
			WorkerMapsUtils.getRelevantInvocations().put(newUnit, m);
			allWorkerUnits.add(newUnit);
		}
	}

	/*
	 * private Set<IRewriteCompilationUnit>
	 * checkForSameMethod(IRewriteCompilationUnit unit) { //TODO THA entfernen wenn
	 * Organizer umgesetzt MethodDeclaration outerMethod = null;
	 * Set<IRewriteCompilationUnit> set = Sets.newConcurrentHashSet();
	 * 
	 * for (Entry<IRewriteCompilationUnit, MethodInvocation> entry :
	 * WorkerMapsUtils.getMethodInvocationsMap().entries()) { MethodInvocation m =
	 * entry.getValue(); if (m.getExpression() != null &&
	 * entry.getKey().equals(unit)) { MethodDeclaration actualMD =
	 * ASTNodes.findParent(m.getExpression(), MethodDeclaration.class).get(); if
	 * (!(actualMD.equals(outerMethod)) && outerMethod != null) { ASTNode newNode =
	 * unit.copyNode(actualMD.getParent()); RewriteCompilationUnit newUnit = new
	 * RewriteCompilationUnit(unit.getPrimary(), newNode);
	 * newUnit.setWorker("methodInvocationsMap"); set.add(newUnit); } outerMethod =
	 * actualMD; } } return set; }
	 */

	public Multimap<IRewriteCompilationUnit, TypeDeclaration> getTypeDeclMap() {
		return WorkerMapsUtils.getTypeDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, FieldDeclaration> getFieldDeclMap() {
		return WorkerMapsUtils.getFieldDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, Assignment> getAssigmentsMap() {
		return WorkerMapsUtils.getAssigmentsMap();
	}

	public Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> getVarDeclMap() {
		return WorkerMapsUtils.getVarDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, SimpleName> getSimpleNamesMap() {
		return WorkerMapsUtils.getSimpleNamesMap();
	}

	public Multimap<IRewriteCompilationUnit, ClassInstanceCreation> getClassInstanceMap() {
		return WorkerMapsUtils.getClassInstanceMap();
	}

	public Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> getSingleVarDeclMap() {
		return WorkerMapsUtils.getSingleVarDeclMap();
	}

	public static Multimap<IRewriteCompilationUnit, MethodInvocation> getMethodInvocationsMap() {
		return WorkerMapsUtils.getMethodInvocationsMap();
	}

	public Multimap<IRewriteCompilationUnit, MethodDeclaration> getMethodDeclarationsMap() {
		return WorkerMapsUtils.getMethodDeclarationsMap();
	}

	public Multimap<IRewriteCompilationUnit, RelevantInvocation> getRelevantInvocations() {
		return WorkerMapsUtils.getRelevantInvocations();
	}

	@Override
	public @Nullable RxCollector refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		// TODO Auto-generated method stub
		// only needed for classes without RefactorScope implemented
		return null;
	}

}
