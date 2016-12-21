package domain;

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
public class SwingWorkerInfo
{
	private static final String binaryName = "javax.swing.SwingWorker";
	private static final Map<String, String> publicMethodsMap = new HashMap<String, String>()
	{
		{
			put( "addPropertyChangeListener", "addPropertyChangeListener" );
			put( "cancel", "cancelObservable" );
			put( "execute", "executeObservable" );
			put( "firePropertyChange", "firePropertyChange" );
			put( "get", "get" );
			put( "getProgress", "getProgress" );
			put( "getPropertyChangeSupport", "getPropertyChangeSupport" );
			put( "getState", "getState" );
			put( "isCancelled", "isCancelled" );
			put( "isDone", "isDone" );
			put( "removePropertyChangeListener", "removePropertyChangeListener" );
			put( "run", "runObservable" );
		}
	};

	/**
	 *
	 * @return full name of the class. i.e: android.os.AsyncTask
	 */
	public static String getBinaryName()
	{
		return binaryName;
	}

	/**
	 *
	 * @return map containing public methods and its equivalent (if any) when
	 *         using RxJava
	 */
	public static Map<String, String> getPublicMethodsMap()
	{
		return publicMethodsMap;
	}
}
