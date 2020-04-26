package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;

public class CollectorGroup {
	private final Multimap<IRewriteCompilationUnit, TypeDeclaration> typeDeclMap;
	private final Multimap<IRewriteCompilationUnit, FieldDeclaration> fieldDeclMap;
	private final Multimap<IRewriteCompilationUnit, Assignment> assigmentsMap;
	private final Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> varDeclMap;
	private final Multimap<IRewriteCompilationUnit, SimpleName> simpleNamesMap;
	private final Multimap<IRewriteCompilationUnit, ClassInstanceCreation> classInstanceMap;
	private final Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> singleVarDeclMap;
	private final Multimap<IRewriteCompilationUnit, MethodInvocation> methodInvocationsMap;
	private final Multimap<IRewriteCompilationUnit, MethodDeclaration> methodDeclarationsMap;
	private final Multimap<IRewriteCompilationUnit, ArrayCreation> arrayCreationsMap;
	private final Multimap<IRewriteCompilationUnit, ReturnStatement> returnStatementsMap;

	public CollectorGroup() {

		typeDeclMap = HashMultimap.create();
		fieldDeclMap = HashMultimap.create();
		assigmentsMap = HashMultimap.create();
		varDeclMap = HashMultimap.create();
		simpleNamesMap = HashMultimap.create();
		classInstanceMap = HashMultimap.create();
		singleVarDeclMap = HashMultimap.create();
		methodInvocationsMap = HashMultimap.create();
		methodDeclarationsMap = HashMultimap.create();
		arrayCreationsMap = HashMultimap.create();
		returnStatementsMap = HashMultimap.create();
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

	@SuppressWarnings("unchecked")
	private <T> void addToMap(IRewriteCompilationUnit cu, List<T> newList, Multimap<IRewriteCompilationUnit, T> map) {
		if (newList.isEmpty() || map == null) {
			return;
		}

		Collection<T> currentList = map.get(cu);
		if (currentList == null) {
			map.put(cu, (T) newList);
		} else {
			currentList.addAll(newList);
		}
	}
	
	public Multimap<IRewriteCompilationUnit, ? extends ASTNode> findMapToIdentifier(WorkerIdentifier identifier) {
		
		if(identifier.equals(NamingUtils.ARRAY_CREATION_IDENTIFIER))
			return arrayCreationsMap;
		if(identifier.equals(NamingUtils.ASSIGNMENTS_IDENTIFIER))
			return assigmentsMap;
		if(identifier.equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER))
			return classInstanceMap;
		if(identifier.equals(NamingUtils.FIELD_DECLARATION_IDENTIFIER))
			return fieldDeclMap;
		if(identifier.equals(NamingUtils.METHOD_DECLARATION_IDENTIFIER))
			return methodDeclarationsMap;
		if(identifier.equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER))
			return methodInvocationsMap;
		if(identifier.equals(NamingUtils.RETURN_STATEMENT_IDENTIFIER))
			return returnStatementsMap;
		if(identifier.equals(NamingUtils.SIMPLE_NAME_IDENTIFIER))
			return simpleNamesMap;
		if(identifier.equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER))
			return singleVarDeclMap;
		if(identifier.equals(NamingUtils.TYPE_DECL_IDENTIFIER))
			return typeDeclMap;
		if(identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER))
			return varDeclMap;
		
		return null;
		
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

	public Multimap<IRewriteCompilationUnit, ArrayCreation> getArrayCreationsMap() {
		return arrayCreationsMap;
	}

	public Multimap<IRewriteCompilationUnit, ReturnStatement> getReturnStatementsMap() {
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
	
	public void clearAllMaps() {
		typeDeclMap.clear();
		fieldDeclMap.clear();
		assigmentsMap.clear();
		varDeclMap.clear();
		simpleNamesMap.clear();
		classInstanceMap.clear();
		singleVarDeclMap.clear();
		methodInvocationsMap.clear();
		methodDeclarationsMap.clear();
		arrayCreationsMap.clear();
		returnStatementsMap.clear();
		
	}
	
	public void addElementsCollectorGroup(CollectorGroup groupToMerge) {
		
		typeDeclMap.putAll(groupToMerge.getTypeDeclMap());
		fieldDeclMap.putAll(groupToMerge.getFieldDeclMap());
		assigmentsMap.putAll(groupToMerge.getAssigmentsMap());
		varDeclMap.putAll(groupToMerge.getVarDeclMap());
		simpleNamesMap.putAll(groupToMerge.getSimpleNamesMap());
		classInstanceMap.putAll(groupToMerge.getClassInstanceMap());
		singleVarDeclMap.putAll(groupToMerge.getSingleVarDeclMap());
		methodInvocationsMap.putAll(groupToMerge.getMethodInvocationsMap());
		methodDeclarationsMap.putAll(groupToMerge.getMethodDeclarationsMap());
		arrayCreationsMap.putAll(groupToMerge.getArrayCreationsMap());
		returnStatementsMap.putAll(groupToMerge.getReturnStatementsMap());
		
		
	}
}
