package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.IWorkerV1;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnitFactory;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016<br>
 * Adapted to new core by Camila Gonzalez on 19/01/2018
 */
public class RxCollector implements IWorker<Void, RxCollector> {
	private final Multimap<String, Multimap<IRewriteCompilationUnit,?>> allWorkerMap = HashMultimap.create();
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

	@SuppressWarnings("unchecked") //TODO THA
	@Override
	public @Nullable RxCollector refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception {
		String className = SwingWorkerInfo.getBinaryName();
		fillAllWorkerMap();
		Set<IRewriteCompilationUnit> newUnits = Sets.newConcurrentHashSet();
		
		for (IRewriteCompilationUnit unit : units) {
			// Initialize Visitor
			DiscoveringVisitor discoveringVisitor = new DiscoveringVisitor(className);
			// Collect information using visitor
			unit.setWorker("All");
			unit.accept(discoveringVisitor);
			
			if(scope.equals(RefactorScope.WHOLE_PROJECT)) {
				for(Entry<String, Multimap<IRewriteCompilationUnit, ?>> entry: allWorkerMap.entries()) {	
					entry.getValue().putAll(unit, getNeededList(entry.getKey(), discoveringVisitor)); 
				}
			} else if(scope.equals(RefactorScope.SEPARATE_OCCURENCES)){
				Set<IRewriteCompilationUnit> allWorkerUnits = addWorkerUnitsToMaps(unit.getPrimary(), discoveringVisitor);
				newUnits.addAll(allWorkerUnits);
				
			}

		}
		
		units.addAll(newUnits);
	
		return this;
	}
	
	public Multimap<String, Multimap<IRewriteCompilationUnit, ?>> getAllWorkerMap(){
		return allWorkerMap;
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
		allCompilationUnits.addAll(typeDeclMap.keySet()); // TODO THA correct calculation not used anymore
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
	
	private void fillAllWorkerMap() {
		allWorkerMap.put("varDeclMap", varDeclMap);
		allWorkerMap.put("typeDeclMap", typeDeclMap);
		allWorkerMap.put("fieldDeclMap", fieldDeclMap);
		allWorkerMap.put("assigmentsMap", assigmentsMap);
		allWorkerMap.put("simpleNamesMap", simpleNamesMap);
		allWorkerMap.put("classInstanceMap", classInstanceMap);
		allWorkerMap.put("singleVarDeclMap", singleVarDeclMap);
		allWorkerMap.put("methodInvocationsMap", methodInvocationsMap);
		allWorkerMap.put("methodDeclarationsMap", methodDeclarationsMap);
		allWorkerMap.put("relevantInvocationsMap", relevantInvocationsMap);
		
	}
	
	private List getNeededList(String key, DiscoveringVisitor visitor){
		switch(key) {
		case "varDeclMap": 
			return visitor.getVarDeclStatements();	
		case "typeDeclMap":
			return visitor.getTypeDeclarations();
		case "fieldDeclMap":
			return visitor.getFieldDeclarations();
		case "assigmentsMap":
			return visitor.getAssignments();
		case "simpleNamesMap":
			return visitor.getSimpleNames();
		case "classInstanceMap":
			return visitor.getClassInstanceCreations();
		case "singleVarDeclMap":
			return visitor.getSingleVarDeclarations();
		case "methodInvocationsMap":
			return visitor.getMethodInvocations();
		case "methodDeclarationsMap":
			return visitor.getMethodDeclarations();
		case "relevantInvocationsMap":
			return visitor.getRelevantInvocations();
		default :
			throw new IllegalStateException("Key not in different Maps!");
		}
	}
	
	@SuppressWarnings("unchecked")
	private Set<IRewriteCompilationUnit> addWorkerUnitsToMaps(ICompilationUnit compilationUnit, DiscoveringVisitor allVisitor) {
		
		// Collect information using visitor
		Set<IRewriteCompilationUnit> allWorkerUnits = Sets.newConcurrentHashSet();
		RewriteCompilationUnitFactory factory = new RewriteCompilationUnitFactory();
		DiscoveringVisitor visitor = new DiscoveringVisitor(SwingWorkerInfo.getBinaryName());
		
		for(Entry<String, Multimap<IRewriteCompilationUnit, ?>> entry: allWorkerMap.entries()) {
			
			if(!getNeededList(entry.getKey(), allVisitor).isEmpty()) {
				IRewriteCompilationUnit unitWorker = factory.from(compilationUnit);
				unitWorker.setWorker(entry.getKey());
				unitWorker.accept(visitor);
				entry.getValue().putAll(unitWorker, getNeededList(entry.getKey(), visitor)); 
				allWorkerUnits.add(unitWorker);
				//if(entry.getKey().matches("methodInvocationsMap")) {
					//allWorkerUnits.addAll(checkForSameMethod(unitWorker));
				//}
				visitor.cleanAllLists();	
			}
		}
		
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
	
	private Set<IRewriteCompilationUnit> checkForSameMethod(IRewriteCompilationUnit unit) {
		
		MethodDeclaration outerMethod = null;
		Set<IRewriteCompilationUnit> set = Sets.newConcurrentHashSet();
		
		for(Entry<IRewriteCompilationUnit,  MethodInvocation> entry :  methodInvocationsMap.entries()) {
			MethodInvocation m = entry.getValue();
			if(m.getExpression() != null && entry.getKey().equals(unit)) {
				MethodDeclaration actualMD = ASTNodes.findParent(m.getExpression(), MethodDeclaration.class).get();
				if(!(actualMD.equals(outerMethod)) && outerMethod != null) {
					ASTNode newNode = unit.copyNode(actualMD.getParent());
					RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
					newUnit.setWorker("methodInvocationsMap");
					set.add(newUnit);	
				}
				outerMethod = actualMD;
			}	
		}
		return set;
	}

}
