package de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;

/**
 * Looks for the first method invocation and checks whether the method is part
 * of the project or not.
 *
 */
public class MethodInvocationVisitor extends ASTVisitor {

	private FutureCollector collector;
	private String group;

	private MethodInvocation methodInvocation;
	private boolean isExternalMethod;
	private boolean isGetter;

	public MethodInvocationVisitor(FutureCollector collector, String group) {
		this.collector = collector;
		this.group = group;

		isExternalMethod = false;
		isGetter = false;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		this.methodInvocation = methodInvocation;

		isExternalMethod = !collector.containsMethodDeclaration(group, methodInvocation.resolveMethodBinding());
		isGetter = collector.isGetter(methodInvocation);

		return false;
	}

	public Optional<Boolean> isExternalMethod() {
		if (methodInvocation == null)
			return Optional.empty();

		return Optional.of(isExternalMethod);
	}
	
	public Boolean shouldRefactor() {
		if (methodInvocation == null)
			return false;

		return isExternalMethod && !isGetter;
	}

	public Optional<MethodInvocation> getMethodInvocation() {
		return Optional.ofNullable(methodInvocation);
	}
}
