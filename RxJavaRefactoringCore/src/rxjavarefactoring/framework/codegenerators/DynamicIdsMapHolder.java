package rxjavarefactoring.framework.codegenerators;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: This class is responsible for generating next ids
 * for new elements thread safely. The ids are unique for a compilation
 * unit<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/23/2016
 */
public final class DynamicIdsMapHolder
{
	private static final String EMPTY = "";
	private static final int INITIAL_ID = 0;

	private static Map<String, Integer> observerCounter = new ConcurrentHashMap<>();
	private static Map<String, Integer> observableCounter = new ConcurrentHashMap<>();

	public static void reset()
	{
		observableCounter = new ConcurrentHashMap<>();
		observerCounter = new ConcurrentHashMap<>();
	}

	/**
	 * retrieves the next id for a generated subscriber for the given
	 * compilation unit
	 * 
	 * @param icuName
	 *            name of the compilation unit
	 * @return next id
	 */
	public static String getNextObserverId( String icuName )
	{
		incAndGetCounter( icuName, DynamicIdsMapHolder.observerCounter );
		return getId( icuName, DynamicIdsMapHolder.observerCounter );
	}

	/**
	 * retrieves the next id generated for a complex rx observable for the given
	 * compilation unit
	 * 
	 * @param icuName
	 *            name of the compilation unit
	 * @return next id
	 */
	public static String getNextObservableId( String icuName )
	{
		incAndGetCounter( icuName, DynamicIdsMapHolder.observableCounter );
		return getId( icuName, DynamicIdsMapHolder.observableCounter );
	}

	/**
	 * retrieves the last id generated for a complex rx observable for the
	 * given compilation unit
	 * 
	 * @param icuName
	 * @return
	 */
	public static String getLastObservableId( String icuName )
	{
		return getId( icuName, DynamicIdsMapHolder.observableCounter );
	}

	private static String getId( String icuName, Map<String, Integer> map )
	{
		Integer integer = map.get( icuName );
		if ( integer == null || integer == INITIAL_ID )
		{
			return EMPTY;
		}
		else
		{
			return String.valueOf( integer );
		}
	}

	private static void incAndGetCounter( String icuName, Map<String, Integer> map )
	{
		Integer integer = map.get( icuName );
		if ( integer == null )
		{
			map.put( icuName, INITIAL_ID );
		}
		else
		{
			map.put( icuName, integer + 1 );
		}
	}

}
