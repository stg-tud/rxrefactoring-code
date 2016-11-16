package rxjavarefactoring.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Contains information about classes.<br>
 * Specifically:
 * <ul>
 * <li>Class binary name (including package)</li>
 * <li>Class public methods</li>
 * <li>Equivalent methods in RxJava of the public methods. Set null if there is
 * no equivalent</li>
 * </ul>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public enum ClassDetails
{
	ASYNC_TASK(
			"android.os.AsyncTask",
			new HashMap<String, String>()
			{
				{
					put( "cancel", "unsubscribe" );
					put( "execute", "subscribe" );
					put( "executeOnExecutor", "subscribe" );
					put( "get", null );
					put( "getStatus", null );
					put( "isCancelled", "isUnsubscribed" );
				}
			} );

	private final String binaryName;
	private final Map<String, String> publicMethodsMap;

	ClassDetails( String binaryName, Map<String, String> publicMethodsMap )
	{
		this.binaryName = binaryName;
		this.publicMethodsMap = publicMethodsMap;
	}

	/**
	 *
	 * @return full name of the class. i.e: android.os.AsyncTask
	 */
	public String getBinaryName()
	{
		return binaryName;
	}

	/**
	 *
	 * @return map containing public methods and its equivalent (if any) when
	 *         using RxJava
	 */
	public Map<String, String> getPublicMethodsMap()
	{
		return publicMethodsMap;
	}
}
