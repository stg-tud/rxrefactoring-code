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

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;

public class CollectorGroup {
	private final Map<IRewriteCompilationUnit, List<TypeDeclaration>> typeDeclMap;
	private final Map<IRewriteCompilationUnit, List<FieldDeclaration>> fieldDeclMap;
	private final Map<IRewriteCompilationUnit, List<Assignment>> assigmentsMap;
	private final Map<IRewriteCompilationUnit, List<VariableDeclarationStatement>> varDeclMap;
	private final Map<IRewriteCompilationUnit, List<SimpleName>> simpleNamesMap;
	private final Map<IRewriteCompilationUnit, List<ClassInstanceCreation>> classInstanceMap;
	private final Map<IRewriteCompilationUnit, List<SingleVariableDeclaration>> singleVarDeclMap;
	private final Map<IRewriteCompilationUnit, List<MethodInvocation>> methodInvocationsMap;
	private final Map<IRewriteCompilationUnit, List<MethodDeclaration>> methodDeclarationsMap;
	private final Map<IRewriteCompilationUnit, List<ArrayCreation>> arrayCreationsMap;
	private final Map<IRewriteCompilationUnit, List<ReturnStatement>> returnStatementsMap;

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

	public void add(IRewriteCompilationUnit cu, VisitorNodes subclasses) {
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

	private <T> void addToMap(IRewriteCompilationUnit cu, List<T> newList, Map<IRewriteCompilationUnit, List<T>> map) {
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

	public Map<IRewriteCompilationUnit, List<TypeDeclaration>> getTypeDeclMap() {
		return typeDeclMap;
	}

	public Map<IRewriteCompilationUnit, List<FieldDeclaration>> getFieldDeclMap() {
		return fieldDeclMap;
	}

	public Map<IRewriteCompilationUnit, List<Assignment>> getAssigmentsMap() {
		return assigmentsMap;
	}

	public Map<IRewriteCompilationUnit, List<VariableDeclarationStatement>> getVarDeclMap() {
		return varDeclMap;
	}

	public Map<IRewriteCompilationUnit, List<SimpleName>> getSimpleNamesMap() {
		return simpleNamesMap;
	}

	public Map<IRewriteCompilationUnit, List<ClassInstanceCreation>> getClassInstanceMap() {
		return classInstanceMap;
	}

	public Map<IRewriteCompilationUnit, List<SingleVariableDeclaration>> getSingleVarDeclMap() {
		return singleVarDeclMap;
	}

	public Map<IRewriteCompilationUnit, List<MethodInvocation>> getMethodInvocationsMap() {
		return methodInvocationsMap;
	}

	public Map<IRewriteCompilationUnit, List<MethodDeclaration>> getMethodDeclarationsMap() {
		return methodDeclarationsMap;
	}

	public Map<IRewriteCompilationUnit, List<ArrayCreation>> getArrayCreationsMap() {
		return arrayCreationsMap;
	}

	public Map<IRewriteCompilationUnit, List<ReturnStatement>> getReturnStatementsMap() {
		return returnStatementsMap;
	}

	public Set<IRewriteCompilationUnit> getCompilationUnits() {
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
