package rxjavarefactoring.framework.utils.visitors;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: <br>
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

	private boolean isInTryBlock(MethodInvocation node )
	{
		return ASTUtil.findParent( node, CatchClause.class ) == null;
	}
}
