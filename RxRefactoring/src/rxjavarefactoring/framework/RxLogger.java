package rxjavarefactoring.framework;

/**
 * Description: This class is responsible for managing logging task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public final class RxLogger
{
	private RxLogger()
	{

	}

	/**
	 * Uses System.out.println(...) to log in the console
	 * 
	 * @param currentClass
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
}
