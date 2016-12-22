package utils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016
 */
public final class RefactoringUtils
{

	public static final int SWINGWORKER_LENGHT = 11;

	private RefactoringUtils()
	{

	}

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
