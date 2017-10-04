package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.Types;

/**
 * Wrapper for Futures.sequence(...) invocations
 * @author mirko
 *
 */
public class FuturesMapWrapper implements FutureMethodWrapper {
	
	private final MethodInvocation futureInvocation;
	
	private FuturesMapWrapper(MethodInvocation futureInvocation) {
		this.futureInvocation = futureInvocation;
	}
	
	public static FuturesMapWrapper create(Expression futureInvocation) {
		
		if (!isFutureMap(futureInvocation))
			throw new IllegalArgumentException("You need to specify an invocation to future.map as parameter, but was: " + futureInvocation);
		
		return new FuturesMapWrapper((MethodInvocation) futureInvocation);
	}
	
	public Expression getFuture() {
		return futureInvocation.getExpression();
	}
	
	public FutureTypeWrapper getFromType() {
		return FutureTypeWrapper.create(getFuture().resolveTypeBinding());
	}
	
	public FutureTypeWrapper getToType() {
		return FutureTypeWrapper.create(futureInvocation.resolveTypeBinding());
	}
	
	public Expression getMapArgument() {
		return (Expression) futureInvocation.arguments().get(0);
	}

	
	public MethodInvocation getExpression() {
		return futureInvocation;
	}
	
	@SuppressWarnings("unchecked")
	public MethodInvocation createMapExpression(IRewriteCompilationUnit unit) {
		/*
		 * Builds:		
		 * future.map(new Func1<T0, T>() {
				@Override
				public T call(T0 arg) {
					return new Mapper<T0, T>() {
		 *				public T apply(T0 arg) {
		 *					MAP
		 *				}.apply(arg);
				}				
			});
		 * 
		 * from: 
		 * future.map(new Mapper<T0, T>() {
		 *				public T apply(T0 arg) {
		 *					MAP
		 *				});
		 * 
		 */
		
		
		AST ast = unit.getAST();
		
		Supplier<Type> fromType = () -> unit.copyNode(Types.fromBinding(ast, getFromType().getTypeParameter(ast)));
		Supplier<Type> toType = () -> unit.copyNode(Types.fromBinding(ast, getToType().getTypeParameter(ast)));
		
		//Func1 Type
		ParameterizedType func1Type = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Func1")));
		func1Type.typeArguments().add(fromType.get());
		func1Type.typeArguments().add(toType.get());
		
		ClassInstanceCreation newFunc1 = ast.newClassInstanceCreation();		
		newFunc1.setType(func1Type);
		
		//Func1 class declaration
		AnonymousClassDeclaration anonClass = ast.newAnonymousClassDeclaration();
		
		//Define call method
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(toType.get());
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		SingleVariableDeclaration callParameter = ast.newSingleVariableDeclaration();
		callParameter.setName(ast.newSimpleName("arg"));
		callParameter.setType(fromType.get());
		
		callMethod.parameters().add(callParameter);
		
		MethodInvocation mapperApply = ast.newMethodInvocation();
		mapperApply.setName(ast.newSimpleName("apply"));
		mapperApply.setExpression(unit.copyNode(getMapArgument()));
		mapperApply.arguments().add(ast.newSimpleName("arg"));
		
		ReturnStatement callReturn = ast.newReturnStatement();
		callReturn.setExpression(mapperApply);
		
		Block callBlock = ast.newBlock();
		callBlock.statements().add(callReturn);
		
		callMethod.setBody(callBlock);
		
		//Add call method
		anonClass.bodyDeclarations().add(callMethod);
		newFunc1.setAnonymousClassDeclaration(anonClass);
		
		
		//observable.map
		MethodInvocation map = ast.newMethodInvocation();
		map.setName(ast.newSimpleName("map"));
		map.setExpression(unit.copyNode(getFuture()));
		map.arguments().add(newFunc1);
		
		
		return map;
	}
	
	public static boolean isFutureMap(Expression expr) {
		if (expr == null || !(expr instanceof MethodInvocation))
			return false;
		
		MethodInvocation method = (MethodInvocation) expr;
		
		return Objects.equals(method.getName().getIdentifier(), "map") && 
				method.getExpression() != null &&
				FutureTypeWrapper.isAkkaFuture(method.getExpression().resolveTypeBinding());
	}

}
