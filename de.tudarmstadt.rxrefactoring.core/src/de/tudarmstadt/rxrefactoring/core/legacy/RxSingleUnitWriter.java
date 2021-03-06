package de.tudarmstadt.rxrefactoring.core.legacy;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;

/**
 * Description: This class is responsible for managing the changes in one
 * compilation unit<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
@Deprecated
public class RxSingleUnitWriter {
	protected final ASTRewrite astRewriter;
	protected final Set<String> addedImports;
	protected final Set<String> removedImports;

	public RxSingleUnitWriter(AST ast, String refactoringDescription) {
		addedImports = new HashSet<>();
		removedImports = new HashSet<>();
		astRewriter = ASTRewrite.create(ast);
	}

	/**
	 * Add import declaration to the compilation unit. The imports are saved into a
	 * list that is used by the {@link RxMultipleUnitsWriter} where they are finally
	 * added
	 *
	 * @param importClass
	 *            name of the class to be imported
	 */
	public synchronized void addImport(String importClass) {
		addedImports.add(importClass);
	}

	/**
	 * Remove import declaration from the compilation unit. The imports are saved
	 * int a list that is used by the {@link RxMultipleUnitsWriter} where they are
	 * finally removed
	 *
	 * @param importClass
	 *            name of the class to be removed
	 */
	public synchronized void removeImport(String importClass) {
		removedImports.add(importClass);
	}

	/**
	 * Removes element from the compilation unit
	 *
	 * @param element
	 *            element to be deleted
	 */
	public synchronized void removeElement(ASTNode element) {
		astRewriter.remove(element, null);
	}

	/**
	 * Removes statement from the compilation unit given a node contained in the
	 * statement
	 *
	 * @param elementInTargetStatement
	 *            a node inside of a statement
	 */
	public synchronized void removeStatement(ASTNode elementInTargetStatement) {
		if (elementInTargetStatement instanceof Statement) {
			astRewriter.remove(elementInTargetStatement, null);
		} else {
			astRewriter.remove(ASTNodes.findParent(elementInTargetStatement, Statement.class).get(), null);
		}
	}

	/**
	 * Adds new statement before a reference statement
	 * 
	 * @param newElement
	 *            new statement
	 * @param referenceStatement
	 */
	public synchronized void addBefore(ASTNode newElement, Statement referenceStatement) {
		Block parentBlock = (Block) referenceStatement.getParent();
		ListRewrite statementsBlock = astRewriter.getListRewrite(parentBlock, Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) astRewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
		statementsBlock.insertBefore(newElement, referenceStatement, null);
		statementsBlock.insertAfter(placeHolder, newElement, null);
	}

	/**
	 * Adds new field declaration before a reference field declaration
	 * 
	 * @param newFieldDecl
	 *            new field declaration
	 * @param referenceFieldDecl
	 *            reference field declaration
	 */
	public synchronized void addFieldDeclarationBefore(FieldDeclaration newFieldDecl,
			FieldDeclaration referenceFieldDecl) {
		ListRewrite classBlock = getClassBlock(referenceFieldDecl);
		classBlock.insertBefore(newFieldDecl, referenceFieldDecl, null);
	}

	/**
	 * Adds a new node at the first position of a {@link TypeDeclaration}
	 *
	 * @param constructor
	 *            new method to be inserted
	 * @param typeDeclaration
	 *            target type declaration
	 */
	public synchronized void addMethod(MethodDeclaration constructor, TypeDeclaration typeDeclaration) {
		ListRewrite listRewrite = astRewriter.getListRewrite(typeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(constructor, null);
	}

	/**
	 * Adds a new statement at the lat position of a {@link MethodDeclaration}
	 *
	 * @param statement
	 *            new method to be inserted
	 * @param methodDeclaration
	 *            target method declaration
	 */
	public synchronized void addStatement(Statement statement, MethodDeclaration methodDeclaration) {
		Block body = methodDeclaration.getBody();
		ListRewrite listRewrite = astRewriter.getListRewrite(body, Block.STATEMENTS_PROPERTY);
		listRewrite.insertLast(statement, null);
	}

	/**
	 * Adds a new method before a reference node. If the reference node is not a
	 * {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted before the parent found.
	 *
	 * @param methodDeclaration
	 *            new method to be inserted before the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public synchronized void addMethodBefore(MethodDeclaration methodDeclaration, ASTNode referenceNode) {
		MethodDeclaration referenceMethod = ASTNodes.findParent(referenceNode, MethodDeclaration.class).get();
		ListRewrite classBlock = getClassBlock(referenceMethod);
		classBlock.insertBefore(methodDeclaration, referenceMethod, null);
	}

	/**
	 * Adds a new method after a reference node. If the reference node is not a
	 * {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted after the parent found.
	 * 
	 * @param methodDeclaration
	 *            new method to be inserted after the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public synchronized void addMethodAfter(MethodDeclaration methodDeclaration, ASTNode referenceNode) {
		MethodDeclaration referenceMethod = ASTNodes.findParent(referenceNode, MethodDeclaration.class).get();
		ListRewrite classBlock = getClassBlock(referenceMethod);
		classBlock.insertAfter(methodDeclaration, referenceMethod, null);
	}

	/**
	 * Adds a new inner class after a reference node. The reference node can be a
	 * {@link FieldDeclaration} or a {@link MethodDeclaration}. If the reference
	 * node is not any of those, then its parents are searched until a
	 * {@link MethodDeclaration} is found. In this case, the new method will be
	 * inserted after the parent found.
	 * 
	 * @param typeDeclaration
	 *            new (class) type declaration to be inserted after the reference
	 *            node
	 * @param referenceNode
	 *            reference node
	 */
	public synchronized void addInnerClassAfter(TypeDeclaration typeDeclaration, ASTNode referenceNode) {
		if (referenceNode instanceof FieldDeclaration) {
			ListRewrite classBlock = getClassBlock(referenceNode);
			classBlock.insertAfter(typeDeclaration, referenceNode, null);
			return;
		}
		MethodDeclaration referenceMethod = ASTNodes.findParent(referenceNode, MethodDeclaration.class).get();
		if (referenceMethod == null) {
			throw new IllegalArgumentException(this.getClass().getName() + ": referenceNode must be a "
					+ "FieldDeclaration, a MethodDeclaration or a child of a MethodDeclaration");
		}
		ListRewrite classBlock = getClassBlock(referenceMethod);
		classBlock.insertAfter(typeDeclaration, referenceMethod, null);
	}

	/**
	 * Replaces a given type by another type specified as a string
	 * 
	 * @param oldType
	 *            old type
	 * @param newType
	 *            new type
	 */
	public synchronized void replaceType(Type oldType, String newType) {
		AST ast = astRewriter.getAST();
		SimpleType newSimpleType = ast.newSimpleType(ast.newName(newType));
		astRewriter.replace(oldType, newSimpleType, null);
	}

	@SuppressWarnings("unchecked")
	public synchronized void replaceParameterizedType(Type oldType, String newType, String newTypeArgument) {
		AST ast = astRewriter.getAST();
		ParameterizedType type = ast.newParameterizedType(ast.newSimpleType(ast.newName(newType)));
		type.typeArguments().add(ast.newSimpleType(ast.newSimpleName(newTypeArgument)));

		astRewriter.replace(oldType, type, null);
	}

	/**
	 * Replaces a given simple name by another name specified as a string
	 * 
	 * @param oldSimpleName
	 *            old simple name
	 * @param newName
	 *            new simple name
	 */
	public synchronized void replaceSimpleName(SimpleName oldSimpleName, String newName) {
		AST ast = astRewriter.getAST();
		SimpleName newSimpleName = ast.newSimpleName(newName);
		astRewriter.replace(oldSimpleName, newSimpleName, null);
	}

	public synchronized void appendSimpleName(SimpleName oldSimpleName, String appendName) {
		AST ast = astRewriter.getAST();
		SimpleName newSimpleName = ast.newSimpleName(oldSimpleName.getIdentifier() + appendName);
		astRewriter.replace(oldSimpleName, newSimpleName, null);
	}

	public synchronized SimpleName appendSimpleName2(SimpleName oldSimpleName, String appendName) {
		AST ast = astRewriter.getAST();
		SimpleName newSimpleName = ast.newSimpleName(oldSimpleName.getIdentifier() + appendName);
		astRewriter.replace(oldSimpleName, newSimpleName, null);

		return newSimpleName;
	}

	/**
	 * Replaces a given node by another node
	 * 
	 * @param newNode
	 *            new node
	 * @param oldNode
	 *            old node
	 */
	public synchronized void replaceNode(ASTNode newNode, ASTNode oldNode) {
		astRewriter.replace(oldNode, newNode, null);
	}

	/**
	 * Moves an expression inside a new method invocation. E.g.: x becomes
	 * className.methodName(x)
	 * 
	 * @param className
	 * @param methodName
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public synchronized void moveInsideMethodInvocation(String className, String methodName, ASTNode node) {
		AST ast = astRewriter.getAST();

		Expression initializerClone = null;// ASTUtils.clone((Expression)node);

		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(ast.newSimpleName(className));
		invocation.setName(ast.newSimpleName(methodName));
		invocation.arguments().add(initializerClone);

		replaceNode(invocation, node);
	}

	@SuppressWarnings("unchecked")
	public synchronized void moveInsideMethodInvocation(String className, String methodName, SimpleName node,
			String append) {
		AST ast = astRewriter.getAST();

		Expression initializerClone = appendSimpleName2((SimpleName) node, append);

		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(ast.newSimpleName(className));
		invocation.setName(ast.newSimpleName(methodName));
		invocation.arguments().add(initializerClone);

		replaceNode(invocation, node);
	}

//	public synchronized void replaceMethodInvocation(String caller, String method1, String method2,
//			MethodInvocation oldNode) {
//		AST ast = astRewriter.getAST();
//
//		MethodInvocation singleMethod = ast.newMethodInvocation();
//		singleMethod.setName(ast.newSimpleName(method2));
//		MethodInvocation toBlockingMethod = ast.newMethodInvocation();
//		toBlockingMethod.setName(ast.newSimpleName(method1));
//
//		Expression old = oldNode.getExpression();
//
//		if (old instanceof ArrayAccess) {
//			ArrayAccess clone = null;// (ArrayAccess)ASTUtil.clone(old);
//			clone.setArray(ast.newSimpleName(caller));
//
//			toBlockingMethod.setExpression(clone);
//		} else {
//			toBlockingMethod.setExpression(ast.newSimpleName(caller));
//		}
//
//		singleMethod.setExpression(toBlockingMethod);
//
//		replaceNode(singleMethod, oldNode);
//	}

	/**
	 * Used by {@link RxMultipleUnitsWriter}
	 * 
	 * @return writer
	 */
	ASTRewrite getAstRewriter() {
		return astRewriter;
	}

	/**
	 * Used by {@link RxMultipleUnitsWriter}
	 * 
	 * @return set of added imports
	 */
	Set<String> getAddedImports() {
		return addedImports;
	}

	/**
	 * Used by {@link RxMultipleUnitsWriter}
	 * 
	 * @return set of removed imports
	 */
	Set<String> getRemovedImports() {
		return removedImports;
	}

	// ### Private Methods ###

	protected ListRewrite getClassBlock(ASTNode referenceNode) {
		ASTNode currentClass = referenceNode.getParent();
		return astRewriter.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
	}
}
