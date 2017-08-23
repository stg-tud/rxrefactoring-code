package de.tudarmstadt.rxrefactoring.ext.akkafuture.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;


public class JavaFutureASTUtils {
	
	/**
	 * Moves an expression inside a new method invocation.
	 * E.g.: x becomes className.methodName(x)
	 * @param className
	 * @param methodName
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public static void moveInsideMethodInvocation(RewriteCompilationUnit unit, String className, String methodName, ASTNode node) {
		AST ast = unit.getAST();
		

		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(ast.newSimpleName(className));
		invocation.setName(ast.newSimpleName(methodName));
		invocation.arguments().add(unit.copyNode(node));

		unit.replace(node, invocation);
	}
	
	@SuppressWarnings("unchecked")
	public static void moveInsideMethodInvocation(RewriteCompilationUnit unit, String className, String methodName, SimpleName node, String append) {
		AST ast = unit.getAST();
		
		Expression initializerClone = ast.newSimpleName(node.getIdentifier() + append);
		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(ast.newSimpleName(className));
		invocation.setName(ast.newSimpleName(methodName));
		invocation.arguments().add(initializerClone);

		unit.replace(node, invocation);
	}
	
	public static void replaceType(RewriteCompilationUnit unit, Type type, String replacementTypeName) {
		AST ast = unit.getAST();
		unit.replace(type, ast.newSimpleType(ast.newSimpleName(replacementTypeName)));
	}
	
	
	
	public static void replaceMethodInvocation(RewriteCompilationUnit unit, String caller, String method1, String method2, MethodInvocation oldNode) {
		AST ast = unit.getAST();
		
		MethodInvocation singleMethod = ast.newMethodInvocation();
		singleMethod.setName(ast.newSimpleName(method2));
		MethodInvocation toBlockingMethod = ast.newMethodInvocation();
		toBlockingMethod.setName(ast.newSimpleName(method1));
		
		Expression old = oldNode.getExpression();
		
		if(old instanceof ArrayAccess) {	
					
			ArrayAccess clone = ast.newArrayAccess();
			clone.setArray(ast.newSimpleName(caller));
			clone.setIndex(unit.copyNode(((ArrayAccess) old).getIndex()));
			
			toBlockingMethod.setExpression(clone);
		} else {
			toBlockingMethod.setExpression(ast.newSimpleName(caller));
		}
		
		singleMethod.setExpression(toBlockingMethod);
		
		unit.replace(oldNode, singleMethod);
	}
	
	
	public static void replaceSimpleName(RewriteCompilationUnit unit, SimpleName name, String replacement) {
		AST ast = unit.getAST();
		unit.replace(name, ast.newSimpleName(replacement));
	}
	
	public static void appendSimpleName(RewriteCompilationUnit unit, SimpleName name, String append) {
		AST ast = unit.getAST();
		unit.replace(name, ast.newSimpleName(name.getIdentifier() + append));
	}
	
	
	public static boolean isMethodParameter(SingleVariableDeclaration singleVarDecl) {
		return singleVarDecl.getParent().getNodeType() == ASTNode.METHOD_DECLARATION;
	}
	
	
	
}
