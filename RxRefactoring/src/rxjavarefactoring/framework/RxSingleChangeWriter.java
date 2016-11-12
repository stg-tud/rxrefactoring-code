package rxjavarefactoring.framework;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;

import rxjavarefactoring.utils.ASTUtil;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public class RxSingleChangeWriter
{
	private ASTRewrite astRewriter;
	private CompilationUnitChange cuChange;
	private Set<String> addedImports;
	private Set<String> removedImports;

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

	public void addImport( String importClass )
	{
		addedImports.add( importClass );
	}

	public void removeImport( String importClass )
	{
		removedImports.add( importClass );
	}

	public void removeStatement( ASTNode elementInTargetStament )
	{
		astRewriter.remove( ASTUtil.getStmtParent( elementInTargetStament ), null );
	}

	public void addStatementBefore( Statement newStatement, Statement referenceStatement )
	{
		Block parentBlock = (Block) referenceStatement.getParent();
		ListRewrite statementsBlock = astRewriter.getListRewrite( parentBlock, Block.STATEMENTS_PROPERTY );
		Statement placeHolder = (Statement) astRewriter.createStringPlaceholder( "", ASTNode.EMPTY_STATEMENT );
		statementsBlock.insertBefore( newStatement, referenceStatement, null );
		statementsBlock.insertAfter( placeHolder, newStatement, null );
	}

	public ASTRewrite getAstRewriter()
	{
		return astRewriter;
	}

	public Set<String> getAddedImports()
	{
		return addedImports;
	}

	public Set<String> getRemovedImports()
	{
		return removedImports;
	}
}
