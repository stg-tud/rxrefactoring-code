package codegenerators;

import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import rxjavarefactoring.framework.codegenerators.IdsManager;

/**
 * Description: Builder to create inner classes as string<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/23/2016
 */
public final class ComplexRxObservableBuilder
{
	private static final String NEW_LINE = "\n";
	private static final String EMPTY = "";

	private StringBuilder rxComplexObservableBuilder;
	private String idNumber;

	private ComplexRxObservableBuilder( String icuName )
	{
		idNumber = IdsManager.getNextComplexObsId( icuName );
		rxComplexObservableBuilder = new StringBuilder();
		rxComplexObservableBuilder.append( "private class ComplexRxObservable" );
		rxComplexObservableBuilder.append( getNumber() );
		rxComplexObservableBuilder.append( "{" );
	}

	/**
	 * Initializes the builder of a private class named "ComplexRxObservable".
	 * If there are multiple classes in the same scope, then the names are
	 * enumerated. i.e: ComplexRxObservable1
	 * 
	 * @return the builder
	 * @param icuName
	 */
	public static ComplexRxObservableBuilder newComplexRxObservable(String icuName )
	{
		;
		return new ComplexRxObservableBuilder( icuName );
	}

	/**
	 * Adds field declarations to the class
	 * 
	 * @param fieldDeclarations
	 *            field declarations to be added
	 * @return the builder
	 */
	public ComplexRxObservableBuilder withFields(List<FieldDeclaration> fieldDeclarations )
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
	public ComplexRxObservableBuilder withGetAsyncObservable(String type, String... statements )
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
	public ComplexRxObservableBuilder withMethod(String method )
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
	public ComplexRxObservableBuilder withMethods(List<MethodDeclaration> methods )
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

	private String getNumber()
	{
		return idNumber;
	}

}
