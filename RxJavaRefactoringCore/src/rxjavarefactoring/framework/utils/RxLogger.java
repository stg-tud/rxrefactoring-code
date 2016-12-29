package rxjavarefactoring.framework.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.ui.console.*;

import rxjavarefactoring.RxJavaRefactoringApp;

/**
 * Description: This class is responsible for managing logging task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public final class RxLogger
{
	private static final String CONSOLE_NAME = "Rx Java Refactoring";

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
		if ( !RxJavaRefactoringApp.isRunningForTests() )
		{
			MessageConsole myConsole = findConsole( CONSOLE_NAME );
			MessageConsoleStream out = myConsole.newMessageStream();
			out.println( "[ INFO ] " + currentClass.getClass().getSimpleName() + ": " + text );
		}
		else
		{
			System.out.println( "[ INFO ] " + currentClass.getClass().getSimpleName() + ": " + text );
		}
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
		if ( !RxJavaRefactoringApp.isRunningForTests() )
		{
			String errorMessage = "[ ERROR ] " + currentClass.getClass().getSimpleName() + ": " + text;
			printErrorToConsole( errorMessage, throwable );
		}
		else
		{
			System.err.println( "[ ERROR ] " + currentClass.getClass().getSimpleName() + ": " + text );
			throwable.printStackTrace();
		}
	}

	/**
	 * Logs exceptions occurred in client (extensions)
	 *
	 * @param throwable
	 *            exception
	 */
	public static void notifyExceptionInClient( Throwable throwable )
	{
		if ( !RxJavaRefactoringApp.isRunningForTests() )
		{
			String errorMessage = "[ ERROR ] Exception in client";
			printErrorToConsole( errorMessage, throwable );
		}
		else
		{
			System.err.println( "[ ERROR ] Exception in client" );
			throwable.printStackTrace();
		}
	}

	private static MessageConsole findConsole( String name )
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for ( int i = 0; i < existing.length; i++ )
			if ( name.equals( existing[ i ].getName() ) )
				return (MessageConsole) existing[ i ];

		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole( name, null );
		conMan.addConsoles( new IConsole[] { myConsole } );
		return myConsole;
	}

	private static void printErrorToConsole( String errorMessage, Throwable throwable )
	{
		MessageConsole myConsole = findConsole( CONSOLE_NAME );
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println( errorMessage );

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		throwable.printStackTrace( pw );

		out.println( sw.toString() );
	}
}
