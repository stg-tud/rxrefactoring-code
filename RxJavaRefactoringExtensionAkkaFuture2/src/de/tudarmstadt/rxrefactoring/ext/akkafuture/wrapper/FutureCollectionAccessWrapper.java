package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;

public class FutureCollectionAccessWrapper implements FutureMethodWrapper {
	
	/**
	 * The collection access method, e.g., coll.add(FUTURE)
	 */
	private final MethodInvocation method;
	
	
	public FutureCollectionAccessWrapper(MethodInvocation method) {
		this.method = method;
		
	}
	
	public static FutureCollectionAccessWrapper create(Expression expression) {
		if (!isCollectionAccess(expression))
			throw new IllegalArgumentException("You need to specify a collection access as parameter, but was: " + expression);
		
		return new FutureCollectionAccessWrapper((MethodInvocation) expression);
	}
	
	
	public MethodInvocation getMethodInvocation() {
		return method;
	}
		
	public boolean isAdd() {
		return Objects.equals(method.getName().getIdentifier(), "add");
	}
	
	public static boolean isCollectionAccess(Expression expression) {
		if (!(expression instanceof MethodInvocation) && expression == null)	
			return false;
		
		MethodInvocation method = (MethodInvocation) expression;
		
		if (method.getExpression() != null && AkkaFutureASTUtils.isCollectionOfFuture(method.getExpression().resolveTypeBinding()) && Objects.equals(method.getName().getIdentifier(), "add")) {
			return true;
		}
		
		return false;
	}
	
	
}
