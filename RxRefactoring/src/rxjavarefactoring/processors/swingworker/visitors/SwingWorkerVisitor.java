package rxjavarefactoring.processors.swingworker.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.domain.ClassDetails;
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

	private List<String> timeoutArguments;
	private Block doInBackgroundBlock;
	private Block doneBlock;
	private Block processBlock;
	private Block timeoutCatchBlock;
	private Type resultType;
	private String resultVariableName;
	private String progressUpdateTypeName;
	private String progressUpdateVariableName;
	private List<MethodInvocation> methodInvocationsGet;
	private List<MethodInvocation> methodInvocationsPublish;
	private List<SuperMethodInvocation> superMethodInvocationsPublish;
	private List<SuperMethodInvocation> superMethodInvocationsGet;
	private List<SuperMethodInvocation> superMethodInvocationsToRemove;

	// for "stateful" classes
	private List<FieldDeclaration> fieldDeclarations;
	private List<MethodDeclaration> additionalMethodDeclarations;

	public SwingWorkerVisitor()
	{
		timeoutArguments = new ArrayList<>();
		methodInvocationsGet = new ArrayList<>();
		methodInvocationsPublish = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
		additionalMethodDeclarations = new ArrayList<>();
		superMethodInvocationsPublish = new ArrayList<>();
		superMethodInvocationsGet = new ArrayList<>();
		superMethodInvocationsToRemove = new ArrayList<>();
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
			else if ( PROCESS.equals( methodDeclarationName ) )
			{
				processBlock = node;
				progressUpdateTypeName = ASTUtil.getParameterType( methodDeclaration, 0 );
				progressUpdateVariableName = ASTUtil.getVariableName( methodDeclaration, 0 );
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
				methodInvocationsGet.add( node );
				return true;
			}
			else if ( node.arguments().size() == 2 )
			{
				// SwingWorker.get(long time, TimeUnit unit)
				NumberLiteral time = (NumberLiteral) node.arguments().get( 0 );
				QualifiedName unit = (QualifiedName) node.arguments().get( 1 );

				timeoutArguments.add( time.getToken() );
				timeoutArguments.add( unit.getFullyQualifiedName() );
				methodInvocationsGet.add( node );
				setTimeoutCatchBlock( node );
			}

		}
		else if ( ASTUtil.matchesTargetMethod( node, PUBLISH, ClassDetails.SWING_WORKER.getBinaryName() ) )
		{
			methodInvocationsPublish.add( node );

		}
		return true;
	}

	@Override
	public boolean visit( FieldDeclaration node )
	{
		fieldDeclarations.add( node );
		return true;
	}

	@Override
	public boolean visit( MethodDeclaration node )
	{
		String methodDeclarationName = node.getName().toString();
		if ( !DO_IN_BACKGROUND.equals( methodDeclarationName ) &&
				!DONE.equals( methodDeclarationName ) &&
				!PROCESS.equals( methodDeclarationName ) )
		{
			additionalMethodDeclarations.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( SuperMethodInvocation node )
	{
		if ( ASTUtil.matchesTargetMethod( node, GET, ClassDetails.SWING_WORKER.getBinaryName() ) )
		{
			superMethodInvocationsGet.add( node );
		}
		else if ( ASTUtil.matchesTargetMethod( node, PUBLISH, ClassDetails.SWING_WORKER.getBinaryName() ) )
		{
			superMethodInvocationsPublish.add( node );
		}
		else if ( ASTUtil.matchesTargetMethod( node, DONE, ClassDetails.SWING_WORKER.getBinaryName() ) ||
				ASTUtil.matchesTargetMethod( node, PROCESS, ClassDetails.SWING_WORKER.getBinaryName() ) )
		{
			superMethodInvocationsToRemove.add( node );
		}
		return true;
	}

	public Block getDoInBackgroundBlock()
	{
		return doInBackgroundBlock;
	}

	public Block getDoneBlock()
	{
		return doneBlock;
	}

	public Type getResultType()
	{
		return resultType;
	}

	public String getProgressUpdateTypeName()
	{
		return progressUpdateTypeName;
	}

	public String getResultVariableName()
	{
		return resultVariableName;
	}

	public List<String> getTimeoutArguments()
	{
		return timeoutArguments;
	}

	public List<MethodInvocation> getMethodInvocationsGet()
	{
		return methodInvocationsGet;
	}

	public Block getTimeoutCatchBlock()
	{
		return timeoutCatchBlock;
	}

	public Block getProcessBlock()
	{
		return processBlock;
	}

	public String getProgressUpdateVariableName()
	{
		return progressUpdateVariableName;
	}

	public List<MethodInvocation> getMethodInvocationsPublish()
	{
		return methodInvocationsPublish;
	}

	public List<FieldDeclaration> getFieldDeclarations()
	{
		return fieldDeclarations;
	}

	public List<MethodDeclaration> getAdditionalMethodDeclarations()
	{
		return additionalMethodDeclarations;
	}

	public List<SuperMethodInvocation> getSuperMethodInvocationsPublish()
	{
		return superMethodInvocationsPublish;
	}

	public List<SuperMethodInvocation> getSuperMethodInvocationsGet()
	{
		return superMethodInvocationsGet;
	}

	public List<SuperMethodInvocation> getSuperMethodInvocationsToRemove()
	{
		return superMethodInvocationsToRemove;
	}

	public boolean hasAdditionalFieldsOrMethods()
	{
		return !fieldDeclarations.isEmpty() || !additionalMethodDeclarations.isEmpty();
	}

	// ### Private Methods ###

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

	private void setTimeoutCatchBlock( MethodInvocation node )
	{
		// get block for TimeoutException
		TryStatement tryStatement = ASTUtil.findParent( node, TryStatement.class );
		if ( tryStatement != null )
		{
			List catchClauses = tryStatement.catchClauses();
			for ( Object catchClauseObject : catchClauses )
			{
				CatchClause catchClause = (CatchClause) catchClauseObject;
				SingleVariableDeclaration declaration = catchClause.getException();
				IMethodBinding methodBinding = node.resolveMethodBinding();
				ITypeBinding[] exceptionTypes = methodBinding.getExceptionTypes();
				// 0 java.lang.InterruptedException
				// 1 java.util.concurrent.ExecutionException
				// 2 java.util.concurrent.TimeoutException
				ITypeBinding timeOutException = exceptionTypes[ 2 ];
				Type type = declaration.getType();
				if ( ASTUtil.isTypeOf( timeOutException, type.resolveBinding().getBinaryName() ) )
				{
					timeoutCatchBlock = catchClause.getBody();
				}
			}
		}
	}
}
