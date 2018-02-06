package de.tudarmstadt.rxrefactoring.core.analysis.cfg.exception;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class ExceptionIdentifier {

	private final String name;

	public ExceptionIdentifier(String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	public static ExceptionIdentifier createFrom(ITypeBinding exceptionType) {
		return new ExceptionIdentifier(exceptionType.getQualifiedName());
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof ExceptionIdentifier && ((ExceptionIdentifier) o).name.equals(name); 
	}
	
	@Override
	public String toString() {
		return "<" + name + ">";
	}

	
	
	public static ExceptionIdentifier ANY_EXCEPTION = new ExceptionIdentifier("java.lang.Exception");
	
	
	public static ExceptionIdentifier ARITHMETIC_EXCEPTION = new ExceptionIdentifier("java.lang.ArithmeticException");
	public static ExceptionIdentifier NEGATIVE_ARRAY_SIZE_EXCEPTION = new ExceptionIdentifier("java.lang.NegativeArraySizeException");
	public static ExceptionIdentifier NULL_POINTER_EXCEPTION = new ExceptionIdentifier("java.lang.NullPointerException");
	
}
