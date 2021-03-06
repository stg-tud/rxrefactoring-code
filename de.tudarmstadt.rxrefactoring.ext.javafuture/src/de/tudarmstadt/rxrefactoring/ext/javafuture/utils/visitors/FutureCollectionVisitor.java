package de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.CollectionInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.VisitorNodes;

/**
 * Search for occurrences of lists of Futures
 * 
 */
public class FutureCollectionVisitor extends ASTVisitor implements VisitorNodes {

	private final String classBinaryName;
	private final String[] collectionBinaryNames = CollectionInfo.getBinaryNames();

	private final List<TypeDeclaration> typeDeclarations;
	private final List<FieldDeclaration> fieldDeclarations;
	private final List<Assignment> assignments;
	private final List<VariableDeclarationStatement> varDeclStatements;
	private final List<SimpleName> simpleNames;
	private final List<ClassInstanceCreation> classInstanceCreations;
	private final List<SingleVariableDeclaration> singleVarDeclarations;
	private final List<MethodDeclaration> methodDeclarations;
	private final List<MethodInvocation> methodInvocations;
	private final List<ArrayCreation> arrayCreations;
	private final List<ReturnStatement> returnStatements;

	private MethodDeclaration currentMethod;
	private Map<ASTNode, MethodDeclaration> parentMethod;
	private Map<MethodDeclaration, Boolean> isMethodPure;

	private boolean insideAnonymousClass;

	private boolean methodRelevant;
	private boolean currentMethodPure;

	public FutureCollectionVisitor(String classBinaryName) {
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
		arrayCreations = new ArrayList<>();
		returnStatements = new ArrayList<>();

		parentMethod = new HashMap<>();
		isMethodPure = new HashMap<>();
	}

	private void setPurity(boolean isPure) {
		currentMethodPure = isPure;
	}

