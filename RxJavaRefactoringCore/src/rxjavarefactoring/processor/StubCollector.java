package rxjavarefactoring.processor;

import java.util.*;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.refactoring.AbstractCollector;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class StubCollector extends AbstractCollector
{
	public StubCollector(String collectorName )
	{
		super( collectorName );
	}
}
