package rxjavarefactoring.processors.swingworker.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.domain.ClassDetails;
import rxjavarefactoring.framework.generalvisitors.TryCatchVisitor;
import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: Viitor to extract relevant information about SwingWorkers<br>
 * <ol>
 * <li>doInBackground Block</li>
 * <li>done Block</li>
 * <li>process Block</li>
 * <li>returned type</li>
 * <li>presence of get method and its arguments</li>
 * <li>progress update type</li>
 * </ol>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class SwingWorkerVisitor extends ASTVisitor
{
	private static final String DO_IN_BACKGROUND = "doInBackground";
	private static final String DONE = "done";
	private static final String PROCESS = "process";
	private static final String PUBLISH = "publish";
	private static final String ASYNC_RESULT = "asyncResult";
	private static final String GET = "get";
	public static final String LONG_TYPE = "long";
	public static final String TYME_UNIT_TYPE = "java.util.concurrent.TimeUnit";

	private boolean methodGetPresent;
	private List<String> timeoutArguments;
	private Block doInBackgroundBlock;
	private Block doneBlock;
	private Type resultType;
	private Type progressUpdateType;
	private String resultVariableName;
	private List<MethodInvocation> methodInvocationsGet;

	public SwingWorkerVisitor()
	{
		timeoutArguments = new ArrayList<>();
		methodInvocationsGet = new ArrayList<>();
	}

	@Override
	public boolean visit( Block node )
	{
		ASTNode parent = node.getParent();
		if ( parent instanceof MethodDeclaration )
		{
			MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
			String methodDeclarationName = methodDeclaration.getName().toString();
			if ( DO_IN_BACKGROUND.equals( methodDeclarationName ) )
			{
				doInBackgroundBlock = node;
				resultType = methodDeclaration.getReturnType2();
			}
			else if ( DONE.equals( methodDeclarationName ) )
			{
				doneBlock = node;
				resultVariableName = createUniqueName( ASYNC_RESULT );
			}
		}
		return true;
	}

	@Override
	public boolean visit( MethodInvocation node )
	{
		if ( ASTUtil.matchesTargetMethod( node, GET, ClassDetails.SWING_WORKER.getBinaryName() ) )
		{
			if ( node.arguments().isEmpty() )
			{
				// SwingWorker.get()
				methodGetPresent = true;
				methodInvocationsGet.add( node );
				return true;
			}
			else if ( node.arguments().size() == 2 )
			{
				// SwingWorker.get(long time, TimeUnit unit)
				NumberLiteral time = (NumberLiteral) node.arguments().get( 0 );
				QualifiedName unit = (QualifiedName) node.arguments().get( 1 );

				methodGetPresent = true;
				timeoutArguments.add( time.getToken() );
				timeoutArguments.add( unit.getFullyQualifiedName() );
				methodInvocationsGet.add( node );
			}

		}
		return true;
	}

	private String createUniqueName( final String name )
	{
		int counter = 0;
		String uniqueName = name;
		while ( doneBlock.toString().contains( name ) )
		{
			uniqueName = name + counter;
		}
		return uniqueName;
	}

	public Block getDoInBackgroundBlock()
	{
		return doInBackgroundBlock;
	}

	public Block getDoneBlock()
	{
		// changes all get() / get(long, TimeUnit) invocation by a variable
		for ( MethodInvocation methodInvocation : methodInvocationsGet )
		{
			SimpleName variableName = methodInvocation.getAST().newSimpleName( resultVariableName );
			ASTUtil.replaceInStatement( methodInvocation, variableName );
		}

		// check catch clauses after removing get() or get(long, TimeUnit)
		if ( doneBlock != null )
		{
			TryCatchVisitor tryCatchVisitor = new TryCatchVisitor();
			doneBlock.accept( tryCatchVisitor );
			// remove unnecessary catch clauses
			Set<ITypeBinding> neededExceptionsTypes = tryCatchVisitor.getNeededExceptionsTypes();
			Map<ITypeBinding, CatchClause> caughtExceptionsMap = tryCatchVisitor.getCaughtExceptionsMap();

			// remove try block if no clauses exist
			if ( neededExceptionsTypes.isEmpty() && !caughtExceptionsMap.isEmpty() )
			{
				TryStatement tryStatement = tryCatchVisitor.getTryStatement();
				Block parentBlock = (Block) ASTUtil.findParent( tryStatement, Block.class );
				int tryStatementPosition = parentBlock.statements().indexOf( tryStatement );
				tryStatement.delete();
				Block tryBody = tryCatchVisitor.getTryBody();
				for ( Object statement : tryBody.statements() )
				{
					ASTNode statementCopy = ASTNode.copySubtree( tryBody.getAST(), (ASTNode) statement );
					parentBlock.statements().add( tryStatementPosition++, statementCopy );
				}
			}
			else
			{
				for ( ITypeBinding caughtExceptionTypeBinding : caughtExceptionsMap.keySet() )
				{
					boolean deleteCatchClause = true;
					for ( ITypeBinding neededExceptionTypeBinding : neededExceptionsTypes )
					{
						if ( ASTUtil.isTypeOf( neededExceptionTypeBinding, caughtExceptionTypeBinding.getBinaryName() ) )
						{
							deleteCatchClause = false;
							break;
						}
					}

					if ( deleteCatchClause )
					{
						CatchClause catchClause = caughtExceptionsMap.get( caughtExceptionTypeBinding );
						catchClause.delete();
					}
				}
			}

		}

		return doneBlock;
	}

	public Type getResultType()
	{
		return resultType;
	}

	public Type getProgressUpdateType()
	{
		return progressUpdateType;
	}

	public String getResultVariableName()
	{
		return resultVariableName;
	}

	public boolean isMethodGetPresent()
	{
		return methodGetPresent;
	}

	public List<String> getTimeoutArguments()
	{
		return timeoutArguments;
	}
}
