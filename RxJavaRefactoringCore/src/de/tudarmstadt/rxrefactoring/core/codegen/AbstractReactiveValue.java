package de.tudarmstadt.rxrefactoring.core.codegen;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

class AbstractReactiveValue implements ReactiveValue {

	public final @NonNull NodeSupplier<SimpleName> name;
	
	public final @NonNull NodeSupplier<? extends Type> type;
	
	private final @NonNull NodeSupplier<ParameterizedType> flowableType;
	
	
	public AbstractReactiveValue(@NonNull NodeSupplier<? extends Type> type, @NonNull NodeSupplier<SimpleName> name) {		
		this.type = type;
		this.name = name;
		this.flowableType = NodeSupplier.parameterizedTypeFrom(NodeSupplier.simpleType("Flowable"), type);
	}

	@Override
	public @NonNull Type buildType(@NonNull AST ast) {
		return type.apply(ast);
	}

	@Override
	public @NonNull SimpleName buildName(@NonNull AST ast) {
		return name.apply(ast);
	}
	
	public @NonNull ParameterizedType buildFlowableType(@NonNull AST ast) {
		return flowableType.apply(ast);
	}
	
	
}
