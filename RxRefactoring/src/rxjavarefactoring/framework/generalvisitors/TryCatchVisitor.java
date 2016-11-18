package rxjavarefactoring.framework.generalvisitors;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/17/2016
 */
public class TryCatchVisitor extends ASTVisitor
{
	private Set<ITypeBinding> neededExceptionsTypes;
    private Map<ITypeBinding, CatchClause> caughtExceptionsMap;
	private Block tryBody;
	private TryStatement tryStatement;

	public TryCatchVisitor()
	{
		neededExceptionsTypes = new HashSet<>();
        caughtExceptionsMap = new HashMap<>();
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

	@Override
	public boolean visit(TryStatement node)
	{
		tryStatement = node;
		tryBody = ASTUtil.clone(node.getBody());
		return true;
	}

	@Override
    public boolean visit(CatchClause node)
    {
        SingleVariableDeclaration exception = node.getException();
        Type exceptionType = exception.getType();
        ITypeBinding exceptionTypeBinding = exceptionType.resolveBinding();
        caughtExceptionsMap.put(exceptionTypeBinding, node);
        return true;
    }

	public Set<ITypeBinding> getNeededExceptionsTypes()
	{
		return neededExceptionsTypes;
	}

	public Map<ITypeBinding, CatchClause> getCaughtExceptionsMap()
	{
		return caughtExceptionsMap;
	}

	public Block getTryBody()
	{
		return tryBody;
	}

	public TryStatement getTryStatement()
	{
		return tryStatement;
	}

	private boolean isInTryBlock(MethodInvocation node )
	{
		return ASTUtil.findParent( node, CatchClause.class ) == null;
	}
}
