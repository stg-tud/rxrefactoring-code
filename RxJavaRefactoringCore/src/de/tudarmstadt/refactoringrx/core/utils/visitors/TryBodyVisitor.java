package de.tudarmstadt.refactoringrx.core.utils.visitors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;

import de.tudarmstadt.refactoringrx.core.utils.ASTUtil;

/**
 * Description: Analyzes the body of a try catch block<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/17/2016
 */
class TryBodyVisitor extends ASTVisitor
{
	private Set<ITypeBinding> neededExceptionsTypes;

	TryBodyVisitor()
	{
		neededExceptionsTypes = new HashSet<>();
	}

	@Override
	public boolean visit( MethodInvocation node )
	{
		if ( isInTryBlock( node ) )
		{
			IMethodBinding methodBinding = node.resolveMethodBinding();
			if ( methodBinding != null )
			{
				ITypeBinding[] exceptionTypes = methodBinding.getExceptionTypes();
				neededExceptionsTypes.addAll( Arrays.asList( exceptionTypes ) );
			}
		}
		return true;
	}

	Set<ITypeBinding> getNeededExceptionsTypes()
	{
		return neededExceptionsTypes;
	}

	private boolean isInTryBlock( MethodInvocation node )
	{
		return ASTUtil.findParent( node, CatchClause.class ) == null;
	}
}
