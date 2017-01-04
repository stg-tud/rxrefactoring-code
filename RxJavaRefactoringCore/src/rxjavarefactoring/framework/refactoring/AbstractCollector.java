package rxjavarefactoring.framework.refactoring;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * Description: Abstract class for collectors. The main purpose of this class is
 * to allow polymorphism. So a list can contain different types of
 * collectors<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public abstract class AbstractCollector extends ASTVisitor
{
	private String collectorName;

	public AbstractCollector( String collectorName )
	{
		this.collectorName = collectorName;
	}

	public String getCollectorName()
	{
		return collectorName;
	}

	/**
	 * Override method to print information after
	 * a project has been refactored.
	 *
	 * The default implementation returns the name
	 * of the collector
	 * 
	 * @return relevant information. For Example:<br>
	 *         - Number of refactored files
	 *         - Number of refactored X elements
	 *         - Number of refactored Y elements
	 *         - ...
	 */
	public String getInfo()
	{
		return collectorName;
	}

	public String getError()
	{
		return "[ Error ]";
	}

	/**
	 * Add or updates the list of a map given its key
	 * 
	 * @param cu
	 *            map key
	 * @param newList
	 *            list to be added (if key doesn't exist) or updated otherwise
	 * @param map
	 *            target map
	 * @param <T>
	 *            Type of the list
	 */
	protected <T> void addToMap( ICompilationUnit cu, List<T> newList, Map<ICompilationUnit, List<T>> map )
	{
		if ( newList.isEmpty() )
		{
			return;
		}
		List<T> currentList = map.get( cu );
		if ( currentList == null )
		{
			map.put( cu, newList );
		}
		else
		{
			currentList.addAll( newList );
			map.put( cu, newList );
		}
	}
}
