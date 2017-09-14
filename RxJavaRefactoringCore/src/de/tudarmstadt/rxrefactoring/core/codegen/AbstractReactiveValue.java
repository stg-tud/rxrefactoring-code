package de.tudarmstadt.rxrefactoring.core.codegen;

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
	public @NonNull NodeSupplier<? extends Type> supplyType() {
		return type;
	}

	@Override
	public @NonNull NodeSupplier<SimpleName> supplyName() {
		return name;
	}
	
	public @NonNull NodeSupplier<ParameterizedType> supplyFlowableType() {
		return flowableType;
	}
	
	
}
