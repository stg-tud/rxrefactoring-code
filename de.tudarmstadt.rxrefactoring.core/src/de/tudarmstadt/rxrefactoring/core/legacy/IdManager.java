package de.tudarmstadt.rxrefactoring.core.legacy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Description: This class is responsible for generating next ids for new
 * elements thread safely. The ids are unique for a compilation unit<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/23/2016
 */
@Deprecated
public final class IdManager {
	private static final String EMPTY = "";
	private static final int INITIAL_ID = 0;

	private static Map<ICompilationUnit, Integer> observerCounter = new ConcurrentHashMap<>();
	private static Map<ICompilationUnit, Integer> observableCounter = new ConcurrentHashMap<>();

	public static void reset() {
		observableCounter = new ConcurrentHashMap<>();
		observerCounter = new ConcurrentHashMap<>();
	}

	/**
	 * retrieves the next id for a generated subscriber for the given compilation
	 * unit
	 * 
	 * @param icuName
	 *            name of the compilation unit
	 * @return next id
	 */
	public static String getNextObserverId(ICompilationUnit icuName) {
		incAndGetCounter(icuName, IdManager.observerCounter);
		return getId(icuName, IdManager.observerCounter);
	}

	/**
	 * retrieves the next id generated for a complex rx observable for the given
	 * compilation unit
	 * 
	 * @param icuName
	 *            name of the compilation unit
	 * @return next id
	 */
	public static String getNextObservableId(ICompilationUnit icuName) {
		incAndGetCounter(icuName, IdManager.observableCounter);
		return getId(icuName, IdManager.observableCounter);
	}

	/**
	 * retrieves the last id generated for a complex rx observable for the given
	 * compilation unit
	 * 
	 * @param icuName
	 * @return
	 */
	public static String getLastObservableId(ICompilationUnit icuName) {
		return getId(icuName, IdManager.observableCounter);
	}

	// ### Private Methods ###

	private static String getId(ICompilationUnit icuName, Map<ICompilationUnit, Integer> map) {
		Integer integer = map.get(icuName);
		if (integer == null || integer == INITIAL_ID) {
			return EMPTY;
		} else {
			return String.valueOf(integer);
		}
	}

	private static void incAndGetCounter(ICompilationUnit icuName, Map<ICompilationUnit, Integer> map) {
		Integer integer = map.get(icuName);
		if (integer == null) {
			map.put(icuName, INITIAL_ID);
		} else {
			map.put(icuName, integer + 1);
		}
	}

}
