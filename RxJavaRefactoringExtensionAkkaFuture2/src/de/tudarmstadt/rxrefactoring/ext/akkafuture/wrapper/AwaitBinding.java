package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class AwaitBinding {

	private final MethodInvocation method;
	
	private AwaitBinding(MethodInvocation method) {
		this.method = method;
	}
	
	public static AwaitBinding create(MethodInvocation method) {
		if (Objects.equals(method.getName().getIdentifier(), "result")) {
			IMethodBinding methodBinding = method.resolveMethodBinding();
			
			if (methodBinding == null) 
				return null;
				
			ITypeBinding type = methodBinding.getDeclaringClass();			
			
			if (type == null)
				return null;
			
			String name = type.getBinaryName();		
			if (Objects.equals(name, "akka.dispatch.Await") || Objects.equals(name, "scala.concurrent.Await"))
				return new AwaitBinding(method);
			else
				return null;
		}
		
		return null;		
	}
	
	public MethodInvocation getMethodInvocation() {
		return method;
	}
	
	public Expression getFuture() {
		return (Expression) method.arguments().get(0);
	}
}
