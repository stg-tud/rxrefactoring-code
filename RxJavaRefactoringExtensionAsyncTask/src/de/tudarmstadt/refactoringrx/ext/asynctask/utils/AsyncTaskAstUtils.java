package de.tudarmstadt.refactoringrx.ext.asynctask.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/18/2017
 */
public class AsyncTaskAstUtils
{
	public static <T> ASTNode findOuterParent( ASTNode node, Class<?> target )
	{
		ASTNode parent = node.getParent();
		ASTNode outerParent = node.getParent();
		while ( parent != null )
		{
			if ( target.isInstance( parent ) )
			{
				outerParent = parent;
			}
			parent = parent.getParent();
		}
		if ( target.isInstance( outerParent ) )
			return outerParent;
		return null;
	}

	public static Expression getinstannceCreationStatement( AST astInvoke, String name )
	{
		ClassInstanceCreation instanceCreationStatement = astInvoke.newClassInstanceCreation();
		instanceCreationStatement.setType( astInvoke.newSimpleType( astInvoke.newSimpleName( name ) ) );
		return instanceCreationStatement;
	}
}
