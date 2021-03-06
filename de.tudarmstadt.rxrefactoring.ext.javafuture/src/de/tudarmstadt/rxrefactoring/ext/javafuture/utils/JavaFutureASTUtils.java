package de.tudarmstadt.rxrefactoring.ext.javafuture.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;

public class JavaFutureASTUtils {

	/**
	 * Moves an expression inside a new method invocation. E.g.: x becomes
	 * className.methodName(x)
	 * 
	 * @param className
	 * @param methodName
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public static void moveInsideMethodInvocation(IRewriteCompilationUnit unit, String className, String methodName,
			ASTNode node) {
		AST ast = unit.getAST();

		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(ast.newSimpleName(className));
		invocation.setName(ast.newSimpleName(methodName));
		invocation.arguments().add(unit.copyNode(node));

		unit.replace(node, invocation);
	}

	@SuppressWarnings("unchecked")
	public static void moveInsideMethodInvocation(IRewriteCompilationUnit unit, String className, String methodName,
			SimpleName node, String append) {
		AST ast = unit.getAST();

		Expression initializerClone = ast.newSimpleName(node.getIdentifier() + append);
		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(ast.newSimpleName(className));
		invocation.setName(ast.newSimpleName(methodName));
		invocation.arguments().add(initializerClone);

		unit.replace(node, invocation);
	}

	public static void replaceType(IRewriteCompilationUnit unit, Type type, String replacementTypeName) {
		AST ast = unit.getAST();
		unit.replace(type, ast.newSimpleType(ast.newSimpleName(replacementTypeName)));
	}

	public static void replaceMethodInvocation(IRewriteCompilationUnit unit, String caller, String method1,
			String method2, MethodInvocation oldNode) {
		AST ast = unit.getAST();

		MethodInvocation singleMethod = ast.newMethodInvocation();
		singleMethod.setName(ast.newSimpleName(method2));
		MethodInvocation toBlockingMethod = ast.newMethodInvocation();
		toBlockingMethod.setName(ast.newSimpleName(method1));

		Expression old = oldNode.getExpression();

		if (old instanceof ArrayAccess) {

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


	public static void replaceWithBlockingGet(IRewriteCompilationUnit unit, MethodInvocation oldNode) {
		Statements.removeExceptionFromEnclosingTry(unit, oldNode, "java.util.concurrent.ExecutionException");
		Statements.removeExceptionFromEnclosingTry(unit, oldNode, "java.util.concurrent.TimeoutException");
		Statements.removeExceptionFromEnclosingTry(unit, oldNode, "java.lang.InterruptedException");

		AST ast = unit.getAST();
		MethodInvocation blockingSingle = ast.newMethodInvocation();
		blockingSingle.setName(ast.newSimpleName("blockingSingle"));
		
		Expression old = oldNode.getExpression();
		Expression clone = unit.copyNode(old);
		
		blockingSingle.setExpression(clone);

		unit.replace(oldNode, blockingSingle);
	}

	public static void removeTryStatement(IRewriteCompilationUnit unit, MethodInvocation mi) {
		Optional<TryStatement> tryStatement = ASTNodes.findParent(mi, TryStatement.class);
		if (tryStatement.isPresent()) {
			Optional<Block> block = ASTNodes.findParent(tryStatement.get(), Block.class);
			if (block.isPresent()) {
				List statementsTry = tryStatement.get().getBody().statements();
				List statementsParent = block.get().statements();
				Block newBlock = unit.getAST().newBlock();
				ListRewrite rewrite = unit.getListRewrite(newBlock, Block.STATEMENTS_PROPERTY);
				List<Statement> combinedStatements = new ArrayList<Statement>();
				for (Object o : statementsParent) {
					if (((Statement)o).equals(tryStatement.get())) {
						for (Object t : statementsTry)
							combinedStatements.add((Statement)t);
					} else
						combinedStatements.add((Statement)o);
				}
				Statement currentStatement = (Statement) combinedStatements.get(0);
				rewrite.insertFirst(currentStatement, null);
				for(int i=1; i<combinedStatements.size(); i++) {
					rewrite.insertAfter(combinedStatements.get(i), currentStatement, null);
					currentStatement = combinedStatements.get(i);
				}
				unit.replace(block.get(), newBlock);
			}
		}
	}
	
	
	public static void removeException(IRewriteCompilationUnit unit, ASTNode node) {
		Optional<TryStatement> tryStatement = ASTNodes.findParent(node, TryStatement.class);
		
		if (tryStatement.isPresent()) {
			Optional<Block> block = ASTNodes.findParent(tryStatement.get(), Block.class);
			if (block.isPresent()) {
				List statementsTry = tryStatement.get().getBody().statements();
				List statementsParent = block.get().statements();
				Block newBlock = unit.getAST().newBlock();
				ListRewrite rewrite = unit.getListRewrite(newBlock, Block.STATEMENTS_PROPERTY);
				List<Statement> combinedStatements = new ArrayList<Statement>();
				for (Object o : statementsParent) {
					if (((Statement)o).equals(tryStatement.get())) {
						for (Object t : statementsTry)
							combinedStatements.add((Statement)t);
					} else
						combinedStatements.add((Statement)o);
				}
				Statement currentStatement = (Statement) combinedStatements.get(0);
				rewrite.insertFirst(currentStatement, null);
				for(int i=1; i<combinedStatements.size(); i++) {
					rewrite.insertAfter(combinedStatements.get(i), currentStatement, null);
					currentStatement = combinedStatements.get(i);
				}
				unit.replace(block.get(), newBlock);
			}
		}
	}
	
	
	
	public static void replaceSimpleName(IRewriteCompilationUnit unit, SimpleName name, String replacement) {
		AST ast = unit.getAST();
		unit.replace(name, ast.newSimpleName(replacement));
	}

	public static void appendSimpleName(IRewriteCompilationUnit unit, SimpleName name, String append) {
		AST ast = unit.getAST();
		unit.replace(name, ast.newSimpleName(name.getIdentifier() + append));
	}

	public static boolean isMethodParameter(SingleVariableDeclaration singleVarDecl) {
		return singleVarDecl.getParent().getNodeType() == ASTNode.METHOD_DECLARATION;
	}
	
	
	
	
	
	


}
