package rxjavarefactoring.framework.writers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: This class is responsible for managing the changes in one compilation unit<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public class RxSingleUnitWriter
{
	private final ASTRewrite astRewriter;
	private final CompilationUnitChange cuChange;
	private final Set<String> addedImports;
	private final Set<String> removedImports;

	public RxSingleUnitWriter( ICompilationUnit icu, AST ast, String refactoringDescription )
	{
		addedImports = new HashSet<>();
		removedImports = new HashSet<>();
		astRewriter = ASTRewrite.create( ast );
		cuChange = new CompilationUnitChange( refactoringDescription, icu );
		cuChange.setSaveMode( TextFileChange.KEEP_SAVE_STATE );
		MultiTextEdit root = new MultiTextEdit();
		cuChange.setEdit( root );
	}

	/**
	 * Add import declaration to the compilation unit. The imports are saved
	 * into a list that is used by the {@link RxMultipleUnitsWriter} where they
	 * are finally added
	 * 
	 * @param importClass
	 *            name of the class to be imported
	 */
	public void addImport( String importClass )
	{
		synchronized ( this )
		{
			addedImports.add( importClass );
		}
	}

	/**
	 * Remove import declaration from the compilation unit. The imports are
	 * saved int a list that is used by the {@link RxMultipleUnitsWriter} where
	 * they are finally removed
	 * 
	 * @param importClass
	 *            name of the class to be removed
	 */
	public void removeImport( String importClass )
	{
		synchronized ( this )
		{
			removedImports.add( importClass );
		}
	}

	/**
	 * Removes element from the compilation unit
	 * 
	 * @param element
	 *            element to be deleted
	 */
	public void removeElement( ASTNode element )
	{
		synchronized ( this )
		{
			astRewriter.remove( element, null );
		}
	}

	/**
	 * Removes statement from the compilation unit given a node contained in the
	 * statement
	 * 
	 * @param elementInTargetStatement
	 *            a node inside of a statement
	 */
	public void removeStatement( ASTNode elementInTargetStatement )
	{
		synchronized ( this )
		{
			if ( elementInTargetStatement instanceof Statement )
			{
				astRewriter.remove( elementInTargetStatement, null );
			}
			else
			{
				astRewriter.remove( ASTUtil.findParent( elementInTargetStatement, Statement.class ), null );
			}
		}
	}

	/**
	 * Adds new statement before a reference statement
	 * 
	 * @param newElement
	 *            new statement
	 * @param referenceStatement
	 */
	public void addBefore( ASTNode newElement, Statement referenceStatement )
	{
		synchronized ( this )
		{
			Block parentBlock = (Block) referenceStatement.getParent();
			ListRewrite statementsBlock = astRewriter.getListRewrite( parentBlock, Block.STATEMENTS_PROPERTY );
			Statement placeHolder = (Statement) astRewriter.createStringPlaceholder( "", ASTNode.EMPTY_STATEMENT );
			statementsBlock.insertBefore( newElement, referenceStatement, null );
			statementsBlock.insertAfter( placeHolder, newElement, null );
		}
	}

	/**
	 * Adds new field declaration before a reference field declaration
	 * 
	 * @param newFieldDecl
	 *            new field declaration
	 * @param referenceFieldDecl
	 *            reference field declaration
	 */
	public void addFieldDeclarationBefore( FieldDeclaration newFieldDecl, FieldDeclaration referenceFieldDecl )
	{
		synchronized ( this )
		{
			ListRewrite classBlock = getClassBlock( referenceFieldDecl );
			classBlock.insertBefore( newFieldDecl, referenceFieldDecl, null );
		}
	}

	/**
	 * Adds a new node at the first position of a {@link TypeDeclaration}
	 *
	 * @param constructor
	 *            new method to be inserted
	 * @param typeDeclaration
	 *            target type declaration
	 */
	public void addMethod(MethodDeclaration constructor, TypeDeclaration typeDeclaration )
	{
		synchronized ( this )
		{
			ListRewrite listRewrite = astRewriter.getListRewrite( typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY );
			listRewrite.insertFirst( constructor, null );
		}
	}

	/**
	 * Adds a new statement at the lat position of a {@link MethodDeclaration}
	 *
	 * @param statement
	 *            new method to be inserted
	 * @param methodDeclaration
	 *            target method declaration
	 */
	public void addStatement(Statement statement, MethodDeclaration methodDeclaration )
	{
		synchronized ( this )
		{
			Block body = methodDeclaration.getBody();
			ListRewrite listRewrite = astRewriter.getListRewrite( body, Block.STATEMENTS_PROPERTY );
			listRewrite.insertLast( statement, null );
		}
	}

	/**
	 * Adds a new method before a reference node. If the reference node is not
	 * a {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted before the parent found.
	 *
	 * @param methodDeclaration
	 *            new method to be inserted before the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public void addMethodBefore( MethodDeclaration methodDeclaration, ASTNode referenceNode )
	{
		synchronized ( this )
		{
			MethodDeclaration referenceMethod = ASTUtil.findParent( referenceNode, MethodDeclaration.class );
			ListRewrite classBlock = getClassBlock( referenceMethod );
			classBlock.insertBefore( methodDeclaration, referenceMethod, null );
		}
	}

	/**
	 * Adds a new method after a reference node. If the reference node is not
	 * a {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted after the parent found.
	 * 
	 * @param methodDeclaration
	 *            new method to be inserted after the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public void addMethodAfter( MethodDeclaration methodDeclaration, ASTNode referenceNode )
	{
		synchronized ( this )
		{
			MethodDeclaration referenceMethod = ASTUtil.findParent( referenceNode, MethodDeclaration.class );
			ListRewrite classBlock = getClassBlock( referenceMethod );
			classBlock.insertAfter( methodDeclaration, referenceMethod, null );
		}
	}

	/**
	 * Adds a new inner class after a reference node. If the reference node is not
	 * a {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted after the parent found.
	 * 
	 * @param typeDeclaration
	 *            new (class) type declaration to be inserted after the reference node
	 * @param referenceNode
	 *            reference node
	 */
	public void addInnerClassAfter( TypeDeclaration typeDeclaration, ASTNode referenceNode )
	{
		synchronized ( this )
		{
			MethodDeclaration referenceMethod = ASTUtil.findParent( referenceNode, MethodDeclaration.class );
			ListRewrite classBlock = getClassBlock( referenceMethod );
			classBlock.insertAfter( typeDeclaration, referenceMethod, null );
		}
	}

	public void replaceType( SimpleType oldType, String newType )
	{
		synchronized ( this )
		{
			AST ast = astRewriter.getAST();
			SimpleType newSimpleType = ast.newSimpleType( ast.newName( newType ) );
			astRewriter.replace( oldType, newSimpleType, null );
		}
	}

	public void replaceSimpleName( SimpleName oldSimpleName, String newName )
	{
		synchronized ( this )
		{
			AST ast = astRewriter.getAST();
			SimpleName newSimpleName = ast.newSimpleName( newName );
			astRewriter.replace( oldSimpleName, newSimpleName, null );
		}
	}

	/**
	 * Used by {@link RxMultipleUnitsWriter}
	 * 
	 * @return writer
	 */
	ASTRewrite getAstRewriter()
	{
		return astRewriter;
	}

	/**
	 * Used by {@link RxMultipleUnitsWriter}
	 * 
	 * @return set of added imports
	 */
	Set<String> getAddedImports()
	{
		return addedImports;
	}

	/**
	 * Used by {@link RxMultipleUnitsWriter}
	 * 
	 * @return set of removed imports
	 */
	Set<String> getRemovedImports()
	{
		return removedImports;
	}

	// ### Private Methods ###

	private ListRewrite getClassBlock( ASTNode referenceNode )
	{
		ASTNode currentClass = referenceNode.getParent();
		return astRewriter.getListRewrite( currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY );
	}
}
