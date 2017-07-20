package de.tudarmstadt.rxrefactoring.ext.asynctask.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/18/2017
 */
public class AsyncTaskASTUtils
{
	public static <T> ASTNode findOuterParent( ASTNode node, Class<?> target )
	{
		ASTNode parent = node.getParent();
		ASTNode outerParent = node.getParent();
		while ( parent != null )
		{
			if ( target.isInstance( parent ) )
			{
				outerParent = parent;
			}
			parent = parent.getParent();
		}
		if ( target.isInstance( outerParent ) )
			return outerParent;
		return null;
	}

	public static Expression getinstannceCreationStatement( AST astInvoke, String name )
	{
		ClassInstanceCreation instanceCreationStatement = astInvoke.newClassInstanceCreation();
		instanceCreationStatement.setType( astInvoke.newSimpleType( astInvoke.newSimpleName( name ) ) );
		return instanceCreationStatement;
	}
	
	/**
	 * Checks whether an AST contains an invocation to a method that is not allowed for refactoring.
	 * Those methods are AsyncTask.isCancelled() and AsyncTask.getStatus().
	 * 
	 * @param root The root of the AST to check.
	 * @return True, if a forbidden method has been found.
	 */
	public static boolean containsForbiddenMethod(ASTNode root) {
		
		//Log.info(AsyncTaskASTUtils.class, "### Contains");
		
		boolean result = ASTUtils.containsNode(root, (n) -> {
			if (n instanceof MethodInvocation) {
				MethodInvocation inv = (MethodInvocation) n;
				//Log.info(AsyncTaskASTUtils.class, inv.resolveMethodBinding() + " " + inv.resolveMethodBinding().getReturnType() );
				return ASTUtils.matchMethod(inv, "^android\\.os\\.AsyncTask(<.*>)?$", "isCancelled", "^boolean$") ||
						ASTUtils.matchMethod(inv, "^android\\.os\\.AsyncTask(<.*>)?$", "getStatus", "^android.os.AsyncTask.Status$");
			}
			
			return false;
		});
	
		//Log.info(AsyncTaskASTUtils.class, "### Result : " + node + ", " + result);
		return result;
	}
}
