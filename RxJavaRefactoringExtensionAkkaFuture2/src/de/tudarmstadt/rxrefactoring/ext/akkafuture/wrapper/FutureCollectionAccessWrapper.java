package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;

public class FutureCollectionAccessWrapper {
	
	/**
	 * The collection access method, e.g., coll.add(FUTURE)
	 */
	private final MethodInvocation method;
	
	
	public FutureCollectionAccessWrapper(MethodInvocation method) {
		this.method = method;
		
	}
	
	public static FutureCollectionAccessWrapper create(MethodInvocation expression) {
		if (!isCollectionAccess(expression))
			throw new IllegalArgumentException("You need to specify a collection access as parameter, but was: " + expression);
		
		return new FutureCollectionAccessWrapper(expression);
	}
	
	
	public MethodInvocation getMethodInvocation() {
		return method;
	}
		
	public boolean isAdd() {
		return Objects.equals(method.getName().getIdentifier(), "add");
	}
	
	public static boolean isCollectionAccess(MethodInvocation method) {
		if (method == null)	return false;
		
		if (method.getExpression() != null && AkkaFutureASTUtils.isCollectionOfFuture(method.getExpression().resolveTypeBinding()) && Objects.equals(method.getName().getIdentifier(), "add")) {
			return true;
		}
		
		return false;
	}
	
	
}
