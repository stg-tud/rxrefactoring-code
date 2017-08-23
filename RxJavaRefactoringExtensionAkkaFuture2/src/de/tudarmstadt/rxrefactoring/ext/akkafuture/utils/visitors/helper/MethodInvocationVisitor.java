package de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.visitors.helper;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.FutureCollector;

/**
 * Looks for the first method invocation and checks whether the method is part of the project or not.
 *
 */
public class MethodInvocationVisitor extends ASTVisitor {

	private FutureCollector collector;
	private String group;
	
	private MethodInvocation methodInvocation;
	private boolean isExternalMethod;
	
	public MethodInvocationVisitor(FutureCollector collector, String group) {
		this.collector = collector;
		this.group = group;
		
		isExternalMethod = false;
	}
	
	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		
		this.methodInvocation = methodInvocation;

		isExternalMethod = !collector.containsMethodDeclaration(
				group,
				methodInvocation.resolveMethodBinding());
		
		return false;
	}

	public Optional<Boolean> isExternalMethod() {
		if(methodInvocation == null)
			return Optional.empty();
		
		return Optional.of(isExternalMethod);
	}
	
	public Optional<MethodInvocation> getMethodInvocation() {
		return Optional.ofNullable(methodInvocation);
	}
}
