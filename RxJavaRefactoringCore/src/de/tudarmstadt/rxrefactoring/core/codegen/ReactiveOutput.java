package de.tudarmstadt.rxrefactoring.core.codegen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public class ReactiveOutput extends AbstractReactiveValue {

	public ReactiveOutput(@NonNull NodeSupplier<? extends Type> type, @NonNull NodeSupplier<SimpleName> name) {
		super(type, name);
	}

	
}
