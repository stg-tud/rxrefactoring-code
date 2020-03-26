
package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RelevantInvocation;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;

public class WorkerUtils {

	private static final List<WorkerIdentifier> allWorkerIdentifier = new ArrayList<WorkerIdentifier>();
	private static final Multimap<WorkerIdentifier, Multimap<IRewriteCompilationUnit, ?>> allMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, TypeDeclaration> typeDeclMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, FieldDeclaration> fieldDeclMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, Assignment> assigmentsMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> varDeclMap = HashMultimap
			.create();
	private static final Multimap<IRewriteCompilationUnit, SimpleName> simpleNamesMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, ClassInstanceCreation> classInstanceMap = HashMultimap
			.create();
	private static final Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> singleVarDeclMap = HashMultimap
			.create();
	private static final Multimap<IRewriteCompilationUnit, MethodInvocation> methodInvocationsMap = HashMultimap
			.create();
	private static final Multimap<IRewriteCompilationUnit, MethodDeclaration> methodDeclarationsMap = HashMultimap
			.create();
	private static final Multimap<IRewriteCompilationUnit, RelevantInvocation> relevantInvocationsMap = HashMultimap
			.create();

	public static void fillAllWorkerIdentifier() {
		allWorkerIdentifier.add(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.TYPE_DECL_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.FIELD_DECLARATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.ASSIGNMENTS_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.SIMPLE_NAME_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.METHOD_INVOCATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.METHOD_DECLARATION_IDENTIFIER);
		allWorkerIdentifier.add(NamingUtils.RELEVANT_INVOCATION_IDENTIFIER);
	}

	public static void fillAllMap() {
		allMap.put(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER, varDeclMap);
		allMap.put(NamingUtils.TYPE_DECL_IDENTIFIER, typeDeclMap);
		allMap.put(NamingUtils.FIELD_DECLARATION_IDENTIFIER, fieldDeclMap);
		allMap.put(NamingUtils.ASSIGNMENTS_IDENTIFIER, assigmentsMap);
		allMap.put(NamingUtils.SIMPLE_NAME_IDENTIFIER, simpleNamesMap);
		allMap.put(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER, classInstanceMap);
		allMap.put(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER, singleVarDeclMap);
		allMap.put(NamingUtils.METHOD_INVOCATION_IDENTIFIER, methodInvocationsMap);
		allMap.put(NamingUtils.METHOD_DECLARATION_IDENTIFIER, methodDeclarationsMap);
		allMap.put(NamingUtils.RELEVANT_INVOCATION_IDENTIFIER, relevantInvocationsMap);

	}

	
	/*public static String getNameToClassOfWorker(Class c) {
		for(Key k: allWorkerIdentifier)	{
			if(k.getClass().equals(c));
				return k.name;
		}
		 return null;
	}*/
	

	public String getDetails() {
		return "Nr. files: " + allWorkerIdentifier.size() + "\n" + "TypeDeclarations = " + typeDeclMap.values().size() + "\n"
				+ "FieldDeclarations = " + fieldDeclMap.values().size() + "\n" + "Assignments = "
				+ assigmentsMap.values().size() + "\n" + "VariableDeclarationStatements = " + varDeclMap.values().size()
				+ "\n" + "SimpleNames = " + simpleNamesMap.values().size() + "\n" + "ClassInstanceCreations = "
				+ classInstanceMap.values().size() + "\n" + "SingleVariableDeclarations = "
				+ singleVarDeclMap.values().size() + "\n" + "MethodInvocations = "
				+ methodInvocationsMap.values().size() + "\n" + "MethodDeclarations = "
				+ methodDeclarationsMap.values().size();
	}

	public static List<WorkerIdentifier> getAllIdentifier() {
		return allWorkerIdentifier;
	}

	public static Multimap<WorkerIdentifier, Multimap<IRewriteCompilationUnit, ?>> getAllMap() {
		return allMap;
	}

	public static Multimap<IRewriteCompilationUnit, TypeDeclaration> getTypeDeclMap() {
		return typeDeclMap;
	}

	public static Multimap<IRewriteCompilationUnit, FieldDeclaration> getFieldDeclMap() {
		return fieldDeclMap;
	}

	public static Multimap<IRewriteCompilationUnit, Assignment> getAssigmentsMap() {
		return assigmentsMap;
	}

	public static Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> getVarDeclMap() {
		return varDeclMap;
	}

	public static Multimap<IRewriteCompilationUnit, SimpleName> getSimpleNamesMap() {
		return simpleNamesMap;
	}

	public static Multimap<IRewriteCompilationUnit, ClassInstanceCreation> getClassInstanceMap() {
		return classInstanceMap;
	}

	public static Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> getSingleVarDeclMap() {
		return singleVarDeclMap;
	}

	public static Multimap<IRewriteCompilationUnit, MethodInvocation> getMethodInvocationsMap() {
		return methodInvocationsMap;
	}

	public static Multimap<IRewriteCompilationUnit, MethodDeclaration> getMethodDeclarationsMap() {
		return methodDeclarationsMap;
	}

	public static Multimap<IRewriteCompilationUnit, RelevantInvocation> getRelevantInvocations() {
		return relevantInvocationsMap;
	}
	
	public static void clearAllMaps() {
		allWorkerIdentifier.clear();
		allMap.clear();
		typeDeclMap.clear();
		fieldDeclMap.clear();
		assigmentsMap.clear();
		varDeclMap.clear();
		simpleNamesMap.clear();
		classInstanceMap.clear();
		singleVarDeclMap.clear();
		methodInvocationsMap.clear();
		methodDeclarationsMap.clear();
		relevantInvocationsMap.clear();
	}
	
	/*public static Multimap getListToWorkerName(String key) {
		switch (key) {
		case "Variable Declarations":
			return varDeclMap;
		case "Type Declarations":
			return typeDeclMap;
		case "Field Declarations":
			return fieldDeclMap;
		case "Assigments":
			return assigmentsMap;
		case "Simple Names":
			return simpleNamesMap;
		case "Class Instances":
			return classInstanceMap;
		case "Single Variable Declarations":
			return singleVarDeclMap;
		case "Method Invocations":
			return methodInvocationsMap;
		case "Method Declarations":
			return methodDeclarationsMap;
		case "Relevant Invocations":
			return relevantInvocationsMap;
		default:
			throw new IllegalStateException("Key not in different Maps!");
		}
	}*/
	
	public static void clearKeys() {
		allWorkerIdentifier.clear();
	}
}
