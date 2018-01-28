package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;

/**
 * Description: Utils to change compilation units, which may be changed later<br>
 * Author: Camila Gonzalez<br>
 * Created: 24/01/2017
 */
public class SwingWorkerASTUtils {
	
	/**
	 * Removes statement from the compilation unit given a node contained in the
	 * statement
	 * @param unit
	 * 			  compilation unit
	 * @param elementInTargetStatement
	 *            a node inside of a statement
	 */
	public static void removeStatement(@NonNull IRewriteCompilationUnit unit, @NonNull ASTNode node){
		if (node instanceof Statement)
		{
			unit.remove(node);
		}
		else
		{
			unit.remove(ASTNodes.findParent(node, Statement.class).get());
		}
	}
	
	/**
	 * Adds a new inner class after a reference node. The reference node can be
	 * a {@link FieldDeclaration} or a {@link MethodDeclaration}. If the reference node is not
	 * any of those, then its parents are searched until a {@link MethodDeclaration} is found.
	 * In this case, the new method will be inserted after the parent found.
	 * @param unit
	 * 			  compilation unit
	 * @param typeDeclaration
	 *            new (class) type declaration to be inserted after the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public static void addInnerClassAfter(@NonNull IRewriteCompilationUnit unit, @NonNull TypeDeclaration typeDeclaration, @NonNull ASTNode referenceNode )
	{
		if ( referenceNode instanceof FieldDeclaration )
		{
			ASTNode currentClass = referenceNode.getParent();
			ListRewrite classBlock = unit.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			classBlock.insertAfter(typeDeclaration, referenceNode, null);
			return;
		}
		Optional<MethodDeclaration> referenceMethod = ASTNodes.findParent( referenceNode, MethodDeclaration.class );
		if (!referenceMethod.isPresent()) {
			throw new IllegalArgumentException( SwingWorkerASTUtils.class.getName() + ": referenceNode must be a " +
					"FieldDeclaration, a MethodDeclaration or a child of a MethodDeclaration" );
		}
		ASTNode currentClass = ASTNodes.findParent(referenceMethod.get(), ASTNode.class).get();
		synchronized(unit) {
		ListRewrite classBlock = unit.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		classBlock.insertAfter( typeDeclaration, referenceMethod.get(), null );
		}
	}
	
	/**
	 * Adds a new method before a reference node. If the reference node is not
	 * a {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted before the parent found.
	 * @param unit
	 * 			  compilation unit
	 * @param methodDeclaration
	 *            new method to be inserted before the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public static void addMethodBefore(@NonNull IRewriteCompilationUnit unit, @NonNull MethodDeclaration methodDeclaration, @NonNull ASTNode referenceNode )
	{
		MethodDeclaration referenceMethod = ASTNodes.findParent(referenceNode, MethodDeclaration.class).get();
		ASTNode currentClass = referenceMethod.getParent();
		synchronized(unit) {
			ListRewrite classBlock = unit.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			classBlock.insertBefore( methodDeclaration, referenceMethod, null );
		}
	}
	
	/**
	 * Adds a new statement at the last position of a {@link MethodDeclaration}
	 * @param unit
	 * 			  compilation unit
	 * @param statement
	 *            new method to be inserted
	 * @param methodDeclaration
	 *            target method declaration
	 */
	public static void addStatement(@NonNull IRewriteCompilationUnit unit, @NonNull Statement statement, @NonNull MethodDeclaration methodDeclaration )
	{
		Block body = methodDeclaration.getBody();
		synchronized(unit) {
			ListRewrite listRewrite = unit.getListRewrite( body, Block.STATEMENTS_PROPERTY );
			listRewrite.insertLast( statement, null );
		}
	}
	
	/**
	 * Adds new node before a reference statement
	 * 
	 * @param newElement
	 *            new statement
	 * @param referenceStatement
	 */
	public static void addBefore(@NonNull IRewriteCompilationUnit unit, @NonNull ASTNode newElement, @NonNull Statement referenceStatement) 
	{		
		Block parentBlock = (Block) referenceStatement.getParent();		
		synchronized(unit) {
			ListRewrite statementsBlock = unit.getListRewrite(parentBlock, Block.STATEMENTS_PROPERTY);
			statementsBlock.insertBefore(newElement, referenceStatement, null);	
		}
	}
		


	
}
