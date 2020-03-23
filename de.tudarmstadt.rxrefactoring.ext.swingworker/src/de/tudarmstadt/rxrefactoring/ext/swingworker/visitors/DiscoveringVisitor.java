package de.tudarmstadt.rxrefactoring.ext.swingworker.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.core.utils.RelevantInvocation;
import de.tudarmstadt.rxrefactoring.core.utils.Types;

/**
 * Description: This visitor collects different {@link ASTNode} types and add
 * them to lists. Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class DiscoveringVisitor extends UnitASTVisitor {

	private final String classBinaryName;
	private final List<TypeDeclaration> typeDeclarations;
	private final List<FieldDeclaration> fieldDeclarations;
	private final List<Assignment> assignments;
	private final List<VariableDeclarationStatement> varDeclStatements;
	private final List<SimpleName> simpleNames;
	private final List<ClassInstanceCreation> classInstanceCreations;
	private final List<SingleVariableDeclaration> singleVarDeclarations;
	private final List<MethodDeclaration> methodDeclarations;
	private final List<MethodInvocation> methodInvocations;
	private final List<RelevantInvocation> relevantInvocations;

	public DiscoveringVisitor(String classBinaryName) {
		this.classBinaryName = classBinaryName;
		typeDeclarations = new ArrayList<>();
		assignments = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
		methodInvocations = new ArrayList<>();
		varDeclStatements = new ArrayList<>();
		simpleNames = new ArrayList<>();
		classInstanceCreations = new ArrayList<>();
		singleVarDeclarations = new ArrayList<>();
		methodDeclarations = new ArrayList<>();
		relevantInvocations = Lists.newLinkedList();
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (Types.isTypeOf(type, classBinaryName)) {
			fieldDeclarations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		Expression leftHandSide = node.getLeftHandSide();
		ITypeBinding type = leftHandSide.resolveTypeBinding();
		if (Types.isTypeOf(type, classBinaryName)) {
			assignments.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (Types.isTypeOf(type, classBinaryName)) {
			varDeclStatements.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		ITypeBinding typeBinding = simpleName.resolveTypeBinding();
		IBinding iBinding = simpleName.resolveBinding();
		if (iBinding != null) {
			int kind = iBinding.getKind();
			if (Types.isTypeOf(typeBinding, classBinaryName) && kind == IBinding.VARIABLE) {
				if (!simpleNames.contains(simpleName)) {
					simpleNames.add(simpleName);
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (Types.isTypeOf(type, classBinaryName)) {
			classInstanceCreations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (Types.isTypeOf(node.resolveBinding(), classBinaryName)) {
			typeDeclarations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		IMethodBinding binding = node.resolveBinding();
		if (binding != null) {
			ITypeBinding returnType = binding.getReturnType();

			if (Types.isTypeOf(returnType, classBinaryName))
				methodDeclarations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding != null) {
			ITypeBinding type = binding.getDeclaringClass();

			if (Types.isTypeOf(type, classBinaryName)) {
				methodInvocations.add(node);
			}

			if (Methods.hasSignature(binding, "java.util.concurrent.Executor", "execute", "java.lang.Runnable")) {
				relevantInvocations.add(new RelevantInvocation(node));
			}
		}

		for (Object arg : node.arguments()) {
			if (arg instanceof SimpleName) {
				SimpleName simpleName = (SimpleName) arg;
				ITypeBinding argType = simpleName.resolveTypeBinding();
				if (Types.isTypeOf(argType, classBinaryName)) {
					if (!simpleNames.contains(simpleName)) {
						simpleNames.add(simpleName);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (Types.isTypeOf(type, classBinaryName)) {
			singleVarDeclarations.add(node);
		}
		return true;
	}
	
	/*public <T> List<T> getList(Class<T> c){
		if(c.isInstance(TypeDeclaration.class))
			return (List<T>) typeDeclarations;
		if(c.isInstance(FieldDeclaration.class))
			return (List<T>) fieldDeclarations;
		if(c.isInstance(VariableDeclarationStatement.class))
			return (List<T>) varDeclStatements;
		if(c.isInstance(SingleVariableDeclaration.class))
			return (List<T>) singleVarDeclarations;
		if(c.isInstance(Assignment.class))
			return (List<T>) assignments;
		if(c.isInstance(SimpleName.class))
			return (List<T>) simpleNames;
		if(c.isInstance(ClassInstanceCreation.class))
			return (List<T>) classInstanceCreations;
		if(c.isInstance(MethodInvocation.class))
			return (List<T>) methodInvocations;
		if(c.isInstance(MethodDeclaration.class))
			return (List<T>) methodDeclarations;
		if(c.isInstance(RelevantInvocation.class))
			return (List<T>) relevantInvocations;		
		
		return null;
	}*/
	
	public List<TypeDeclaration> getTypeDeclarations() {
		return typeDeclarations;
	}

	public List<FieldDeclaration> getFieldDeclarations() {
		return fieldDeclarations;
	}

	public List<Assignment> getAssignments() {
		return assignments;
	}

	public List<VariableDeclarationStatement> getVarDeclStatements() {
		return varDeclStatements;
	}

	public List<SimpleName> getSimpleNames() {
		return simpleNames;
	}

	public List<ClassInstanceCreation> getClassInstanceCreations() {
		return classInstanceCreations;
	}

	public List<SingleVariableDeclaration> getSingleVarDeclarations() {
		return singleVarDeclarations;
	}

	public List<MethodInvocation> getMethodInvocations() {
		return methodInvocations;
	}

	public List<MethodDeclaration> getMethodDeclarations() {
		return methodDeclarations;
	}

	public List<RelevantInvocation> getRelevantInvocations() {
		return relevantInvocations;
	}
	
	public void cleanAllLists() {
		this.typeDeclarations.clear();
		this.fieldDeclarations.clear();
		this.assignments.clear();
		this.varDeclStatements.clear();
		this.simpleNames.clear();
		this.classInstanceCreations.clear();
		this.singleVarDeclarations.clear();
		this.methodInvocations.clear();
		this.methodDeclarations.clear();
		this.relevantInvocations.clear();
		
	}
}


