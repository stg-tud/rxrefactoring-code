package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class ReactiveObject {
	
	private final @Nullable IInput input;
	
	private final @NonNull IComputation computation;
	
	private final @NonNull IOutput[] outputs;

	public ReactiveObject(@Nullable IInput input, @NonNull IComputation computation, @NonNull IOutput[] outputs) {
		this.input = input;
		this.computation = computation;
		this.outputs = outputs;
	}
	
	public boolean hasInput() {
		return input != null;
	}
		
	public int numberOfOutputs() {
		return outputs.length;
	}
	

}
