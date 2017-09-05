package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import java.util.List;
import java.util.Map;

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

public interface VisitorNodes {
	List<TypeDeclaration> getTypeDeclarations();
	List<FieldDeclaration> getFieldDeclarations();
	List<Assignment> getAssignments();
	List<VariableDeclarationStatement> getVarDeclStatements();
	List<SimpleName> getSimpleNames();
	List<ClassInstanceCreation> getClassInstanceCreations();
	List<SingleVariableDeclaration> getSingleVarDeclarations();
	List<MethodInvocation> getMethodInvocations();
	List<MethodDeclaration> getMethodDeclarations();
	List<ArrayCreation> getArrayCreations();
	List<ReturnStatement> getReturnStatements();
	
	Map<ASTNode, MethodDeclaration> getParentMethods();
	Map<MethodDeclaration, Boolean> getIsMethodPures();
}
