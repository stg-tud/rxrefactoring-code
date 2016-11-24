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
public final class IdsManager
{
	private static final String EMPTY = "";
	private static final int INITIAL_ID = 0;

	private static Map<String, Integer> subscriberCounter = new ConcurrentHashMap<>();
	private static Map<String, Integer> complexObservableCounter = new ConcurrentHashMap<>();

	/**
	 * retrieves the next id for a generated subscriber considering
	 * the compilation unit
	 * 
	 * @param icuName
	 *            name of the compilation unit
	 * @return next id
	 */
	public static String getNextSubscriberId( String icuName )
	{
		incAndGetCounter( icuName, IdsManager.subscriberCounter );
		return getNextId( icuName, IdsManager.subscriberCounter );
	}

	/**
	 * retrieves the next id for a generated complex rx observable considering
	 * the compilation unit
	 * 
	 * @param icuName
	 *            name of the compilation unit
	 * @return next id
	 */
	public static String getNextComplexObsId( String icuName )
	{
		incAndGetCounter( icuName, IdsManager.complexObservableCounter );
		return getNextId( icuName, IdsManager.complexObservableCounter );
	}

	private static String getNextId( String icuName, Map<String, Integer> map )
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
