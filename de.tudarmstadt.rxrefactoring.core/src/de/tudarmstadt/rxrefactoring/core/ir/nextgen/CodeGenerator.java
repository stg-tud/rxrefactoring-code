package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;

public abstract class CodeGenerator implements ICodeGenerator {
	
	private final @NonNull ReactiveObject object;

	public CodeGenerator(@NonNull ReactiveObject object) {
		this.object = object;
	}
	
	protected @NonNull ReactiveObject getObject() {
		return object;
	}

}
