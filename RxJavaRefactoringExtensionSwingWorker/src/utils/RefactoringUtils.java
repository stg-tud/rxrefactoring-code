package utils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016
 */
public final class RefactoringUtils
{
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
	 * @param swingworkerVarName
	 *            string containing the char sequence "swingworker"
	 *            (case insensitive)
	 * @return updated string
	 */
	public static String getNewVarName( String swingworkerVarName )
	{
		String newVarName = swingworkerVarName;
		String searchableVarName = swingworkerVarName.toUpperCase();
		int swingworkerPosition = searchableVarName.indexOf( "SWINGWORKER" );
		if ( swingworkerPosition >= 0 )
		{

			char[] chars = swingworkerVarName.toCharArray();
			char firstLetter = chars[ swingworkerPosition ];
			if ( firstLetter == 's' )
			{
				newVarName = swingworkerVarName.replaceAll( "(?i)swingworker", "rxObserver" );
			}
			else if ( firstLetter == 'S' )
			{
				newVarName = swingworkerVarName.replaceAll( "(?i)swingworker", "RxObserver" );
			}
			return newVarName;
		}

		int workerPosition = searchableVarName.indexOf( "WORKER" );
		if ( workerPosition >= 0 )
		{
			char[] chars = swingworkerVarName.toCharArray();
			char firstLetter = chars[ workerPosition ];
			if ( firstLetter == 'w' )
			{
				newVarName = swingworkerVarName.replaceAll( "(?i)worker", "rxObserver" );
			}
			else if ( firstLetter == 'W' )
			{
				newVarName = swingworkerVarName.replaceAll( "(?i)worker", "RxObserver" );
			}
			return newVarName;
		}

		return newVarName;
	}
}
