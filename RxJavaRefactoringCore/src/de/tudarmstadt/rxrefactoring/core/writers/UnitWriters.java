package de.tudarmstadt.rxrefactoring.core.writers;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * This class stores a {@link UnitWriter} for each compilation unit.
 * Use this class in order to produce new unit writers.
 * 
 * @author Grebiel Jose Ifill Brito, Mirko Köhler
 */
public class UnitWriters {
	
	private static Map<ICompilationUnit, UnitWriter> writers;
	static {
		initializeUnitWriters();
	}

	public static void initializeUnitWriters()	{
		writers = new ConcurrentHashMap<>();
	}

	/**
	 * Returns the {@link UnitWriter} for the given compilation unit.
	 * <br>
	 * <br>
	 *  If there is no
	 * {@link UnitWriter} available, then the factory is used to create a new one
	 * and stores it for the unit. Later invocations of this method return the created
	 * {@link UnitWriter} for the unit.
	 * 
	 * @param unit The compilation unit for which the writer should be created.
	 * @param factory The factory that creates a new writer if there is no writer
	 * available for the unit.
	 * 
	 * @return A {@link UnitWriter} for the given compilation unit. The type of the writer
	 * is expected to be the same as the type that would be produced by the factory.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <W extends UnitWriter> W getOrPut(ICompilationUnit unit, UnitWriterFactory<W> factory) {
		Objects.requireNonNull(unit, "unit can not be null.");
		Objects.requireNonNull(factory, "factory can not be null.");
		
		UnitWriter writer = writers.get(unit);
		
		if (writer == null) {
			W newWriter = factory.create(); 
			writers.put(unit, newWriter);
			return newWriter;
		}
			
		return (W) writer;			
	}
	
	/**
	 * @deprecated Use {@link #getOrPut(ICompilationUnit, UnitWriterFactory)} instead.
	 */
	@Deprecated
	public synchronized static <W extends UnitWriter> W getSingleUnitWriter( ICompilationUnit unit, W writer ) {
		return getOrPut(unit, () -> writer);
	}

	public static UnitWriter get(ICompilationUnit unit)	{
		return writers.get( unit );
	}
}
