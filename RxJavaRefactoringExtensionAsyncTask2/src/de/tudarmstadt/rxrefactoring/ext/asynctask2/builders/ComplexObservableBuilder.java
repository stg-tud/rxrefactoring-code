package de.tudarmstadt.rxrefactoring.ext.asynctask2.builders;

import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;

/**
 * Description: Builder to create inner classes as string<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/23/2016
 */
public final class ComplexObservableBuilder {
	private static final String NEW_LINE = "\n";
	private static final String EMPTY = "";

	private StringBuilder rxComplexObservableBuilder;
	private String idNumber;

	private ComplexObservableBuilder(String icuName) {
		idNumber = IdManager.getNextObservableId(icuName);
		rxComplexObservableBuilder = new StringBuilder();
		rxComplexObservableBuilder.append("private class ComplexRxObservable");
		rxComplexObservableBuilder.append(getNumber());
		rxComplexObservableBuilder.append("{");
	}

	/**
	 * Initializes the builder of a private class named "ComplexRxObservable".
	 * If there are multiple classes in the same scope, then the names are
	 * enumerated. i.e: ComplexRxObservable1
	 * 
	 * @return the builder
	 */
	public String getComplexObservableName() {
		return "ComplexRxObservable" + getNumber();
	}

	/**
	 * Initializes the builder of a private class named "ComplexRxObservable".
	 * If there are multiple classes in the same scope, then the names are
	 * enumerated. i.e: ComplexRxObservable1
	 * 
	 * @return the builder
	 * @param icuName
	 */
	public static ComplexObservableBuilder newComplexRxObservable(String icuName) {
		;
		return new ComplexObservableBuilder(icuName);
	}

	/**
	 * Adds field declarations to the class
	 * 
	 * @param fieldDeclarations
	 *            field declarations to be added
	 * @return the builder
	 */
	public ComplexObservableBuilder withFields(List<FieldDeclaration> fieldDeclarations) {
		for (FieldDeclaration field : fieldDeclarations) {
			rxComplexObservableBuilder.append(field.toString());
			rxComplexObservableBuilder.append(NEW_LINE);
		}
		return this;
	}

	/**
	 * Adds a GetAsyncObservable Method given the type of the
	 * {@link rx.Observable} and its statements
	 * 
	 * @param type
	 *            type of the {@link rx.Observable}
	 * @param statements
	 *            statemens to be added in the method
	 * @return the builder
	 */
	public ComplexObservableBuilder withGetAsyncObservable(String type, String... statements) {
		rxComplexObservableBuilder.append("public Observable<");
		rxComplexObservableBuilder.append(type);
		rxComplexObservableBuilder.append("> getAsyncObservable");
		rxComplexObservableBuilder.append(getNumber());
		rxComplexObservableBuilder.append("() {");
		rxComplexObservableBuilder.append(NEW_LINE);
		rxComplexObservableBuilder.append("");
		for (String statement : statements) {

			rxComplexObservableBuilder.append(statement);
			rxComplexObservableBuilder.append(NEW_LINE);
		}
		rxComplexObservableBuilder.append("}");
		return this;
	}

	/**
	 * Adds a method to the class
	 * 
	 * @param method
	 *            method as string
	 * @return the builder
	 */
	public ComplexObservableBuilder withMethod(String method) {
		rxComplexObservableBuilder.append(method);
		return this;
	}

	/**
	 * Adds methods to a class given a list of {@link MethodDeclaration}
	 * 
	 * @param methods
	 *            the methods to be added
	 * @return the builder
	 */
	public ComplexObservableBuilder withMethods(List<MethodDeclaration> methods) {
		for (MethodDeclaration method : methods) {
			rxComplexObservableBuilder.append(method.toString());
			rxComplexObservableBuilder.append(NEW_LINE);
		}
		return this;
	}

	/**
	 * Finishes building the class
	 * 
	 * @return string representing the class
	 */
	public String build() {
		rxComplexObservableBuilder.append("}");
		return rxComplexObservableBuilder.toString();
	}

	/**
	 * Method which returns the asyncmethod name which will be used in place of
	 * AsyncTask.execute() method
	 * 
	 * @return
	 */
	public String getAsyncmethodName() {
		return "getAsyncObservable" + getNumber();
	}

	/**
	 * Method which returns the asyncmethod name which will be used in place of
	 * AsyncTask.execute() method
	 * 
	 * @return
	 */
	public String getAsynSubscription() {
		return "subscription" + getNumber();
	}
	// ### Private Methods ###

	private String getNumber() {
		return idNumber;
	}

}
