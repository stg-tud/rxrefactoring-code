package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;

import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Wrapper for future.onFailure(...) invocations
 * @author mirko
 *
 */
public class FutureOnFailureWrapper implements FutureMethodWrapper {
	
	private final MethodInvocation futureInvocation;
	
	private FutureOnFailureWrapper(MethodInvocation futureInvocation) {
		this.futureInvocation = futureInvocation;
	}
	
	public static FutureOnFailureWrapper create(Expression futureInvocation) {
		
		if (!isFutureOnFailure(futureInvocation))
			throw new IllegalArgumentException("You need to specify an invocation to future.map as parameter, but was: " + futureInvocation);
		
		return new FutureOnFailureWrapper((MethodInvocation) futureInvocation);
	}
	
	public Expression getFuture() {
		return futureInvocation.getExpression();
	}
		
	public Expression getOnFailureArgument() {
		return (Expression) futureInvocation.arguments().get(0);
	}

	
	public MethodInvocation getExpression() {
		return futureInvocation;
	}
	
	
	public MethodInvocation createOnErrorExpression(IRewriteCompilationUnit unit) {
		/*
		 * Builds:		
		 * future.doOnError(new Action1<Throwable>() {
                @Override
                public void call(Throwable action1Throwable) {
                   new OnFailure() {
		           		@Override
		                public void onFailure(Throwable e) throws Throwable {
		                	BLOCK
		                }
	                }.onFailure(action1Throwable);
                }
            });
		 * 
		 * from: 
		 * future.onFailure(new OnFailure() {
                @Override
                public void onFailure(Throwable e) throws Throwable {
                	BLOCK
                }
            });
		 * 
		 */
		
		
		AST ast = unit.getAST();		
		
		
		//Define the call method
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
				
		SingleVariableDeclaration callParameter = ast.newSingleVariableDeclaration();
		callParameter.setName(ast.newSimpleName("action1Throwable"));
		callParameter.setType(ast.newSimpleType(ast.newSimpleName("Throwable")));
		
		callMethod.parameters().add(callParameter);
		
		
		MethodInvocation onFailure = ast.newMethodInvocation();
		onFailure.setName(ast.newSimpleName("onFailure"));
		onFailure.setExpression(unit.copyNode(getOnFailureArgument()));
		onFailure.arguments().add(ast.newSimpleName("action1Throwable"));
		
		Block callBlock = ast.newBlock();
		callBlock.statements().add(ast.newExpressionStatement(onFailure));
		
		callMethod.setBody(callBlock);
		
		
		//Action1 class declaration
		AnonymousClassDeclaration anonClass = ast.newAnonymousClassDeclaration();
		anonClass.bodyDeclarations().add(callMethod);	
		
		ParameterizedType action1Type = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Action1")));
		action1Type.typeArguments().add(ast.newSimpleType(ast.newSimpleName("Throwable")));
				
		ClassInstanceCreation newAction1 = ast.newClassInstanceCreation();
		newAction1.setType(action1Type);
		newAction1.setAnonymousClassDeclaration(anonClass);
		
		
		//observable doOnError
		MethodInvocation doOnError = ast.newMethodInvocation();
		doOnError.setName(ast.newSimpleName("doOnError"));
		doOnError.setExpression(unit.copyNode(getFuture()));
		doOnError.arguments().add(newAction1);
		
		
		return doOnError;
	}
	
	public static boolean isFutureOnFailure(Expression expr) {
		if (expr == null || !(expr instanceof MethodInvocation))
			return false;
		
		MethodInvocation method = (MethodInvocation) expr;
		
		return Objects.equals(method.getName().getIdentifier(), "onFailure") && 
				method.getExpression() != null &&
				FutureTypeWrapper.isAkkaFuture(method.getExpression().resolveTypeBinding());
	}

}
