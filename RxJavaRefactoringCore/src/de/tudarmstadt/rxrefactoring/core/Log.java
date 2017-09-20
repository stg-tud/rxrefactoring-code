package de.tudarmstadt.rxrefactoring.core;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Description: This class is responsible for managing logging task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public final class Log {

	private static final String CONSOLE_NAME = "Rx Java Refactoring";

	public static final PrintStream INFO = System.out;
	public static final PrintStream ERR = System.out;
	public static final PrintStream PLUGIN_INFO = System.out; // new
																// PrintStream(findConsole(CONSOLE_NAME).newOutputStream());

	private static final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

	// This class should not be instantiated
	private Log() {
	}

	/**
	 * Uses System.out.println(...) to log in the console
	 *
	 * @param currentObject
	 *            class invoking the method
	 * @param text
	 *            logging text
	 * 
	 */
	private static void info(PrintStream out, Class<?> cls, Object text) {
		String className = convertClassName(cls);
		for (String splitted : Objects.toString(text).split("\n")) {
			println(out, "INFO", className, splitted);
		}
		
	}

	public static void info(Class<?> cls, Object text) {
		info(INFO, cls, text);
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
	private static void error(PrintStream out, Class<?> cls, Object text, Throwable throwable) {
		String className = convertClassName(cls);
		for (String splitted : Objects.toString(text).split("\n")) {
			println(out, "ERR", className, splitted);
		}

		if (Objects.nonNull(throwable)) {
			throwable.printStackTrace(out);
		}

	}

	public static void error(Class<?> cls, Object text, Throwable throwable) {
		error(ERR, cls, text, throwable);
	}

	public static void error(Class<?> cls, Object text) {
		error(cls, text, null);
	}

	public static void errorInClient(Class<?> cls, Throwable throwable) {
		error(PLUGIN_INFO, cls, "An error occured:", throwable);
	}
	
	@Deprecated
	public static void handleException(Class<?> cls, String when, Throwable throwable) {
		info(cls, "### ERROR DURING " + when + " ###");
		throwable.printStackTrace(INFO);
		info(cls, "### FINISH ###");
	}
	
	
	private static String convertClassName(Class<?> cls) {
		Objects.requireNonNull(cls, "Class can not be null.");
		return cls.getSimpleName();
	}

	private static void println(PrintStream out, String tag, String cls, String text) {
		out.println(toConsoleLine(tag, cls, text));
	}
	
	private static String toConsoleLine(String tag, String cls, String text) {
		return "[" + format.format(new Date()) + "][" + tag + "][" + cls + "]: " + text;
	}
	

	/**
	 * Shows text in the console of the Eclipse instance that's running the plugin
	 * 
	 * @param currentClass
	 *            current class
	 * @param text
	 *            message text
	 */
	@Deprecated
	public static void showInConsole(Object currentClass, Object text) {
		MessageConsole myConsole = findConsole(CONSOLE_NAME);
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println("[ INFO ] " + currentClass.getClass().getSimpleName() + ": " + text);
	}

	// ### Private Methods ###

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];

		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	// private static void printErrorToConsole( Object errorMessage, Throwable
	// throwable )
	// {
	// MessageConsole myConsole = findConsole( CONSOLE_NAME );
	// MessageConsoleStream out = myConsole.newMessageStream();
	// out.println( Objects.toString(errorMessage) );
	//
	// StringWriter sw = new StringWriter();
	// PrintWriter pw = new PrintWriter( sw );
	// throwable.printStackTrace( pw );
	//
	// out.println( sw.toString() );
	// }
}
