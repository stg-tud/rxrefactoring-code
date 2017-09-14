package de.tudarmstadt.rxrefactoring.core.codegen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public interface ReactiveValue {

	public @NonNull NodeSupplier<? extends Type> supplyType();
	
	public @NonNull NodeSupplier<SimpleName> supplyName();
}
