package de.tudarmstadt.rxrefactoring.core.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;

import de.tudarmstadt.rxrefactoring.core.codegen.ASTNodeFactory;
import de.tudarmstadt.rxrefactoring.core.exceptions.RxInvalidSyntaxException;

/**
 * Description: Responsible for validating source code given as text <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public final class SourceCodeValidator
{
	private SourceCodeValidator()
	{
		// This class should not be instantiated
	}

	/**
	 * Validates the source code of a given statement
	 * 
	 * @param statement
	 *            statement to be validated
	 * @throws RxInvalidSyntaxException
	 *             if the syntax is invalid
	 */
	public static void validateStatement( String statement ) throws RxInvalidSyntaxException
	{
		Block singleStatementBlock = ASTNodeFactory.createStatementsBlockFromText( AST.newAST( AST.JLS8 ), statement );
		if ( singleStatementBlock.statements().isEmpty() )
		{
			throw new RxInvalidSyntaxException( "Invalid Syntax for Statement", statement );
		}
	}
}
