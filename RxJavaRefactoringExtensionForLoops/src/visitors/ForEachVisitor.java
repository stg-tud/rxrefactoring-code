/**
 * 
 */
package visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;

/**
 * @author Ram
 * Modified by: Grebiel José Ifill Brito
 *
 */
public class ForEachVisitor extends ASTVisitor
{
	private List<EnhancedForStatement> forBlocks;

	public ForEachVisitor()
	{
		this.forBlocks = new ArrayList<EnhancedForStatement>();
	}

	@Override
	public boolean visit( EnhancedForStatement node )
	{
		forBlocks.add( node );
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public List<EnhancedForStatement> getForBlocks()
	{
		return forBlocks;
	}
}
