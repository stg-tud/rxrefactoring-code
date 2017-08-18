package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;

public class CollectorGroup {
	private final Map<RewriteCompilationUnit, List<TypeDeclaration>> typeDeclMap;
	private final Map<RewriteCompilationUnit, List<FieldDeclaration>> fieldDeclMap;
	private final Map<RewriteCompilationUnit, List<Assignment>> assigmentsMap;
	private final Map<RewriteCompilationUnit, List<VariableDeclarationStatement>> varDeclMap;
	private final Map<RewriteCompilationUnit, List<SimpleName>> simpleNamesMap;
	private final Map<RewriteCompilationUnit, List<ClassInstanceCreation>> classInstanceMap;
	private final Map<RewriteCompilationUnit, List<SingleVariableDeclaration>> singleVarDeclMap;
	private final Map<RewriteCompilationUnit, List<MethodInvocation>> methodInvocationsMap;
	private final Map<RewriteCompilationUnit, List<MethodDeclaration>> methodDeclarationsMap;
	private final Map<RewriteCompilationUnit, List<ArrayCreation>> arrayCreationsMap;
	private final Map<RewriteCompilationUnit, List<ReturnStatement>> returnStatementsMap; 
	
	public CollectorGroup() {

		typeDeclMap = new HashMap<>();
		fieldDeclMap = new HashMap<>();
		assigmentsMap = new HashMap<>();
		varDeclMap = new HashMap<>();
		simpleNamesMap = new HashMap<>();
		classInstanceMap = new HashMap<>();
		singleVarDeclMap = new HashMap<>();
		methodInvocationsMap = new HashMap<>();
		methodDeclarationsMap = new HashMap<>();
		arrayCreationsMap = new HashMap<>();
		returnStatementsMap = new HashMap<>();
	}
	
	public void add(RewriteCompilationUnit cu, VisitorNodes subclasses) {
		addToMap(cu, subclasses.getTypeDeclarations(), typeDeclMap);
		addToMap(cu, subclasses.getFieldDeclarations(), fieldDeclMap);
		addToMap(cu, subclasses.getAssignments(), assigmentsMap);
		addToMap(cu, subclasses.getVarDeclStatements(), varDeclMap);
		addToMap(cu, subclasses.getSimpleNames(), simpleNamesMap);
		addToMap(cu, subclasses.getClassInstanceCreations(), classInstanceMap);
		addToMap(cu, subclasses.getSingleVarDeclarations(), singleVarDeclMap);
		addToMap(cu, subclasses.getMethodInvocations(), methodInvocationsMap);
		addToMap(cu, subclasses.getMethodDeclarations(), methodDeclarationsMap);
		addToMap(cu, subclasses.getArrayCreations(), arrayCreationsMap);
		addToMap(cu, subclasses.getReturnStatements(), returnStatementsMap);
	}
	
	private <T> void addToMap(RewriteCompilationUnit cu, List<T> newList, Map<RewriteCompilationUnit, List<T>> map) {
		if (newList.isEmpty() || map == null) {
			return;
		}
		
		List<T> currentList = map.get(cu);
		if (currentList == null) {
			map.put(cu, newList);
		} else {
			currentList.addAll(newList);
		}
	}
	
	public Map<RewriteCompilationUnit, List<TypeDeclaration>> getTypeDeclMap() {
		return typeDeclMap;
	}

	public Map<RewriteCompilationUnit, List<FieldDeclaration>> getFieldDeclMap() {
		return fieldDeclMap;
	}

	public Map<RewriteCompilationUnit, List<Assignment>> getAssigmentsMap() {
		return assigmentsMap;
	}

	public Map<RewriteCompilationUnit, List<VariableDeclarationStatement>> getVarDeclMap() {
		return varDeclMap;
	}

	public Map<RewriteCompilationUnit, List<SimpleName>> getSimpleNamesMap() {
		return simpleNamesMap;
	}

	public Map<RewriteCompilationUnit, List<ClassInstanceCreation>> getClassInstanceMap() {
		return classInstanceMap;
	}

	public Map<RewriteCompilationUnit, List<SingleVariableDeclaration>> getSingleVarDeclMap() {
		return singleVarDeclMap;
	}

	public Map<RewriteCompilationUnit, List<MethodInvocation>> getMethodInvocationsMap() {
		return methodInvocationsMap;
	}

	public Map<RewriteCompilationUnit, List<MethodDeclaration>> getMethodDeclarationsMap() {
		return methodDeclarationsMap;
	}
	
	public Map<RewriteCompilationUnit, List<ArrayCreation>> getArrayCreationsMap() {
		return arrayCreationsMap;
	}
	
	public Map<RewriteCompilationUnit, List<ReturnStatement>> getReturnStatementsMap() {
		return returnStatementsMap;
	}
	
	public Set<RewriteCompilationUnit> getCompilationUnits() {
		Set<RewriteCompilationUnit> allCompilationUnits = new HashSet<>();

		allCompilationUnits.addAll(typeDeclMap.keySet());
		allCompilationUnits.addAll(fieldDeclMap.keySet());
		allCompilationUnits.addAll(assigmentsMap.keySet());
		allCompilationUnits.addAll(varDeclMap.keySet());
		allCompilationUnits.addAll(simpleNamesMap.keySet());
		allCompilationUnits.addAll(classInstanceMap.keySet());
		allCompilationUnits.addAll(singleVarDeclMap.keySet());
		allCompilationUnits.addAll(methodInvocationsMap.keySet());
		allCompilationUnits.addAll(methodDeclarationsMap.keySet());
		allCompilationUnits.addAll(arrayCreationsMap.keySet());
		allCompilationUnits.addAll(returnStatementsMap.keySet());

		return allCompilationUnits;
	}

	public int getNumberOfCompilationUnits() {
		return getCompilationUnits().size();
	}
	
	public Map<String, Integer> getMapsResults() {
		HashMap<String, Integer> map = new HashMap<>();

		map.put("TypeDeclarations", typeDeclMap.values().size());
		map.put("FieldDeclarations", fieldDeclMap.values().size());
		map.put("Assignments", assigmentsMap.values().size());
		map.put("VariableDeclarationStatements", varDeclMap.values().size());
		map.put("SimpleNames", simpleNamesMap.values().size());
		map.put("ClassInstanceCreations", classInstanceMap.values().size());
		map.put("SingleVariableDeclarations", singleVarDeclMap.values().size());
		map.put("MethodInvocations", methodInvocationsMap.values().size());
		map.put("MethodDeclarations", methodDeclarationsMap.values().size());
		map.put("ArrayCreations", arrayCreationsMap.values().size());
		map.put("ReturnStatements", returnStatementsMap.values().size());

		return map;
	}
}
