package de.tudarmstadt.rxrefactoring.ext.akkafuture.utils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureTypeWrapper;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AkkaFutureASTUtils {

	
	public static Annotation createOverrideAnnotation(AST ast) {
		MarkerAnnotation annotation = ast.newMarkerAnnotation();
		annotation.setTypeName(ast.newSimpleName("Override"));
		
		return annotation;
	}
	
	/*
	 * Builds 
	 * 
	 * Observable.fromCallable(new Callable<RETURN_TYPE>() {
	 * 	   @Override
	 *     public RETURN_TYPE call() throws Exception {
	 *         BLOCK
	 *     }
	 * })
	 * 
	 * 
	 * block has to be a "fresh" node (without parent, e.g., a copied node).
	 * returnType is copied by the method.
	 */	
	public static MethodInvocation buildFromCallable(RewriteCompilationUnit unit, Supplier<Type> returnType, Supplier<Block> block) {
		
		AST ast = unit.getAST();
		
		
//		//Define type: Callable
//		ParameterizedType tCallable = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Callable"))); //Callable<>
//		tCallable.typeArguments().add(returnType.get()); //Callable<T>
//		
//				
//		//Define method: call()
//		MethodDeclaration callMethod = ast.newMethodDeclaration();
//		callMethod.setName(ast.newSimpleName("call"));
//		callMethod.setReturnType2(returnType.get());
//		callMethod.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("Exception")));
//		//callMethod.modifiers().add(createOverrideAnnotation(ast));
//		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
//		
//				
//		callMethod.setBody(block);
//		
//		
//		//Define anonymous class
//		AnonymousClassDeclaration classDecl = ast.newAnonymousClassDeclaration();
//		classDecl.bodyDeclarations().add(callMethod);
//		
//		//Define constructor call: new Callable() { ... }
//		ClassInstanceCreation initCallable = ast.newClassInstanceCreation();
//		initCallable.setType(tCallable);
//		initCallable.setAnonymousClassDeclaration(classDecl);
		
		ClassInstanceCreation initCallable = buildCallableFromBlock(unit, returnType, block);
		
		//Define method invoke: Observable.fromCallable(new Callable ...)
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("fromCallable"));
		invoke.setExpression(ast.newSimpleName("Observable"));
		invoke.arguments().add(initCallable);
		
		return invoke;		
	}
	
	/*
	 * Builds
	 * new Callable<TYPE> () {
	 *  @Override
	 *     public TYPE call() throws Exception {
	 *         BLOCK;
	 *     }
	 * }
	 */
	public static ClassInstanceCreation buildCallableFromBlock(RewriteCompilationUnit unit, Supplier<Type> type, Supplier<Block> block) {
		
		AST ast = unit.getAST();
		
		ParameterizedType tCallable = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Callable"))); //Callable<>
		tCallable.typeArguments().add(type.get()); //Callable<T>
		
				
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(type.get());
		callMethod.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("Exception")));
		//callMethod.modifiers().add(createOverrideAnnotation(ast));
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
				
		callMethod.setBody(block.get());
		
		
		//Define anonymous class
		AnonymousClassDeclaration classDecl = ast.newAnonymousClassDeclaration();
		classDecl.bodyDeclarations().add(callMethod);
		
		//Define constructor call: new Callable() { ... }
		ClassInstanceCreation initCallable = ast.newClassInstanceCreation();
		initCallable.setType(tCallable);
		initCallable.setAnonymousClassDeclaration(classDecl);
		
		return initCallable;
	}
	
	public static void replaceThisWithFullyQualifiedThisIn(ASTNode root, RewriteCompilationUnit unit) {
		
		final AST ast = unit.getAST();
		
		class ThisVisitor extends ASTVisitor {
			public boolean visit(ThisExpression node) {
				
				
				
				if (node.getQualifier() == null) {
					
					ITypeBinding thisBinding = node.resolveTypeBinding().getErasure();
					
					ThisExpression thisExpr = ast.newThisExpression();
					thisExpr.setQualifier(ast.newName(ASTUtils.typeFromBinding(ast, thisBinding).toString()));
					
					unit.replace(node, thisExpr);
				}
				
				return true;
			}
		}
		
		root.accept(new ThisVisitor());
	}
	
	/*
	 * Builds
	 * new Callable<TYPE> () {
	 *  @Override
	 *     public TYPE call() throws Exception {
	 *         return EXPR;
	 *     }
	 * }
	 */
	public static ClassInstanceCreation buildCallableFromExpr(RewriteCompilationUnit unit, Supplier<Type> type, Supplier<Expression> expr) {
		
		AST ast = unit.getAST();
		
		ReturnStatement returnStmt = ast.newReturnStatement();
		returnStmt.setExpression(expr.get());
		
		Block block = ast.newBlock();
		block.statements().add(returnStmt);
		
		return buildCallableFromBlock(unit, type, () -> block);
	}
	
	/*
	 * Builds 
	 * 
	 * Observable.fromCallable(() -> expr)
	 * 
	 * 
	 * expr has to be a "fresh" node (without parent, e.g., a copied node).
	 */	
	public static MethodInvocation buildFromCallable(RewriteCompilationUnit unit, Expression expr) {
		
		AST ast = unit.getAST();
						
		LambdaExpression lambda = ast.newLambdaExpression();
		lambda.setBody(expr);
		
		//Define method invoke: Observable.fromCallable(new Callable ...)
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("fromCallable"));
		invoke.setExpression(ast.newSimpleName("Observable"));
		invoke.arguments().add(lambda);
		
		return invoke;		
	}
	
	public static List<Expression> futureReferencesInMethodInvocation(MethodInvocation method) {
		List<Expression> result = Lists.newLinkedList();
		
		Expression expr = method.getExpression();		
		if (expr != null && FutureTypeWrapper.isAkkaFuture(expr.resolveTypeBinding())) {
			result.add(expr);
		}
		
		for (Object element : method.arguments()) {
			Expression e = (Expression) element;
			if (FutureTypeWrapper.isAkkaFuture(e.resolveTypeBinding())) {
				result.add(e);
			}
		}
		
		return result;
	}
	
	
	
	public static boolean isAwait(ITypeBinding type) {
		if (type == null)
			return false;
		
		String name = type.getBinaryName();		
		return Objects.equals(name, "akka.dispatch.Await") || Objects.equals(name, "scala.concurrent.Await");
	}
	
	public static boolean isPatterns(ITypeBinding type) {
		if (type == null)
			return false;
		
		String name = type.getBinaryName();		
		return Objects.equals(name, "akka.pattern.Patterns");
	}
	
	public static boolean isFutures(ITypeBinding type) {
		if (type == null)
			return false;
		
		String name = type.getBinaryName();		
		return Objects.equals(name, "akka.dispatch.Futures") || Objects.equals(name, "scala.concurrent.Futures");
	}
	
	public static MethodInvocation createSchedulersIo(AST ast) {
		MethodInvocation method = ast.newMethodInvocation();
		method.setExpression(ast.newSimpleName("Schedulers"));
		method.setName(ast.newSimpleName("io"));
		
		return method;
	}
	
	public static boolean isCollectionOfFuture(ITypeBinding binding) {
		if (binding == null || binding.getTypeArguments().length != 1 || !FutureTypeWrapper.isAkkaFuture(binding.getTypeArguments()[0]))
			return false;
									
		//TODO: Check for other collection types
		if (Objects.equals(binding.getBinaryName(), "java.util.ArrayList")) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Calls the consumer for every variable access in the expression.
	 * Can be used to see which variables have to be declared final
	 * in the anonymous class.
	 * 
	 * @param unit
	 * @param expr
	 * @param addStatement
	 */
	public static void doWithVariablesFromExpression(RewriteCompilationUnit unit, Expression expr, Consumer<Statement> addStatement) {
		
		AST ast = unit.getAST();
		
		/*
		 * Build
		 * 
		 * final var1Final = var;
		 * ...
		 */
		List<SimpleName> variables = ASTUtils.findVariablesIn(expr);
		Set<String> alreadyDeclared = Sets.newHashSet();
		
		for(SimpleName var : variables) {
			
			//If the variable is already final or a field, do nothing
			IBinding binding = var.resolveBinding();			
			if (binding != null) {
				int modifiers = binding.getModifiers(); 
				
				if (Modifier.isFinal(modifiers) || (modifiers & Modifier.FIELD_ACCESS) != 0)
					continue;
			}		
					
			
			ITypeBinding varType = var.resolveTypeBinding();
			if (varType == null) {
				continue;
			}
			
			String newVarName = var.getIdentifier() + "Final";
			
			//If variable has not been declared already, then add a declaration.
			if (!alreadyDeclared.contains(newVarName)) {					
				
				//Add the variable declaration.
				VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
				fragment.setName(ast.newSimpleName(newVarName));
				fragment.setInitializer(ast.newSimpleName(var.getIdentifier()));
				
				VariableDeclarationStatement varStatement = ast.newVariableDeclarationStatement(fragment);
				varStatement.setType(ASTUtils.typeFromBinding(ast, varType));
				varStatement.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
				
				addStatement.accept(varStatement);
				
				alreadyDeclared.add(newVarName);
			}
			
			//Replace variable reference with new variable name
			unit.replace(var, ast.newSimpleName(newVarName));			
		}
	}
}
