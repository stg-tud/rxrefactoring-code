package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class Function {

	private final @NonNull Block block;
	
	private final @NonNull SingleVariableDeclaration[] inputs;

	public Function(@NonNull Block block, @NonNull SingleVariableDeclaration... inputs) {
		Objects.requireNonNull(block);
		
		this.block = block;
		this.inputs = inputs;
	}
	
	
	
	
}
