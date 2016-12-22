package rxjavarefactoring.framework.writers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class RxSingleUnitWriterMapHolder
{
	private static Map<ICompilationUnit, RxSingleUnitWriter> rxSingleUnitWriterMap = new ConcurrentHashMap<>();

	public static RxSingleUnitWriter getSingleUnitWriter( ICompilationUnit icu, AST ast, String refactoringDescription )
	{
		RxSingleUnitWriter rxSingleUnitWriter = rxSingleUnitWriterMap.get( icu );
		if ( rxSingleUnitWriter != null )
		{
			return rxSingleUnitWriter;
		}

		RxSingleUnitWriter newRxSingleUnitWriter = new RxSingleUnitWriter( icu, ast, refactoringDescription );
		rxSingleUnitWriterMap.put( icu, newRxSingleUnitWriter );
		return newRxSingleUnitWriter;
	}
}
