package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;

public class FutureTypeWrapper {
	
	private final ITypeBinding futureType;
	
	private FutureTypeWrapper(ITypeBinding futureType) {
		this.futureType = futureType;
	}
	
	public static FutureTypeWrapper create(ITypeBinding type) {
		if (isAkkaFuture(type)) {
			return new FutureTypeWrapper(type);
		}
		
		return null;
	}
	
	public static boolean isAkkaFuture(ITypeBinding type) {
		if (type == null)
			return false;
		
		String name = type.getBinaryName();		
		return Objects.equals(name, "akka.dispatch.Future") || Objects.equals(name, "scala.concurrent.Future");
	}
	
	public Type createType(AST ast) {
		return ASTUtils.typeFromBinding(ast, futureType);
		
	}
	
	public Type toObservableType(AST ast) {
		ParameterizedType type = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Observable")));
		type.typeArguments().add(ASTUtils.typeFromBinding(ast, getTypeParameter(ast)));
		
		return type;
	}
	
	public ITypeBinding getTypeParameter(AST ast) {
		ITypeBinding[] types = futureType.getTypeArguments();
		
		if (types.length == 0) {
			return ast.resolveWellKnownType("java.lang.Object");
		} else {
			return types[0];
		}
		
	}
	
}
