package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016<br>
 * Adapted to new core by Camila Gonzalez on 18/01/2018
 */
public final class RefactoringUtils
{

	private static final String SWING_WORKER_STATE_VALUE = "SwingWorker.StateValue";
	private static final String RX_OBSERVER_FIRST_LOWER = "rxObserver";
	private static final String RX_OBSERVER_FIRST_UPPER = "RxObserver";
	private static final String SWING_WORKER_UPPER = "SWINGWORKER";
	private static final String WORKER_UPPER = "WORKER";

	private RefactoringUtils()
	{
		// This class should not be instantiated
	}

	/**
	 * Returns a string where the char sequence "swingworker" or
	 * "worker" was replaced by rxObservable. Example:<br>
	 * <ul>
	 * <li>swingworker -> rxObservable</li>
	 * <li>swingWorker -> rxObservable</li>
	 * <li>mySwingWorker -> myRxObservable</li>
	 * <li>mySwingWorker10 -> myRxObservable10</li>
	 * </ul>
	 * 
	 * @param text
	 *            string containing the char sequence "swingworker"
	 *            (case insensitive)
	 * @return updated string
	 */
	public static String cleanSwingWorkerName( String text )
	{
		StringBuilder sb = new StringBuilder( text );
		String searchableText = text.toUpperCase();

		int startIndex = 0;
		while ( startIndex != -1 )
		{
			int swPosition = searchableText.indexOf( SWING_WORKER_UPPER, startIndex );
			if ( swPosition == -1 )
			{
				break;
			}
			int swStatePosition = text.indexOf( SWING_WORKER_STATE_VALUE, startIndex );
			if ( swPosition != swStatePosition )
			{
				int endIndex = swPosition + SWING_WORKER_UPPER.length();
				if ( text.charAt( swPosition ) == 's' )
				{
					sb.replace( swPosition, endIndex, RX_OBSERVER_FIRST_LOWER );
				}
				else
				{
					sb.replace( swPosition, endIndex, RX_OBSERVER_FIRST_UPPER );
				}
				startIndex = swPosition + RX_OBSERVER_FIRST_LOWER.length();
				searchableText = sb.toString().toUpperCase();
				text = sb.toString();
			}
			else
			{
				startIndex = searchableText.indexOf( SWING_WORKER_UPPER, SWING_WORKER_STATE_VALUE.length() + 1 );
			}
		}

		startIndex = 0;
		searchableText = sb.toString().toUpperCase();
		while ( startIndex != -1 )
		{
			int swPosition = searchableText.indexOf( WORKER_UPPER, startIndex );
			if ( swPosition == -1 )
			{
				break;
			}
			int swStatePosition = text.indexOf( SWING_WORKER_STATE_VALUE, startIndex );
			if ( swStatePosition != ( swPosition - SWING_WORKER_UPPER.length() + WORKER_UPPER.length() ) )
			{
				int endIndex = swPosition + WORKER_UPPER.length();
				if ( text.charAt( swPosition ) == 'w' )
				{
					sb.replace( swPosition, endIndex, RX_OBSERVER_FIRST_LOWER );
				}
				else
				{
					sb.replace( swPosition, endIndex, RX_OBSERVER_FIRST_UPPER );
				}
				startIndex = swPosition + RX_OBSERVER_FIRST_LOWER.length();
				searchableText = sb.toString().toUpperCase();
				text = sb.toString();
			}
			else
			{
				startIndex = searchableText.indexOf( WORKER_UPPER, SWING_WORKER_STATE_VALUE.length() + 1 );
			}
		}

		return sb.toString();
	}
	
	/**
	 * Uses {@link SwingWorkerInfo} to determine the method name to be used
	 * after refactoring. If no name for methodName is found in {@link SwingWorkerInfo},
	 * then it means, that the method belongs to a custom implementation and
	 * therefore the same name is returned.
	 * 
	 * @param methodName
	 *            original method name
	 * @return method name to be used after refactoring
	 */
	public static String getNewMethodName( String methodName )
	{
		String newMethodName = SwingWorkerInfo.getPublicMethodsMap().get( methodName );

		if ( newMethodName == null )
		{
			newMethodName = methodName;
		}
		return newMethodName;
	}
}
