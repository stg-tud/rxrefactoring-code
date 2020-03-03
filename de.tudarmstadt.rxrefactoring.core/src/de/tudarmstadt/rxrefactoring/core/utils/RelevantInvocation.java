package de.tudarmstadt.rxrefactoring.core.utils;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class RelevantInvocation {

	private MethodInvocation m;

	public RelevantInvocation(MethodInvocation m) {
		this.m = m;
	}

	public MethodInvocation getMethodInvocation() {
		return m;
	}
}
