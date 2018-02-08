package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public final class Methods {
	
	private Methods() { }

	/**
	 * Checks whether a method matches the given signature.
	 * 
	 * @param mb The binding of the method to check.
	 * @param className The declaring class of the method, e.g., {@code java.lang.String}, or {@code null} if the class should not be checked.
	 * @param methodName The name of the method, e.g., {@code equals}.
	 * @param parameterTypeNames The names of the parameter types as qualified name, e.g., {@code java.lang.Object} or {@code boolean}.
	 * 
	 * @return False, if method does not match the description or method is {@code null}.
	 */
	public static boolean hasSignature(IMethodBinding mb, @Nullable String className, @NonNull String methodName, String... parameterTypeNames) {
		
		if (mb == null) {
			return false;
		}
			
		ITypeBinding[] mbParameters = mb.getParameterTypes();
		boolean result = 
				(className == null || Types.isExactTypeOf(mb.getDeclaringClass(), className))
				&& Objects.equals(mb.getName(), methodName)
				&& parameterTypeNames.length == mbParameters.length;

		if (!result) {
			return false;
		}
		
		for (int i = 0; i < parameterTypeNames.length; i++) {
			if (!Types.isExactTypeOf(mbParameters[i], parameterTypeNames[i])) {
				return false;
			}
		}

		return true;	
	}
	
	
	
	
}
