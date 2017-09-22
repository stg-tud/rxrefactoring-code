package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;

@FunctionalInterface
public interface NodeSupplier<NodeType extends ASTNode> {
	
	/**
	 * Supplies a node for the given AST.
	 */
	@NonNull NodeType apply(@NonNull RewriteCompilationUnit unit);
	
	@SuppressWarnings("null")
	default <Y extends ASTNode> @NonNull NodeSupplier<Y> map(@NonNull BiFunction<RewriteCompilationUnit, NodeType, Y> f) {
		return ast -> f.apply(ast, apply(ast));
	}
	
	@SuppressWarnings("null")
	static @NonNull NodeSupplier<Type> OBJECT_TYPE =
			unit -> unit.getAST().newSimpleType(unit.getAST().newSimpleName("Object"));
			
	/**
	 * Creates a supplier that generates types {@code name<supplier>}.
	 * @param base The name of the parameterized type.
	 * @param parameters The type parameters.
	 * @return A parameterized type 
	 */
	@SafeVarargs
	@SuppressWarnings({ "null", "unchecked" })
	static @NonNull NodeSupplier<ParameterizedType> parameterizedType(@NonNull NodeSupplier<? extends Type> base, @NonNull NodeSupplier<? extends Type>... parameters) {
		return unit -> {
			AST ast = unit.getAST();
			
			ParameterizedType parameterizedType = ast.newParameterizedType(base.apply(unit));
			
			for (@NonNull NodeSupplier<?> parameter : parameters) {
				parameterizedType.typeArguments().add(parameter.apply(unit));
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
		return unit -> unit.getAST().newSimpleName(identifier);
	}
	
	
	@SuppressWarnings("null")
	static @NonNull NodeSupplier<SimpleType> simpleType(@NonNull String identifier) {
		return unit -> unit.getAST().newSimpleType(unit.getAST().newSimpleName(identifier));
	}
	
	
	

	

}
