package de.tudarmstadt.rxrefactoring.core.utils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;

public class Expressions {

	/**
	 * Resolves the variable binding of an expression if the expression represents
	 * a variable access.
	 * 
	 * @param expr The variable expression for which the binding should be resolved. 
	 * @return The variable binding of the expression, or null if the expression is not a variable.
	 */
	public static IVariableBinding resolveVariableBinding(Expression expr) {
		if (expr instanceof Name) {
			IBinding binding = ((Name) expr).resolveBinding();
			
			if (binding instanceof IVariableBinding) {
				return (IVariableBinding) binding;
			}
		} else if (expr instanceof FieldAccess) {
			return ((FieldAccess) expr).resolveFieldBinding();
		}
		
		return null;
	}
	
	
}
