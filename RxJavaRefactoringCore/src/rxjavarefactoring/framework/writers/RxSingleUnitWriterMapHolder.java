package rxjavarefactoring.framework.writers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.ICompilationUnit;

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

	public static <Writer extends RxSingleUnitWriter> Writer getSingleUnitWriter( ICompilationUnit icu, Writer rxSingleUnitWriterInstance )
	{
		synchronized ( lock )
		{

			Writer rxSingleUnitWriter = (Writer) rxSingleUnitWriterMap.get( icu );
			if ( rxSingleUnitWriter != null )
			{
				return rxSingleUnitWriter;
			}

			rxSingleUnitWriterMap.put( icu, rxSingleUnitWriterInstance );
			return rxSingleUnitWriterInstance;
		}
	}

	public static RxSingleUnitWriter findSingleUnitWriter( ICompilationUnit icu )
	{
		return rxSingleUnitWriterMap.get( icu );
	}
}
