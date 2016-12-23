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
	private static Object lock = new Object();

	private static Map<ICompilationUnit, RxSingleUnitWriter> rxSingleUnitWriterMap;

	public static void initializeUnitWriters()
	{
		rxSingleUnitWriterMap = new ConcurrentHashMap<>();
	}

	public static RxSingleUnitWriter getSingleUnitWriter( ICompilationUnit icu, AST ast, String refactoringDescription )
	{
		synchronized ( lock )
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

	public static RxSingleUnitWriter findSingleUnitWriter( ICompilationUnit icu )
	{
		return rxSingleUnitWriterMap.get( icu );
	}
}
