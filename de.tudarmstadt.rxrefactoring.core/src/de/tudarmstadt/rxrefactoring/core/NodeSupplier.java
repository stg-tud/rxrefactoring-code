package de.tudarmstadt.rxrefactoring.core;

import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

/**
 * A node supplier is a factory for {@link ASTNode}. It creates nodes for a
 * specified {@link RewriteCompilationUnit}.
 * 
 * @author mirko
 *
 * @param <NodeType>
 *            The type of node that is created by this supplier.
 */
@FunctionalInterface
public interface NodeSupplier<NodeType extends ASTNode> {

	/**
	 * Supplies a node for the given AST.
	 */
	@NonNull
	NodeType apply(@NonNull AST unit);

	@SuppressWarnings("null")
	default <Y extends ASTNode> de.tudarmstadt.rxrefactoring.core.NodeSupplier<Y> map(
			@NonNull BiFunction<AST, NodeType, Y> f) {
		return ast -> f.apply(ast, apply(ast));
	}

	@SuppressWarnings("null")
	static @NonNull NodeSupplier<Type> OBJECT_TYPE = ast -> ast
			.newSimpleType(ast.newSimpleName("Object"));

	@SuppressWarnings("null")
	static @NonNull NodeSupplier<Type> VOID_TYPE = ast -> ast
			.newSimpleType(ast.newSimpleName("Void"));

	@SuppressWarnings("null")
	static @NonNull NodeSupplier<Type> PRIMITIVE_VOID_TYPE = ast -> ast.newPrimitiveType(PrimitiveType.VOID);

	/**
	 * Creates a supplier that generates types {@code name<supplier>}.
	 * 
	 * @param base
	 *            The name of the parameterized type.
	 * @param parameters
	 *            The type parameters.
	 * @return A parameterized type
	 */
	@SafeVarargs
	@SuppressWarnings({ "null", "unchecked" })
	static @NonNull NodeSupplier<ParameterizedType> parameterizedType(@NonNull NodeSupplier<? extends Type> base,
			@NonNull NodeSupplier<? extends Type>... parameters) {
		return ast -> {
			

			ParameterizedType parameterizedType = ast.newParameterizedType(base.apply(ast));

			for (@NonNull
			NodeSupplier<?> parameter : parameters) {
				parameterizedType.typeArguments().add(parameter.apply(ast));
			}

			return parameterizedType;
		};
	}

	/**
	 * Creates a supplier for simple names with the given String as identifier.
	 * 
	 * @param identifier
	 *            The identifier of the created names.
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
