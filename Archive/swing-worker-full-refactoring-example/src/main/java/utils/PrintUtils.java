package utils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public final class PrintUtils
{
	public static void printMessage( String message )
	{
		System.out.println( "[" + Thread.currentThread().getName() + "]" + " - " + message );
	}
}
