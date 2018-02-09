package de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

public interface ExceptionIdentifier {
	

	boolean isHandledBy(Type type);
	
	default boolean isAnyException() {
		return this instanceof AnyExceptionIdentifier;
	}
	
	
	class SomeExceptionIdentifier implements ExceptionIdentifier {		
	
		private final String name;
	
		private SomeExceptionIdentifier(String name) {
			this.name = Objects.requireNonNull(name);
		}
		
		public static ExceptionIdentifier create(String name) {
			return new SomeExceptionIdentifier(name);
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			return o != null && o instanceof SomeExceptionIdentifier && ((SomeExceptionIdentifier) o).name.equals(name); 
		}
		
		@Override
		public String toString() {
			return "<" + name + ">";
		}
		
		public boolean isHandledBy(Type type) {
			//TODO Check for subtype
			ITypeBinding binding = type.resolveBinding();
			
			if (binding != null) {
				return name.equals(binding.getErasure().getQualifiedName());
			} else {
				return name.equals(type.toString());
			}
			
		}
	}
	
	class AnyExceptionIdentifier implements ExceptionIdentifier {
		
		private static AnyExceptionIdentifier instance = new AnyExceptionIdentifier();
		
		private AnyExceptionIdentifier() { }
		
		public static ExceptionIdentifier create() {
			return instance;
		}
		
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			return o != null && o instanceof AnyExceptionIdentifier; 
		}
		
		@Override
		public String toString() {
			return "<ANY>";
		}
		
		public boolean isHandledBy(Type type) {
			return true;			
		}
	}
		
	public static ExceptionIdentifier createFrom(ITypeBinding exceptionType) {
		return SomeExceptionIdentifier.create(exceptionType.getErasure().getQualifiedName());
	}	
	
	public static ExceptionIdentifier ANY_EXCEPTION = AnyExceptionIdentifier.create();
	
	
	public static ExceptionIdentifier ARITHMETIC_EXCEPTION = SomeExceptionIdentifier.create("java.lang.ArithmeticException");
	public static ExceptionIdentifier ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = SomeExceptionIdentifier.create("java.lang.ArrayIndexOutOfBoundsException");
	public static ExceptionIdentifier CLASS_CAST_EXCEPTION = SomeExceptionIdentifier.create("java.lang.ClassCastException");
	public static ExceptionIdentifier NEGATIVE_ARRAY_SIZE_EXCEPTION = SomeExceptionIdentifier.create("java.lang.NegativeArraySizeException");
	public static ExceptionIdentifier NULL_POINTER_EXCEPTION = SomeExceptionIdentifier.create("java.lang.NullPointerException");
	
}
