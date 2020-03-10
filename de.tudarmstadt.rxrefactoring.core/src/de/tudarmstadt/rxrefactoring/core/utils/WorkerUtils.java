
package de.tudarmstadt.rxrefactoring.core.utils;

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

public class WorkerUtils {

	private static final List<Key> allKeys = new ArrayList<Key>();
	private static final Multimap<String, Multimap<IRewriteCompilationUnit, ?>> allMap = HashMultimap.create();
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

	public static void fillAllKeys() {
		allKeys.add(new Key("Variable Declaration Statements", VariableDeclarationStatement.class));
		allKeys.add(new Key("Type Declarations", TypeDeclaration.class));
		allKeys.add(new Key("Field Declarations", FieldDeclaration.class));
		allKeys.add(new Key("Assignments", Assignment.class));
		allKeys.add(new Key("Simple Names", SimpleName.class));
		allKeys.add(new Key("Class Instances", ClassInstanceCreation.class));
		allKeys.add(new Key("Single Variable Declarations", SingleVariableDeclaration.class));
		allKeys.add(new Key("Method Invocations", MethodInvocation.class));
		allKeys.add(new Key("Method Declarations", MethodDeclaration.class));
		allKeys.add(new Key("Relevant Invocations", RelevantInvocation.class));

		/*
		 * allKeys.put(new Key("Variable Declarations", VariableDeclaration.class),
		 * varDeclMap); allKeys.put(new Key("Type Declarations", TypeDeclaration.class),
		 * typeDeclMap); allKeys.put(new Key("Field Declarations",
		 * FieldDeclaration.class), fieldDeclMap); allKeys.put(new Key("Assigments",
		 * Assignment.class), assigmentsMap); allKeys.put(new Key("Simple Names",
		 * SimpleName.class), simpleNamesMap); allKeys.put(new Key("Class Instances",
		 * ClassInstanceCreation.class), classInstanceMap); allKeys.put(new
		 * Key("Single Variable Declarations", SingleVariableDeclaration.class),
		 * singleVarDeclMap); allKeys.put(new Key("Method Invocations",
		 * MethodInvocation.class), methodInvocationsMap); allKeys.put(new
		 * Key("Method Declarations", MethodDeclaration.class), methodDeclarationsMap);
		 * allKeys.put(new Key("Relevant Invocations", RelevantInvocation.class),
		 * relevantInvocationsMap);
		 */

	}

	public static void fillAllMap() {
		allMap.put("Variable Declarations", varDeclMap);
		allMap.put("Type Declarations", typeDeclMap);
		allMap.put("Field Declarations", fieldDeclMap);
		allMap.put("Assigments", assigmentsMap);
		allMap.put("Simple Names", simpleNamesMap);
		allMap.put("Class Instances", classInstanceMap);
		allMap.put("Single Variable Declarations", singleVarDeclMap);
		allMap.put("Method Invocations", methodInvocationsMap);
		allMap.put("Method Declarations", methodDeclarationsMap);
		allMap.put("Relevant Invocations", relevantInvocationsMap);

	}

	

	

	public String getDetails() {
		return "Nr. files: " + allKeys.size() + "\n" + "TypeDeclarations = " + typeDeclMap.values().size() + "\n"
				+ "FieldDeclarations = " + fieldDeclMap.values().size() + "\n" + "Assignments = "
				+ assigmentsMap.values().size() + "\n" + "VariableDeclarationStatements = " + varDeclMap.values().size()
				+ "\n" + "SimpleNames = " + simpleNamesMap.values().size() + "\n" + "ClassInstanceCreations = "
				+ classInstanceMap.values().size() + "\n" + "SingleVariableDeclarations = "
				+ singleVarDeclMap.values().size() + "\n" + "MethodInvocations = "
				+ methodInvocationsMap.values().size() + "\n" + "MethodDeclarations = "
				+ methodDeclarationsMap.values().size();
	}

	public static List<Key> getAllKeys() {
		return allKeys;
	}

	public static Multimap<String, Multimap<IRewriteCompilationUnit, ?>> getAllMap() {
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
		allKeys.clear();
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
	
	public static void clearKeys() {
		allKeys.clear();
	}

	public static class Key {
		public String name;
		public Class c;

		Key(String name, Class c) {
			this.name = name;
			this.c = c;
		}
	}

}
