package rxjavarefactoring.processors.asynctask.visitors;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.utils.ASTUtil;

/**
 * Description: Visitor to extract relevant information about AsyncTasks<br>
 * <ol>
 * <li>onPreExecute Block</li> // TODO
 * <li>doInBackground Block</li>
 * <li>onProgressUpdate Block</li> // TODO
 * <li>onPostExecute Block</li>
 * <li>returned type</li>
 * </ol>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class AsyncTaskVisitor extends ASTVisitor
{

	private static final String DO_IN_BACKGROUND = "doInBackground";
	private static final String ON_POST_EXECUTE = "onPostExecute";

	private Block doInBackgroundBlock;
	private Block onPostExecuteBlock;
	private Type returnedType;
	private String resultVariableName;

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
				returnedType = methodDeclaration.getReturnType2();
			}
			else if ( ON_POST_EXECUTE.equals( methodDeclarationName ) )
			{
				onPostExecuteBlock = node;
				resultVariableName = ASTUtil.getVariableName( methodDeclaration, 0 );
			}
		}
		return super.visit( node );
	}

	public Block getDoInBackgroundBlock()
	{
		return doInBackgroundBlock;
	}

	public Block getOnPostExecuteBlock()
	{
		return onPostExecuteBlock;
	}

	public Type getReturnedType()
	{
		return returnedType;
	}

	public String getResultVariableName()
	{
		return resultVariableName;
	}
}
