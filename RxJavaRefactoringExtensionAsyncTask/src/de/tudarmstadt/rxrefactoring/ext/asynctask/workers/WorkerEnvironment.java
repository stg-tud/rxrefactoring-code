package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

/**
 * Provides utility methods for workers.
 * 
 * @author mirko
 *
 */
interface WorkerEnvironment {

	/**
	 * Replaces all invocations to {@code publish} with invocations to {@subscriber.onNext}
	 * 
	 * @param asyncTask The AsyncTask which publish invocations are replaced.
	 * @param writer The writer responsible for the replacement.
	 * @param builder The builder of the subscriber.
	 */
	default void replacePublishInvocations(AsyncTaskWrapper asyncTask, SubscriberBuilder builder) {
		//Iterate over all publish invocations
		for (MethodInvocation publishInvocation : asyncTask.getPublishInvocations()) {
			
			List<?> argumentList = publishInvocation.arguments();
						
			@SuppressWarnings("unchecked")
			MethodInvocation invoke = builder.getSubscriberPublish((List<Expression>) argumentList);
			
			asyncTask.getUnit().replace(publishInvocation, invoke);
		
		}
		
	}
	
	default void addStatementBefore(BundledCompilationUnit unit, Statement newStatement, Statement referenceStatement) {
		if (referenceStatement.getParent() instanceof Statement) {
			unit.replace(referenceStatement, newStatement);
		} else {
			Block parentBlock = ASTUtils.findParent(referenceStatement, Block.class);
			ListRewrite statementsBlock = unit.getListRewrite(parentBlock, Block.STATEMENTS_PROPERTY);
			//Statement placeHolder = (Statement) astRewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
			statementsBlock.insertBefore(newStatement, referenceStatement, null);
			//statementsBlock.insertAfter(placeHolder, newStatement, null);
		}
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
	default public void addMethodBefore(BundledCompilationUnit unit, MethodDeclaration methodDeclaration, ASTNode referenceNode) {
		MethodDeclaration referenceMethod = ASTUtils.findParent(referenceNode, MethodDeclaration.class);
		ListRewrite classBlock = getClassBlock(unit, referenceMethod);
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
	default public void addMethodAfter(BundledCompilationUnit unit, MethodDeclaration methodDeclaration, ASTNode referenceNode) {
		MethodDeclaration referenceMethod = ASTUtils.findParent(referenceNode, MethodDeclaration.class);
		ListRewrite classBlock = getClassBlock(unit, referenceMethod);
		classBlock.insertAfter(methodDeclaration, referenceMethod, null);
	}
	
	default ListRewrite getClassBlock(BundledCompilationUnit unit, ASTNode referenceNode) {
		ASTNode currentClass = referenceNode.getParent();
		
		
		if (currentClass instanceof TypeDeclaration) {
			return unit.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		} else if (currentClass instanceof AnonymousClassDeclaration) {
			return unit.getListRewrite(currentClass, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
		}
		
		throw new IllegalArgumentException("getClassBlock only works for type declarations or anonymous class declarations, but was " + currentClass);		
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
	default void addInnerClassAfter(BundledCompilationUnit unit, TypeDeclaration typeDeclaration, ASTNode referenceNode) {
		if (referenceNode instanceof FieldDeclaration) {
			ListRewrite classBlock = getClassBlock(unit, referenceNode);
			classBlock.insertAfter(typeDeclaration, referenceNode, null);
			return;
		}
		MethodDeclaration referenceMethod = ASTUtils.findParent(referenceNode, MethodDeclaration.class);
		if (referenceMethod == null) {
			throw new IllegalArgumentException(this.getClass().getName() + ": referenceNode must be a "
					+ "FieldDeclaration, a MethodDeclaration or a child of a MethodDeclaration");
		}
		ListRewrite classBlock = getClassBlock(unit, referenceMethod);
		classBlock.insertAfter(typeDeclaration, referenceMethod, null);
	}
	
	default void addStatementToClass(BundledCompilationUnit unit, ASTNode newStatement, TypeDeclaration type) {
		ListRewrite statementsBlock = unit.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		statementsBlock.insertFirst(newStatement, null);
	}
	
	default void removeSuperClass(BundledCompilationUnit unit, TypeDeclaration classRef) {
		unit.remove(classRef.getSuperclassType());
	}
}
