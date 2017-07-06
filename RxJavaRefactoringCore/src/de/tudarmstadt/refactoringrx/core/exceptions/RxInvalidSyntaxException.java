package de.tudarmstadt.refactoringrx.core.exceptions;

/**
 * Description: Exception for invalid source syntax. It outputs the source code
 * so the developers can check at it on the console. The source code could be
 * copied to an IDE to facilitate the analysis.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class RxInvalidSyntaxException extends RuntimeException
{
	public RxInvalidSyntaxException( String message, String sourceCode )
	{
		super( message + " - Source Code:\n" + sourceCode );
	}
}
