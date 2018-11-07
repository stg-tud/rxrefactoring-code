package de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
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
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.VisitorNodes;

public class FutureVisitor extends ASTVisitor implements VisitorNodes {
	private final ClassInfo classInfo;
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

	private MethodDeclaration currentMethod;
	private Map<ASTNode, MethodDeclaration> parentMethod;
	private Map<MethodDeclaration, Boolean> isMethodPure;

	private boolean insideAnonymousClass;

	private boolean methodRelevant;
	private boolean currentMethodPure;

	public FutureVisitor(ClassInfo classInfo) {
		this.classInfo = classInfo;
		this.classBinaryName = classInfo.getBinaryName();

		typeDeclarations = new ArrayList<>();
		assignments = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
		methodInvocations = new ArrayList<>();
		varDeclStatements = new ArrayList<>();
		simpleNames = new ArrayList<>();
		classInstanceCreations = new ArrayList<>();
		singleVarDeclarations = new ArrayList<>();
		methodDeclarations = new ArrayList<>();

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
		if (ASTUtils.isClassOf(node, classBinaryName)) {
			fieldDeclarations.add(node);
		}

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (ASTUtils.isClassOf(type, classBinaryName)) {
			varDeclStatements.add(node);

			addParent(node);
		}

		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		Expression leftHandSide = node.getLeftHandSide();

		if (leftHandSide.getNodeType() != Expression.ARRAY_ACCESS) {

			ITypeBinding type = leftHandSide.resolveTypeBinding();

			if (ASTUtils.isClassOf(type, classBinaryName)) {
				assignments.add(node);

				addParent(node);
			}
		}

		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		ITypeBinding typeBinding = simpleName.resolveTypeBinding();
		IBinding iBinding = simpleName.resolveBinding();

		if (iBinding != null) {
			int kind = iBinding.getKind();

			if (ASTUtils.isClassOf(typeBinding, classBinaryName) && kind == IBinding.VARIABLE) {
				if (!simpleNames.contains(simpleName)) {
					simpleNames.add(simpleName);

					addParent(simpleName);

					if (ASTUtils.isField(simpleName)) {
						setPurity(false);
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (ASTUtils.isClassOf(type, classBinaryName)) {
			classInstanceCreations.add(node);

			addParent(node);
		}

		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {

		if (!insideAnonymousClass) {
			currentMethod = node;
			setPurity(true);
		}

		IMethodBinding binding = node.resolveBinding();

		// Check return type
		if (binding != null) {
			ITypeBinding returnType = binding.getReturnType();

			if (Types.isExactTypeOf(returnType.getErasure(), classBinaryName)) {
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
				if (Types.isExactTypeOf(type.getErasure(), classBinaryName)) {
					setPurity(false);
					methodRelevant = true;
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
	public boolean visit(AnonymousClassDeclaration node) {
		insideAnonymousClass = true;

		return true;
	}

	@Override
	public void endVisit(AnonymousClassDeclaration node) {
		insideAnonymousClass = false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		ITypeBinding typeBinding = null;

		if (node.getExpression() != null) {
			typeBinding = node.getExpression().resolveTypeBinding();
		}

		if (typeBinding != null) {
			// ITypeBinding type = binding.getDeclaringClass();
			if (ASTUtils.isClassOf(typeBinding, classBinaryName)) {
				if (classInfo.getUnsupportedMethods().contains(node.getName().getIdentifier())) {
					Log.info(getClass(), node.getName().getIdentifier() + " is not supported!");
					setPurity(false);
					methodRelevant = true;
				} else {
					methodInvocations.add(node);
					addParent(node);

					if (ASTUtils.isInsideTryCatchBlock(node)) {
						setPurity(false);
					}
				}
			}
		}

		for (Object arg : node.arguments()) {
			if (arg instanceof SimpleName) {
				SimpleName simpleName = (SimpleName) arg;
				ITypeBinding argType = simpleName.resolveTypeBinding();
				if (ASTUtils.isClassOf(argType, classBinaryName)) {
					setPurity(false);
					methodRelevant = true;
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
	public boolean visit(SingleVariableDeclaration node) {
		ITypeBinding type = node.getType().resolveBinding();
		if (ASTUtils.isClassOf(type, classBinaryName)) {
			addParent(node);
			singleVarDeclarations.add(node);
		}
		return true;
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
		return new ArrayList<>();
	}

	@Override
	public List<ReturnStatement> getReturnStatements() {
		return new ArrayList<>();
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
