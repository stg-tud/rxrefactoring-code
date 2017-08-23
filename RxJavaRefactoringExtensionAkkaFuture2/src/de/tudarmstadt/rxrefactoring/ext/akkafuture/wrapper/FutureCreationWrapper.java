package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;

/**
 * Models how a future is created.
 * 
 * @author mirko
 *
 */
public class FutureCreationWrapper {
	
	/**
	 * The expression that creates the future.
	 */
	private final Expression expression;
	
	private final ASTNode parent;
	
	public FutureCreationWrapper(Expression expression) {
		this.expression = expression;
		this.parent = expression.getParent();
		
		
		//Check expression for use cases...
		if (expression instanceof MethodInvocation) {
			//...
		}
		
	}
	
	public static FutureCreationWrapper create(Expression expression) {
		if (!isFutureCreation(expression))
			throw new IllegalArgumentException("You need to specify a Future creation as parameter, but was: " + expression);
		
		return new FutureCreationWrapper(expression);
	}
	
	
	/**
	 * Returns the enclosing collection if the creation is
	 * inside a {@code collection.add(creation)} expression.
	 * 
	 * @return collection of the enclosing add expression, or
	 * null if the creation is not inside such a statement.
	 */
	public Expression getEnclosingCollection() {
		MethodInvocation method = getParentAsMethodInvocation();
		
		if (method != null) {
			Expression expr = method.getExpression(); //has to be a collection
			if (expr != null) {
				ITypeBinding type = expr.resolveTypeBinding();
				if (type != null && ASTUtils.isTypeOf(type, "java.util.Collection")) {
					return expr;
				}
			}			
		}
		
		return null;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	public MethodInvocation getParentAsMethodInvocation() {
		if (parent instanceof MethodInvocation) {
			return (MethodInvocation) parent;
		}
		
		return null;
	}
	
	public static boolean isFutureCreation(Expression expression) {
		if (expression == null)
			return false;
		
		ITypeBinding returnType = expression.resolveTypeBinding();
		
		if (returnType != null && FutureTypeWrapper.isAkkaFuture(returnType) && expression instanceof MethodInvocation) {
			MethodInvocation method = (MethodInvocation) expression;
			IMethodBinding binding = method.resolveMethodBinding();
			
			if (binding != null && Objects.equals(method.getName().getIdentifier(), "ask") && AkkaFutureASTUtils.isPatterns(binding.getDeclaringClass()))
				return true;
			
			if (binding != null && Objects.equals(method.getName().getIdentifier(), "future") && AkkaFutureASTUtils.isFutures(binding.getDeclaringClass()))
				return true;
		}		
		
		return false;
		
	}
}
