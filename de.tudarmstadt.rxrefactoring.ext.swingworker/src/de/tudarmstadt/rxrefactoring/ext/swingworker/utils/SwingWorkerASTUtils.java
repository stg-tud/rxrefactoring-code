package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;

/**
 * Description: Utils to change compilation units<br>
 * Author: Camila Gonzalez<br>
 * Created: 24/01/2017
 */
public class SwingWorkerASTUtils {

	/**
	 * Removes statement from the compilation unit given a node contained in the
	 * statement
	 * 
	 * @param unit
	 *            compilation unit
	 * @param elementInTargetStatement
	 *            a node inside of a statement
	 */
	public static void removeStatement(@NonNull IRewriteCompilationUnit unit, @NonNull ASTNode node) {
		synchronized (unit) {
			if (node instanceof Statement) {
				unit.remove(node);
			} else {
				unit.remove(ASTNodes.findParent(node, Statement.class).get());
			}
		}
	}

	/**
	 * Adds a new inner class after a reference node. The reference node can be a
	 * {@link FieldDeclaration} or a {@link MethodDeclaration}. If the reference
	 * node is not any of those, then its parents are searched until a
	 * {@link MethodDeclaration} is found. In this case, the new method will be
	 * inserted after the parent found.
	 * 
	 * @param unit
	 *            compilation unit
	 * @param typeDeclaration
	 *            new (class) type declaration to be inserted after the reference
	 *            node
	 * @param referenceNode
	 *            reference node
	 */
	public static void addInnerClassAfter(@NonNull IRewriteCompilationUnit unit,
			@NonNull TypeDeclaration typeDeclaration, @NonNull ASTNode referenceNode) {
		synchronized (unit) {
			if (referenceNode instanceof FieldDeclaration) {
				ASTNode currentClass = referenceNode.getParent();
				ListRewrite classBlock = unit.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				classBlock.insertAfter(typeDeclaration, referenceNode, null);
				return;
			}
			Optional<MethodDeclaration> referenceMethod = ASTNodes.findParent(referenceNode, MethodDeclaration.class);
			if (!referenceMethod.isPresent()) {
				throw new IllegalArgumentException(SwingWorkerASTUtils.class.getName() + ": referenceNode must be a "
						+ "FieldDeclaration, a MethodDeclaration or a child of a MethodDeclaration");
			}
			ASTNode currentClass = ASTNodes.findParent(referenceMethod.get(), ASTNode.class).get();

			ListRewrite classBlock = unit.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			classBlock.insertAfter(typeDeclaration, referenceMethod.get(), null);
		}
	}

	/**
	 * Creates a {@link SimpleName}
	 * 
	 * @param ast
	 *            {@link AST}
	 * @param identifier
	 *            identifier for new name
	 * @returns a {@link SimpleName}
	 * 
	 */
	public static @NonNull SimpleName newSimpleName(@NonNull AST ast, @NonNull String identifier) {
		synchronized (ast) {
			return ast.newSimpleName(identifier);
		}
	}

	/**
	 * Creates an {@link ExpressionStatement} which assigns a variable a newly
	 * created instance
	 * 
	 * @param ast
	 *            {@link AST}
	 * @param variable
	 *            name of the variable
	 * @param type
	 *            the type of the newly created instance
	 * @returns an {@link ExpressionStatement}
	 * 
	 */
	public static ExpressionStatement newAssignment(@NonNull AST ast, @NonNull String variable, @NonNull String type) {
		synchronized (ast) {
			Assignment newAssignment = ast.newAssignment();
			newAssignment.setLeftHandSide(newSimpleName(ast, variable));
			ClassInstanceCreation right = ast.newClassInstanceCreation();
			right.setType(ast.newSimpleType(ast.newName(type)));
			newAssignment.setRightHandSide(right);
			return ast.newExpressionStatement(newAssignment);
		}
	}

	/**
	 * Creates a {@link ClassInstanceCreation}
	 * 
	 * @param ast
	 *            {@link AST}
	 * @param className
	 *            the name of the new class
	 * @returns a {@link ClassInstanceCreation}
	 * 
	 */
	public static ClassInstanceCreation newClassInstanceCreation(@NonNull AST ast, @NonNull String className) {
		synchronized (ast) {
			ClassInstanceCreation newClassInstanceCreation = ast.newClassInstanceCreation();
			newClassInstanceCreation.setType(ast.newSimpleType(ast.newName(className)));
			return newClassInstanceCreation;
		}
	}

	/**
	 * Copies the subtree of an {@link ASTNode}
	 * 
	 * @param ast
	 *            {@link AST}
	 * @param node
	 *            the original {@link ASTNode}
	 * @returns a {@link ASTNode}
	 * 
	 */
	public static ASTNode copySubtree(@NonNull AST ast, @NonNull ASTNode node) {
		synchronized (ast) {
			return ASTNode.copySubtree(ast, node);
		}
	}

	/**
	 * Creates a {@link SimpleType}
	 * 
	 * @param ast
	 *            {@link AST}
	 * @param identifier
	 *            identifier for new type
	 * @returns a {@link SimpleType}
	 * 
	 */
	public static SimpleType newSimpleType(@NonNull AST ast, @NonNull String identifier) {
		synchronized (ast) {
			return ast.newSimpleType(ast.newName(identifier));
		}
	}

	/**
	 * Adds a new method before a reference node. If the reference node is not a
	 * {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted before the parent found.
	 * 
	 * @param unit
	 *            compilation unit
	 * @param methodDeclaration
	 *            new method to be inserted before the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public static void addMethodBefore(@NonNull IRewriteCompilationUnit unit,
			@NonNull MethodDeclaration methodDeclaration, @NonNull ASTNode referenceNode) {
		synchronized (unit) {
			MethodDeclaration referenceMethod = ASTNodes.findParent(referenceNode, MethodDeclaration.class).get();
			ASTNode currentClass = referenceMethod.getParent();
			ListRewrite classBlock = unit.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			classBlock.insertBefore(methodDeclaration, referenceMethod, null);
		}
	}

	/**
	 * Adds a new statement at the last position of a {@link MethodDeclaration}
	 * 
	 * @param unit
	 *            compilation unit
	 * @param statement
	 *            new method to be inserted
	 * @param methodDeclaration
	 *            target method declaration
	 */
	public static void addStatement(@NonNull IRewriteCompilationUnit unit, @NonNull Statement statement,
			@NonNull MethodDeclaration methodDeclaration) {
		synchronized (unit) {
			Block body = methodDeclaration.getBody();
			ListRewrite listRewrite = unit.getListRewrite(body, Block.STATEMENTS_PROPERTY);
			listRewrite.insertLast(statement, null);
		}
	}

	/**
	 * Adds new node before a reference statement
	 * 
	 * @param newElement
	 *            new statement
	 * @param referenceStatement
	 */
	public static void addBefore(@NonNull IRewriteCompilationUnit unit, @NonNull ASTNode newElement,
			@NonNull Statement referenceStatement) {
		synchronized (unit) {
			Block parentBlock = (Block) referenceStatement.getParent();
			ListRewrite statementsBlock = unit.getListRewrite(parentBlock, Block.STATEMENTS_PROPERTY);
			statementsBlock.insertBefore(newElement, referenceStatement, null);
		}
	}

}
