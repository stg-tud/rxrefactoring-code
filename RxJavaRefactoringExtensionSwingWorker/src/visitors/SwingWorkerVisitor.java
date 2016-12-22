package visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: Visitor to extract relevant information about SwingWorkers<br>
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
	private Block interruptedCatchBlock;
	private String timeoutExceptionName;
	private String interruptedExceptionName;
	private Type resultType;
	private Type processType;
	private String asyncResultVarName;
	private String processVariableName;
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
	public boolean visit( ClassInstanceCreation node )
	{
		ParameterizedType type = (ParameterizedType) node.getType();
		List argumentTypes = type.typeArguments();
		resultType = (Type) argumentTypes.get( 0 );
		processType = (Type) argumentTypes.get( 1 );
		return true;
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
				asyncResultVarName = createUniqueName( ASYNC_RESULT );
			}
			else if ( PROCESS.equals( methodDeclarationName ) )
			{
				processBlock = node;
				processVariableName = ASTUtil.getVariableName( methodDeclaration, 0 );
			}
		}
		return true;
	}

	@Override
	public boolean visit( MethodInvocation node )
	{
		if ( ASTUtil.matchesTargetMethod( node, GET, SwingWorkerInfo.getBinaryName() ) )
		{
			if ( node.arguments().isEmpty() )
			{
				// SwingWorker.get()
				methodInvocationsGet.add( node );
				setInterruptedException( node );
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
				setExceptionsBlocks( node );
			}

		}
		else if ( ASTUtil.matchesTargetMethod( node, PUBLISH, SwingWorkerInfo.getBinaryName() ) )
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
		if ( ASTUtil.matchesTargetMethod( node, GET, SwingWorkerInfo.getBinaryName() ) )
		{
			superMethodInvocationsGet.add( node );
		}
		else if ( ASTUtil.matchesTargetMethod( node, PUBLISH, SwingWorkerInfo.getBinaryName() ) )
		{
			superMethodInvocationsPublish.add( node );
		}
		else if ( ASTUtil.matchesTargetMethod( node, DONE, SwingWorkerInfo.getBinaryName() ) ||
				ASTUtil.matchesTargetMethod( node, PROCESS, SwingWorkerInfo.getBinaryName() ) )
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

	public Type getProcessType()
	{
		return processType;
	}

	public String getAsyncResultVarName()
	{
		return asyncResultVarName;
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

	public Block getInterruptedCatchBlock()
	{
		return interruptedCatchBlock;
	}

	public String getTimeoutExceptionName()
	{
		return timeoutExceptionName;
	}

	public String getInterruptedExceptionName()
	{
		return interruptedExceptionName;
	}

	public Block getProcessBlock()
	{
		return processBlock;
	}

	public String getProcessVariableName()
	{
		return processVariableName;
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

	private void setInterruptedException( MethodInvocation node )
	{
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
				ITypeBinding interruptedException = exceptionTypes[ 0 ];
				Type type = declaration.getType();
				if ( ASTUtil.isTypeOf( interruptedException, type.resolveBinding().getBinaryName() ) )
				{
					interruptedCatchBlock = catchClause.getBody();
					interruptedExceptionName = catchClause.getException().getName().toString();
				}
			}
		}
	}

	private void setExceptionsBlocks( MethodInvocation node )
	{
		// get block for TimeoutException and InterruptedException
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
				ITypeBinding interruptedException = exceptionTypes[ 0 ];
				Type type = declaration.getType();
				if ( ASTUtil.isTypeOf( interruptedException, type.resolveBinding().getBinaryName() ) )
				{
					interruptedCatchBlock = catchClause.getBody();
					interruptedExceptionName = catchClause.getException().getName().toString();
				}

				ITypeBinding timeOutException = exceptionTypes[ 2 ];
				if ( ASTUtil.isTypeOf( timeOutException, type.resolveBinding().getBinaryName() ) )
				{
					timeoutCatchBlock = catchClause.getBody();
					timeoutExceptionName = catchClause.getException().getName().toString();
				}
			}
		}
	}
}
