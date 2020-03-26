package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


import de.tudarmstadt.rxrefactoring.core.utils.RelevantInvocation;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

public class WorkerMapsUtils extends WorkerUtils {

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

	public static List getNeededList(WorkerIdentifier identifier, DiscoveringVisitor visitor) {

		if (identifier.equals(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER))
			return visitor.getVarDeclStatements();
		else if (identifier.equals(NamingUtils.TYPE_DECL_IDENTIFIER))
			return visitor.getTypeDeclarations();
		else if (identifier.equals(NamingUtils.FIELD_DECLARATION_IDENTIFIER))
			return visitor.getFieldDeclarations();
		else if (identifier.equals(NamingUtils.ASSIGNMENTS_IDENTIFIER))
			return visitor.getAssignments();
		else if (identifier.equals(NamingUtils.SIMPLE_NAME_IDENTIFIER))
			return visitor.getSimpleNames();
		else if (identifier.equals(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER))
			return visitor.getClassInstanceCreations();
		else if (identifier.equals(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER))
			return visitor.getSingleVarDeclarations();
		else if (identifier.equals(NamingUtils.METHOD_INVOCATION_IDENTIFIER))
			return visitor.getMethodInvocations();
		else if (identifier.equals(NamingUtils.METHOD_DECLARATION_IDENTIFIER))
			return visitor.getMethodDeclarations();
		else if (identifier.equals(NamingUtils.RELEVANT_INVOCATION_IDENTIFIER))
			return visitor.getRelevantInvocations();
		else {
			throw new IllegalStateException("Key not in different Maps!");
		}
	}
}
