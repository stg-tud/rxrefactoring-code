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
import de.tudarmstadt.rxrefactoring.core.utils.WorkerUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

public class WorkerMapsUtils extends WorkerUtils{
	
	public static List<VariableDeclarationStatement> getNeededList(VariableDeclarationStatement t,
			DiscoveringVisitor visitor) {
		return visitor.getVarDeclStatements();
	}

	public static List<TypeDeclaration> getNeededList(TypeDeclaration t, DiscoveringVisitor visitor) {
		return visitor.getTypeDeclarations();
	}

	public static List<FieldDeclaration> getNeededList(FieldDeclaration t, DiscoveringVisitor visitor) {
		return visitor.getFieldDeclarations();
	}

	public static List<Assignment> getNeededList(Assignment t, DiscoveringVisitor visitor) {
		return visitor.getAssignments();
	}

	public static List<SimpleName> getNeededList(SimpleName t, DiscoveringVisitor visitor) {
		return visitor.getSimpleNames();
	}

	public static List<SingleVariableDeclaration> getNeededList(SingleVariableDeclaration t,
			DiscoveringVisitor visitor) {
		return visitor.getSingleVarDeclarations();
	}

	public static List<MethodInvocation> getNeededList(MethodInvocation t, DiscoveringVisitor visitor) {
		return visitor.getMethodInvocations();
	}

	public static List<MethodDeclaration> getNeededList(MethodDeclaration t, DiscoveringVisitor visitor) {
		return visitor.getMethodDeclarations();
	}

	public static List<RelevantInvocation> getNeededList(RelevantInvocation t, DiscoveringVisitor visitor) {
		return visitor.getRelevantInvocations();
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


}
