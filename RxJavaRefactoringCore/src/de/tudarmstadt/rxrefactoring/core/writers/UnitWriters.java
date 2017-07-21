package de.tudarmstadt.rxrefactoring.core.writers;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class UnitWriters {
	private static Map<ICompilationUnit, UnitWriter> writers;

	public static void initializeUnitWriters()	{
		writers = new ConcurrentHashMap<>();
	}

	public synchronized static <W extends UnitWriter> W getOrElse(ICompilationUnit unit, UnitWriterFactory<W> factory) {
		UnitWriter writer = writers.get(unit);
		
		if (writer == null && factory != null) {
			W newWriter = factory.create(); 
			writers.put(unit, newWriter);
			return newWriter;
		}
			
		return (W) writer;			
	}
	
	/**
	 * @deprecated Use {@link #getOrElse(ICompilationUnit, UnitWriterFactory)} instead.
	 */
	@Deprecated
	public synchronized static <W extends UnitWriter> W getSingleUnitWriter( ICompilationUnit unit, W writer ) {
		return getOrElse(unit, () -> writer);
	}

	public static UnitWriter get(ICompilationUnit unit)	{
		return writers.get( unit );
	}
}
