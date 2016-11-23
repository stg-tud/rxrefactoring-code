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
public class RxSingleChangeWriter
{
	private final ASTRewrite astRewriter;
	private final CompilationUnitChange cuChange;
	private final Set<String> addedImports;
	private final Set<String> removedImports;

	public RxSingleChangeWriter( ICompilationUnit icu, AST ast, String refactoringDescription )
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
	 * into a list that is used by the {@link RxMultipleChangeWriter} where they
	 * are finally added
	 * 
	 * @param importClass
	 *            name of the class to be imported
	 */
	public void addImport( String importClass )
	{
		addedImports.add( importClass );
	}

	/**
	 * Remove import declaration from the compilation unit. The imports are
	 * saved int a list that is used by the {@link RxMultipleChangeWriter} where
	 * they are finally removed
	 * 
	 * @param importClass
	 *            name of the class to be removed
	 */
	public void removeImport( String importClass )
	{
		removedImports.add( importClass );
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
		astRewriter.remove( ASTUtil.findParent( elementInTargetStatement, Statement.class ), null );
	}

	/**
	 * Adds new statement before a reference statement
	 * 
	 * @param newStatement
	 *            new statement
	 * @param referenceStatement
	 *            reference statement
	 */
	public void addStatementBefore( Statement newStatement, Statement referenceStatement )
	{
		Block parentBlock = (Block) referenceStatement.getParent();
		ListRewrite statementsBlock = astRewriter.getListRewrite( parentBlock, Block.STATEMENTS_PROPERTY );
		Statement placeHolder = (Statement) astRewriter.createStringPlaceholder( "", ASTNode.EMPTY_STATEMENT );
		statementsBlock.insertBefore( newStatement, referenceStatement, null );
		statementsBlock.insertAfter( placeHolder, newStatement, null );
	}

	/**
	 * Adds a new method after a reference node. If the reference node is not
	 * a {@link MethodDeclaration}, then its parent {@link MethodDeclaration} is
	 * searched and taken as a reference. In this case, the new method will be
	 * inserted after the parent found.
	 * @param methodDeclaration new method to be inserted after the reference node
	 * @param referenceNode reference node
	 */
	public void addMethodAfter(MethodDeclaration methodDeclaration, ASTNode referenceNode)
	{
		MethodDeclaration referenceMethod = ASTUtil.findParent(referenceNode, MethodDeclaration.class);
		ASTNode currentClass = referenceMethod.getParent();
		ListRewrite classBlock = astRewriter.getListRewrite(currentClass, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		classBlock.insertAfter(methodDeclaration, referenceMethod, null);
	}

	/**
	 * Used by {@link RxMultipleChangeWriter}
	 * 
	 * @return writer
	 */
	ASTRewrite getAstRewriter()
	{
		return astRewriter;
	}

	/**
	 * Used by {@link RxMultipleChangeWriter}
	 * 
	 * @return set of added imports
	 */
	Set<String> getAddedImports()
	{
		return addedImports;
	}

	/**
	 * Used by {@link RxMultipleChangeWriter}
	 * 
	 * @return set of removed imports
	 */
	Set<String> getRemovedImports()
	{
		return removedImports;
	}
}