	private void addParent(ASTNode node) {
		if (currentMethod == null)
			return;

		methodRelevant = true;

		parentMethod.put(node, currentMethod);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		ITypeBinding type = node.getType().resolveBinding();

		if (Arrays.stream(collectionBinaryNames).allMatch(s -> Types.isExactTypeOf(type, s))
				|| node.getType() instanceof ArrayType) {
			ITypeBinding binding = ASTUtils.getTypeArgumentBinding(type, classBinaryName);

			if (binding != null) {
				fieldDeclarations.add(node);
			}
		}

		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		Expression leftHandSide = node.getLeftHandSide();

		if (leftHandSide.getNodeType() == Expression.ARRAY_ACCESS) {
			ITypeBinding type = leftHandSide.resolveTypeBinding();

			if (Types.isExactTypeOf(type, classBinaryName)) {
				addParent(node);
				assignments.add(node);
			}
		}

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding type = node.getType().resolveBinding();

		if (type == null)
			return true;

		// Check for collections
		if (Types.isTypeOf(type, collectionBinaryNames) || Types.isTypeOf(type, "java.util.Iterator")) {

			if (ASTUtils.getTypeArgumentBinding(type, classBinaryName) != null) {
				varDeclStatements.add(node);
				addParent(node);
			}
		} else if (type != null && type.isArray()) {
			// Array
			if (Types.isExactTypeOf(type.getElementType(), classBinaryName)) {
				varDeclStatements.add(node);
				addParent(node);
			}
		}

		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (Types.isTypeOf(type, collectionBinaryNames)) {
			if (ASTUtils.getTypeArgumentBinding(type, classBinaryName) != null) {
				classInstanceCreations.add(node);
				addParent(node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding iBinding = simpleName.resolveBinding();
		if (iBinding != null && iBinding.getKind() == IBinding.VARIABLE) {
			ITypeBinding typeBinding = simpleName.resolveTypeBinding();

			// collection
			if (Types.isTypeOf(typeBinding, collectionBinaryNames)
					&& ASTUtils.getTypeArgumentBinding(typeBinding, classBinaryName) != null) {
				addSimpleName(simpleName);
			} else if (typeBinding != null && typeBinding.isArray()
					&& Types.isExactTypeOf(typeBinding.getElementType(), classBinaryName)) {
				addSimpleName(simpleName);
			}
		}

		return true;
	}

	private void addSimpleName(SimpleName simpleName) {
		if (!simpleNames.contains(simpleName)) {
			simpleNames.add(simpleName);

			addParent(simpleName);

			if (ASTUtils.isField(simpleName)) {
				setPurity(false);
			}
		}
	}

	@Override
	public boolean visit(MethodDeclaration node) {

		if (!insideAnonymousClass) {
			currentMethod = node;
			setPurity(true);
		}

		IMethodBinding binding = node.resolveBinding();

		if (binding != null) {
			ITypeBinding returnType = binding.getReturnType();

			if (Types.isTypeOf(returnType, collectionBinaryNames)
					&& ASTUtils.getTypeArgumentBinding(returnType, classBinaryName) != null) {
				methodDeclarations.add(node);

				methodRelevant = true;
				setPurity(false);
			}
		}

		// go through the parameters to check them for 'purity'
		for (Object parameter : node.parameters()) {
			if (parameter instanceof SingleVariableDeclaration) {

				SingleVariableDeclaration singleVarDecl = (SingleVariableDeclaration) parameter;

				ITypeBinding type = singleVarDecl.getType().resolveBinding();
				if (Types.isExactTypeOf(type, classBinaryName)) {
					methodRelevant = true;
					setPurity(false);
					break;
				}
			}
		}

		return true;
	}

	@Override
	public void endVisit(MethodDeclaration node) {

		if (insideAnonymousClass)
			return;

		if (currentMethod != null && methodRelevant)
			isMethodPure.put(currentMethod, currentMethodPure);

		currentMethod = null;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding != null) {
			ITypeBinding type = binding.getDeclaringClass();
			if (Types.isTypeOf(type, collectionBinaryNames)
					&& ASTUtils.getTypeArgumentBinding(type, classBinaryName) != null) {
				methodInvocations.add(node);
				addParent(node);
			}
		}

		for (Object arg : node.arguments()) {
			if (arg instanceof SimpleName) {
				SimpleName simpleName = (SimpleName) arg;
				ITypeBinding argType = simpleName.resolveTypeBinding();
				if (Types.isTypeOf(argType, collectionBinaryNames)
						&& ASTUtils.getTypeArgumentBinding(argType, classBinaryName) != null) {

					setPurity(false);
					if (!simpleNames.contains(simpleName)) {
						simpleNames.add(simpleName);
						addParent(simpleName);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(ArrayCreation node) {

		ITypeBinding type = node.getType().getElementType().resolveBinding();

		if (Types.isExactTypeOf(type, classBinaryName)) {
			addParent(node);
			arrayCreations.add(node);
		}

		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {

		// If the method was added that means it returns a list of futures.
		if (methodDeclarations.contains(currentMethod)) {

			addParent(node);
			returnStatements.add(node);
		}

		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		insideAnonymousClass = true;

		return true;
	}

	@Override
	public void endVisit(AnonymousClassDeclaration node) {
		insideAnonymousClass = false;
	}

	@Override
	public List<TypeDeclaration> getTypeDeclarations() {

		return typeDeclarations;
	}

	@Override
	public List<FieldDeclaration> getFieldDeclarations() {
		return fieldDeclarations;
	}

	@Override
	public List<Assignment> getAssignments() {
		return assignments;
	}

	@Override
	public List<VariableDeclarationStatement> getVarDeclStatements() {
		return varDeclStatements;
	}

	@Override
	public List<SimpleName> getSimpleNames() {
		return simpleNames;
	}

	@Override
	public List<ClassInstanceCreation> getClassInstanceCreations() {
		return classInstanceCreations;
	}

	@Override
	public List<SingleVariableDeclaration> getSingleVarDeclarations() {
		return singleVarDeclarations;
	}

	@Override
	public List<MethodInvocation> getMethodInvocations() {
		return methodInvocations;
	}

	@Override
	public List<MethodDeclaration> getMethodDeclarations() {
		return methodDeclarations;
	}

	@Override
	public List<ArrayCreation> getArrayCreations() {
		return arrayCreations;
	}

	@Override
	public List<ReturnStatement> getReturnStatements() {
		return returnStatements;
	}

	@Override
	public Map<ASTNode, MethodDeclaration> getParentMethods() {
		return parentMethod;
	}

	@Override
	public Map<MethodDeclaration, Boolean> getIsMethodPures() {
		return isMethodPure;
	}
}
