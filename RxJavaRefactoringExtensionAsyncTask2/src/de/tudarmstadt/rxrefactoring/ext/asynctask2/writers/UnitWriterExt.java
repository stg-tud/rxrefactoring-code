package de.tudarmstadt.rxrefactoring.ext.asynctask2.writers;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;



/**
 * Description: Single Unit Writer to be used for this extension<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class UnitWriterExt extends UnitWriter {
	public UnitWriterExt( ICompilationUnit icu, AST ast, String refactoringDescription )
	{
		super( icu, ast, refactoringDescription );
	}

	public synchronized void replace( ASTNode oldValue, ASTNode newValue )
	{
		astRewriter.replace( oldValue, newValue, null );
	}

	public synchronized void addStatementToClass( ASTNode newStatement, TypeDeclaration type )
	{
		ListRewrite statementsBlock = astRewriter.getListRewrite( type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY );
		statementsBlock.insertFirst( newStatement, null );
	}

	public synchronized void addStatementBefore( Statement newStatement, Statement referenceStatement )
	{
		if ( referenceStatement.getParent() instanceof Statement )
		{
			astRewriter.replace( referenceStatement, newStatement, null );
		}
		else
		{
			Block parentBlock = ASTUtils.findParent( referenceStatement, Block.class );
			ListRewrite statementsBlock = astRewriter.getListRewrite( parentBlock, Block.STATEMENTS_PROPERTY );
			Statement placeHolder = (Statement) astRewriter.createStringPlaceholder( "", ASTNode.EMPTY_STATEMENT );
			statementsBlock.insertBefore( newStatement, referenceStatement, null );
			statementsBlock.insertAfter( placeHolder, newStatement, null );
		}
	}

	public synchronized void replaceStatement( Statement oldStatement, Statement newStatement )
	{
		astRewriter.replace( oldStatement, newStatement, null );
	}

	public synchronized void replaceStatement( MethodDeclaration m, MethodDeclaration getAsyncMethod )
	{
		astRewriter.replace( m, getAsyncMethod, null );
	}

	public synchronized void removeSuperClass( TypeDeclaration classRef )
	{
		astRewriter.remove( classRef.getSuperclassType(), null );
	}

	public synchronized void removeMethod( ASTNode parent, TypeDeclaration className )
	{
		ListRewrite listRewriter = astRewriter.getListRewrite( className, TypeDeclaration.BODY_DECLARATIONS_PROPERTY );
		listRewriter.remove( parent, null );
	}
}
