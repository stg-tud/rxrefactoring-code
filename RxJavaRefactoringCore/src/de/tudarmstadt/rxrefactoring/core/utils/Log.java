package de.tudarmstadt.rxrefactoring.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.ui.console.*;

/**
 * Description: This class is responsible for managing logging task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public final class Log
{
	private static final String CONSOLE_NAME = "Rx Java Refactoring";

	private Log()
	{
		// This class should not be instantiated
	}

	/**
	 * Uses System.out.println(...) to log in the console
	 *
	 * @param currentObject
	 *            class invoking the method
	 * @param text
	 *            logging text
	 */
	public static void info(Object currentObject, Object text)	{
		System.out.println( "[ INFO ] " + convertObjectName(currentObject) + ": " + text );
	}
	
	public static void info(Class<?> cls, Object text) {
		System.out.println( "[ INFO ] " + convertClassName(cls) + ": " + text );
	}

	
	/**
	 * Uses System.err.println(...) to log in the console and prints stack trace
	 *
	 * @param currentObject
	 *            class invoking the method
	 * @param text
	 *            logging text
	 * @param throwable
	 *            exception
	 */
	public static void error(Object currentObject, Object text, Throwable throwable) {
		System.err.println( "[ ERROR ] " + convertObjectName(currentObject) + ": " + text );
		
		if (Objects.nonNull(throwable))
			throwable.printStackTrace();
	}
	
	/**
	 * Produces the name of the class of an object.
	 * @param clss 
	 * @return
	 */
	private static String convertObjectName(Object obj) {
		if (obj == null)
			return "<NONE>";
		else
			return convertClassName(obj.getClass());
	}
	
	private static String convertClassName(Class<?> cls) {
		return cls.getSimpleName();
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

	/**
	 * Shows text in the console of the Eclipse instance that's
	 * running the plugin
	 * 
	 * @param currentClass
	 *            current class
	 * @param text
	 *            message text
	 */
	public static void showInConsole( Object currentClass, Object text )
	{
		MessageConsole myConsole = findConsole( CONSOLE_NAME );
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println( "[ INFO ] " + currentClass.getClass().getSimpleName() + ": " + text );
	}

	// ### Private Methods ###

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

	private static void printErrorToConsole( Object errorMessage, Throwable throwable )
	{
		MessageConsole myConsole = findConsole( CONSOLE_NAME );
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println( Objects.toString(errorMessage) );

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		throwable.printStackTrace( pw );

		out.println( sw.toString() );
	}
}
