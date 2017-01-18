package writer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;

/**
 * Description: Single Unit Writer to be used for this extension<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class SingleUnitExtensionWriter extends RxSingleUnitWriter
{
	public SingleUnitExtensionWriter( ICompilationUnit icu, AST ast, String refactoringDescription )
	{
		super( icu, ast, refactoringDescription );
	}

	public void replace( ASTNode oldValue, ASTNode newValue )
	{
		synchronized ( this )
		{
			astRewriter.replace( oldValue, newValue, null );
		}
	}

	public void addStatementToClass( ASTNode newStatement, TypeDeclaration type )
	{
		synchronized ( this )
		{
			ListRewrite statementsBlock = astRewriter.getListRewrite( type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY );
			statementsBlock.insertFirst( newStatement, null );
		}
	}

	public void addStatementBefore( Statement newStatement, Statement referenceStatement )
	{
		synchronized ( this )
		{
			if ( referenceStatement.getParent() instanceof Statement )
			{
				astRewriter.replace( referenceStatement, newStatement, null );
			}
			else
			{
				Block parentBlock = ASTUtil.findParent( referenceStatement, Block.class );
				ListRewrite statementsBlock = astRewriter.getListRewrite( parentBlock, Block.STATEMENTS_PROPERTY );
				Statement placeHolder = (Statement) astRewriter.createStringPlaceholder( "", ASTNode.EMPTY_STATEMENT );
				statementsBlock.insertBefore( newStatement, referenceStatement, null );
				statementsBlock.insertAfter( placeHolder, newStatement, null );
			}
		}
	}

	public void replaceStatement( Statement oldStatement, Statement newStatement )
	{
		synchronized ( this )
		{
			astRewriter.replace( oldStatement, newStatement, null );
		}
	}

	public void replaceStatement( MethodDeclaration m, MethodDeclaration getAsyncMethod )
	{
		synchronized ( this )
		{
			astRewriter.replace( m, getAsyncMethod, null );
		}
	}

	public void removeSuperClass( TypeDeclaration classRef )
	{
		synchronized ( this )
		{
			astRewriter.remove( classRef.getSuperclassType(), null );
		}
	}

	public void removeMethod( ASTNode parent, TypeDeclaration className )
	{
		synchronized ( this )
		{
			ListRewrite listRewriter = astRewriter.getListRewrite( className, TypeDeclaration.BODY_DECLARATIONS_PROPERTY );
			listRewriter.remove( parent, null );
		}
	}
}
