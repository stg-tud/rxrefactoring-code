package rxjavarefactoring.framework.utils;

/**
 * Description: This class is responsible for managing logging task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public final class RxLogger
{
	private RxLogger()
	{
		// This class should not be instantiated
	}

	/**
	 * Uses System.out.println(...) to log in the console
	 * 
	 * @param currentClass
	 *            class invoking the method
	 * @param text
	 *            logging text
	 */
	public static void info( Object currentClass, String text )
	{
		System.out.println( "[ INFO ] " + currentClass.getClass().getSimpleName() + ": " + text );
	}

	/**
	 * Uses System.err.println(...) to log in the console and prints stack trace
	 * 
	 * @param currentClass
	 *            class invoking the method
	 * @param text
	 *            logging text
	 * @param throwable
	 *            exception
	 */
	public static void error( Object currentClass, String text, Throwable throwable )
	{
		System.err.println( "[ ERROR ] " + currentClass.getClass().getSimpleName() + ": " + text );
		throwable.printStackTrace();
	}

	/**
	 * Logs exceptions occurred in client (extensions)
	 * 
	 * @param throwable
	 *            exception
	 */
	public static void notifyExceptionInClient( Throwable throwable )
	{
		System.err.println( "[ ERROR ] Exception in client" );
		throwable.printStackTrace();
	}
}
