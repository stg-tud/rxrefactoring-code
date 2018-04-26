package de.tudarmstadt.rxrefactoring.core.utils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
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
			
			if (binding == null) {
				
			} else if (binding instanceof IVariableBinding) {
				return (IVariableBinding) binding;
			} 
		} else if (expr instanceof FieldAccess) {
			return ((FieldAccess) expr).resolveFieldBinding();
		} else if (expr instanceof ArrayAccess) {
			return resolveVariableBinding(((ArrayAccess) expr).getArray());
		}
		
		return null;
	}
	
	private class NamedVariableBinding implements IVariableBinding {

		private final int kind;
		
		public NamedVariableBinding(int kind) {
			this.kind = kind;
		}
		
		@Override
		public IAnnotationBinding[] getAnnotations() {			
			return new IAnnotationBinding[0];
		}

		@Override
		public int getKind() {			
			return kind;
		}

		@Override
		public int getModifiers() {
			return 0;
		}

		@Override
		public boolean isDeprecated() {
			return false;
		}

		@Override
		public boolean isRecovered() {
			return false;
		}

		@Override
		public boolean isSynthetic() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public IJavaElement getJavaElement() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getKey() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEqualTo(IBinding binding) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isField() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEnumConstant() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isParameter() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ITypeBinding getDeclaringClass() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ITypeBinding getType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getVariableId() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getConstantValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IMethodBinding getDeclaringMethod() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IVariableBinding getVariableDeclaration() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEffectivelyFinal() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	
}
