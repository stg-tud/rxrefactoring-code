package rxjavarefactoring.framework.utils.visitors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: Collects information about try-catch blocks inside of a node. It
 * uses {@link TryBodyVisitor} to analyze the body of the try catch blocks and
 * {@link TryCatchClausesVisitor} to analyze the catch clauses<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/17/2016
 */
public class TryStatementVisitor extends ASTVisitor
{
	private Map<TryStatement, Set<ITypeBinding>> neededExceptionsTypesMap;
	private Map<TryStatement, Map<ITypeBinding, CatchClause>> caughtExceptionsMap;
	private Map<TryStatement, Block> tryBodyMap;

	public TryStatementVisitor()
	{
		neededExceptionsTypesMap = new HashMap<>();
		caughtExceptionsMap = new HashMap<>();
		tryBodyMap = new HashMap<>();
	}

	@Override
	public boolean visit( TryStatement node )
	{
		TryBodyVisitor tryBodyVisitor = new TryBodyVisitor();
		TryCatchClausesVisitor tryCatchClausesVisitor = new TryCatchClausesVisitor();
		node.accept( tryBodyVisitor );
		node.accept( tryCatchClausesVisitor );
		neededExceptionsTypesMap.put( node, tryBodyVisitor.getNeededExceptionsTypes() );
		caughtExceptionsMap.put( node, tryCatchClausesVisitor.getCaughtExceptionsMap() );
		tryBodyMap.put( node, ASTUtil.clone( node.getBody() ) );
		return true;
	}

	public Map<TryStatement, Set<ITypeBinding>> getNeededExceptionsTypesMap()
	{
		return neededExceptionsTypesMap;
	}

	public Map<TryStatement, Map<ITypeBinding, CatchClause>> getCaughtExceptionsMap()
	{
		return caughtExceptionsMap;
	}

	public Map<TryStatement, Block> getTryBodyMap()
	{
		return tryBodyMap;
	}
}
