package de.tudarmstadt.rxrefactoring.core.codegen.util;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.codegen.NodeSupplier;

public class SchedulerBuilder {

	public static NodeSupplier<Expression> schedulersComputation() {
		return ast -> {
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("computation"));
			invoke.setExpression(ast.newSimpleName("Schedulers"));
			
			return invoke;
		};		
	}
	
	public static NodeSupplier<Expression> schedulersIO() {
		return ast -> {
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("io"));
			invoke.setExpression(ast.newSimpleName("Schedulers"));
			
			return invoke;
		};
	}
	
	public static NodeSupplier<Expression> schedulersFrom(NodeSupplier<Expression> expr) {
		return ast -> {
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("from"));
			invoke.setExpression(ast.newSimpleName("Schedulers"));
			invoke.arguments().add(expr.apply(ast));
			
			return invoke;
		};		
	}
}
