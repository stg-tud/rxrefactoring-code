package rxjavarefactoring.framework.utils.visitors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.*;

/**
 * Description: Analyzes the catch clauses of a try-catch block<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/17/2016
 */
class TryCatchClausesVisitor extends ASTVisitor
{
    private Map<ITypeBinding, CatchClause> caughtExceptionsMap;

	TryCatchClausesVisitor()
	{
        caughtExceptionsMap = new HashMap<>();
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

	Map<ITypeBinding, CatchClause> getCaughtExceptionsMap()
	{
		return caughtExceptionsMap;
	}
}
