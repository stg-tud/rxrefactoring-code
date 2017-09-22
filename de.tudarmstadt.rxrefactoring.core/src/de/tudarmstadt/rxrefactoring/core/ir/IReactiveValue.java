package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;

public interface IReactiveValue {
	public @NonNull NodeSupplier<? extends Type> supplyType();
	
	default @NonNull NodeSupplier<ParameterizedType> supplyFlowableType() {
		return NodeSupplier.parameterizedType(NodeSupplier.simpleType("Flowable"), supplyType());
	}
	
	default @NonNull NodeSupplier<ParameterizedType> supplyPublishProcessorType() {
		return NodeSupplier.parameterizedType(NodeSupplier.simpleType("PublishProcessor"), supplyType());
	} 
	
	public @NonNull NodeSupplier<SimpleName> supplyInternalName();
	
	public @NonNull NodeSupplier<SimpleName> supplyExternalName();
	
	public void addToTypeDeclaration(@NonNull RewriteCompilationUnit unit, @NonNull List<?> bodyDeclarations);
}
