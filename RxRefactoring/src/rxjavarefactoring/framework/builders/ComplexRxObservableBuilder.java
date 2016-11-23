package rxjavarefactoring.framework.builders;

import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Description: Builder to create inner classes as string<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/23/2016
 */
public final class ComplexRxObservableBuilder
{
	private static final String NEW_LINE = "\n";
	private static final String EMPTY = "";

	private static StringBuilder rxComplexObservableBuilder;
	private static int counter = -1;

	private ComplexRxObservableBuilder()
	{

	}

	/**
	 * Resets the counter used to create unique subscriber names
	 */
	public static void resetCounter()
	{
		counter = -1;
	}

	/**
	 * Initializes the builder of a private class named "ComplexRxObservable".
	 * If there are multiple classes in the same scope, then the names are
	 * enumerated. i.e: ComplexRxObservable1
	 * 
	 * @return the builder
	 */
	public static ComplexRxObservableBuilder newComplexRxObservable()
	{
		counter++;
		rxComplexObservableBuilder = new StringBuilder();
		rxComplexObservableBuilder.append( "private class ComplexRxObservable" );
		rxComplexObservableBuilder.append( getNumber() );
		rxComplexObservableBuilder.append( "{" );
		return new ComplexRxObservableBuilder();
	}

	/**
	 * Adds field declarations to the class
	 * 
	 * @param fieldDeclarations
	 *            field declarations to be added
	 * @return the builder
	 */
	public ComplexRxObservableBuilder withFields( List<FieldDeclaration> fieldDeclarations )
	{
		for ( FieldDeclaration field : fieldDeclarations )
		{
			rxComplexObservableBuilder.append( field.toString() );
			rxComplexObservableBuilder.append( NEW_LINE );
		}
		return this;
	}

	/**
	 * Adds a GetAsyncObservable Method given the type of the {@link rx.Observable} and its statements
	 * 
	 * @param type
	 *            type of the {@link rx.Observable}
	 * @param statements
	 *            statemens to be added in the method
	 * @return the builder
	 */
	public ComplexRxObservableBuilder withGetAsyncObservable( String type, String... statements )
	{
		rxComplexObservableBuilder.append( "public Observable<" );
		rxComplexObservableBuilder.append( type );
		rxComplexObservableBuilder.append( "> getAsyncObservable" );
		rxComplexObservableBuilder.append( getNumber() );
		rxComplexObservableBuilder.append( "() {" );
		for ( String statement : statements )
		{

			rxComplexObservableBuilder.append( statement );
			rxComplexObservableBuilder.append( NEW_LINE );
		}
		rxComplexObservableBuilder.append( "}" );
		return this;
	}

	/**
	 * Adds a method to the class
	 * 
	 * @param method
	 *            method as string
	 * @return the builder
	 */
	public ComplexRxObservableBuilder withMethod( String method )
	{
		rxComplexObservableBuilder.append( method );
		return this;
	}

	/**
	 * Adds methods to a class given a list of {@link MethodDeclaration}
	 * 
	 * @param methods
	 *            the methods to be added
	 * @return the builder
	 */
	public ComplexRxObservableBuilder withMethods( List<MethodDeclaration> methods )
	{
		for ( MethodDeclaration method : methods )
		{
			rxComplexObservableBuilder.append( method.toString() );
			rxComplexObservableBuilder.append( NEW_LINE );
		}
		return this;
	}

	/**
	 * Finishes building the class
	 * 
	 * @return string representing the class
	 */
	public String build()
	{
		rxComplexObservableBuilder.append( "}" );
		return rxComplexObservableBuilder.toString();
	}

	// ### Private Methods ###

	private static String getNumber()
	{
		String number = String.valueOf( counter );
		if ( counter == 0 )
		{
			number = EMPTY;
		}
		return number;
	}

}
