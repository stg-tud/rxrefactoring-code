package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.ArrayList;
import java.util.List;

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
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

public class WorkerMapsUtils {

	private static final Multimap<String, Multimap<IRewriteCompilationUnit, ? extends ASTNode>> allWorkerMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, TypeDeclaration> typeDeclMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, FieldDeclaration> fieldDeclMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, Assignment> assigmentsMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> varDeclMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, SimpleName> simpleNamesMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, ClassInstanceCreation> classInstanceMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> singleVarDeclMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, MethodInvocation> methodInvocationsMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, MethodDeclaration> methodDeclarationsMap = HashMultimap.create();
	private static final Multimap<IRewriteCompilationUnit, MethodInvocation> relevantInvocationsMap = HashMultimap.create();

	

	public static void fillAllWorkerMap() {
		allWorkerMap.put("Variable Declarations", varDeclMap);
		allWorkerMap.put("Type Declarations", typeDeclMap);
		allWorkerMap.put("Field Declarations", fieldDeclMap);
		allWorkerMap.put("Assigments", assigmentsMap);
		allWorkerMap.put("Simple Names", simpleNamesMap);
		allWorkerMap.put("Class Instances", classInstanceMap);
		allWorkerMap.put("Single Variable Declarations", singleVarDeclMap);
		allWorkerMap.put("Method Invocations", methodInvocationsMap);
		allWorkerMap.put("Method Declarations", methodDeclarationsMap);
		allWorkerMap.put("Relevant Invocations", relevantInvocationsMap);

	}

	public static List getNeededList(String key, DiscoveringVisitor visitor) {
		switch (key) {
		case "Variable Declarations":
			return visitor.getVarDeclStatements();
		case "Type Declarations":
			return visitor.getTypeDeclarations();
		case "Field Declarations":
			return visitor.getFieldDeclarations();
		case "Assigments":
			return visitor.getAssignments();
		case "Simple Names":
			return visitor.getSimpleNames();
		case "Class Instances":
			return visitor.getClassInstanceCreations();
		case "Single Variable Declarations":
			return visitor.getSingleVarDeclarations();
		case "Method Invocations":
			return visitor.getMethodInvocations();
		case "Method Declarations":
			return visitor.getMethodDeclarations();
		case "Relevant Invocations":
			return visitor.getRelevantInvocations();
		default:
			throw new IllegalStateException("Key not in different Maps!");
		}
	}
	
	public String getDetails() {
		return "Nr. files: " + allWorkerMap.size() + "\n" + "TypeDeclarations = "
				+ typeDeclMap.values().size() + "\n" + "FieldDeclarations = " + fieldDeclMap.values().size() + "\n"
				+ "Assignments = " + assigmentsMap.values().size() + "\n" + "VariableDeclarationStatements = "
				+ varDeclMap.values().size() + "\n" + "SimpleNames = " + simpleNamesMap.values().size() + "\n"
				+ "ClassInstanceCreations = " + classInstanceMap.values().size() + "\n"
				+ "SingleVariableDeclarations = " + singleVarDeclMap.values().size() + "\n" + "MethodInvocations = "
				+ methodInvocationsMap.values().size() + "\n" + "MethodDeclarations = "
				+ methodDeclarationsMap.values().size();
	}
	
	public static Multimap<String, Multimap<IRewriteCompilationUnit, ? extends ASTNode>> getAllWorkerMap() {
		return allWorkerMap;
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

	public static Multimap<IRewriteCompilationUnit, MethodInvocation> getRelevantInvocations() {
		return relevantInvocationsMap;
	}

}
