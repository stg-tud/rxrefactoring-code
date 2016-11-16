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

	private boolean methodGetPresent;
	private List<String> timeoutArguments;
	private Block doInBackgroundBlock;
	private Block doneBlock;
	private Type resultType;
	private Type progressUpdateType;
	private String resultVariableName;

	public SwingWorkerVisitor()
	{
		timeoutArguments = new ArrayList<>();
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
			methodGetPresent = true;
			if ( !node.arguments().isEmpty() )
			{
				NumberLiteral time = (NumberLiteral) node.arguments().get( 0 );
				QualifiedName unit = (QualifiedName) node.arguments().get( 1 );
				timeoutArguments.add( time.getToken() );
				timeoutArguments.add( unit.getFullyQualifiedName() );
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