package de.tudarmstadt.rxrefactoring.core.ir.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.core.ir.NodeSupplier;

public class SchedulerBuilder {

	public static NodeSupplier<Expression> schedulersComputation() {
		return unit -> {
			AST ast = unit.getAST();
			
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("computation"));
			invoke.setExpression(ast.newSimpleName("Schedulers"));
			
			return invoke;
		};		
	}
	
	public static NodeSupplier<Expression> schedulersIO() {
		return unit -> {
			AST ast = unit.getAST();
			
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("io"));
			invoke.setExpression(ast.newSimpleName("Schedulers"));
			
			return invoke;
		};
	}
	
	public static NodeSupplier<Expression> schedulersFrom(NodeSupplier<Expression> expr) {
		return unit -> {
			AST ast = unit.getAST();
			
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("from"));
			invoke.setExpression(ast.newSimpleName("Schedulers"));
			invoke.arguments().add(expr.apply(unit));
			
			return invoke;
		};		
	}
}
