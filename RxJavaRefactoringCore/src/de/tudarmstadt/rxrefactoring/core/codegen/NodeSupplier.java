package de.tudarmstadt.rxrefactoring.core.codegen;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

@FunctionalInterface
public interface NodeSupplier<NodeType extends ASTNode> {
	
	/**
	 * Supplies a node for the given AST.
	 */
	@NonNull NodeType apply(@NonNull AST ast);
	
	@SuppressWarnings("null")
	default <Y extends ASTNode> @NonNull NodeSupplier<Y> map(@NonNull BiFunction<AST, NodeType, Y> f) {
		return ast -> f.apply(ast, apply(ast));
	}
	
	@SuppressWarnings("null")
	static NodeSupplier<Type> OBJECT_TYPE =
			ast -> ast.newSimpleType(ast.newSimpleName("Object"));
			
	/**
	 * Creates a supplier that generates types {@code name<supplier>}.
	 * @param base The name of the parameterized type.
	 * @param parameters The type parameters.
	 * @return A parameterized type 
	 */
	@SafeVarargs
	@SuppressWarnings({ "null", "unchecked" })
	static @NonNull NodeSupplier<ParameterizedType> parameterizedTypeFrom(@NonNull NodeSupplier<? extends Type> base, @NonNull NodeSupplier<? extends Type>... parameters) {
		return ast -> {
			ParameterizedType parameterizedType = ast.newParameterizedType(base.apply(ast));
			
			for (@NonNull NodeSupplier<?> parameter : parameters) {
				parameterizedType.typeArguments().add(parameter.apply(ast));
			}	
			
			return parameterizedType;
		};
	}
	
	/**
	 * Creates a supplier for simple names with the given String as identifier.
	 * @param identifier The identifier of the created names.
	 * @return A new supplier.
	 */
	@SuppressWarnings("null")
	static @NonNull NodeSupplier<SimpleName> simpleName(@NonNull String identifier) {
		return ast -> ast.newSimpleName(identifier);
	}
	
	
	@SuppressWarnings("null")
	static @NonNull NodeSupplier<SimpleType> simpleType(@NonNull String identifier) {
		return ast -> ast.newSimpleType(ast.newSimpleName(identifier));
	}
	
	
	

	

}
