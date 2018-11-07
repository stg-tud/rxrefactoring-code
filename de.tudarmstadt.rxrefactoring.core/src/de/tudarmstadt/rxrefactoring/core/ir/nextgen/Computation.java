package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;
import de.tudarmstadt.rxrefactoring.core.ir.util.CallableBuilder;

public class Computation implements IComputation {

	private final @Nullable IInput input;
	
	private final @NonNull NodeSupplier<Block> block;
	
		
	public Computation(@Nullable IInput input, @NonNull NodeSupplier<Block> block) {
		this.input = input;
		this.block = block;
	}

	@Override
	public @Nullable IInput getInput() {
		return null;
	}

	@Override
	public @NonNull NodeSupplier<Block> getBody() {
		return block;
	}
	
	public @NonNull NodeSupplier<ClassInstanceCreation> supplyCallable() {
		NodeSupplier<Type> type = NodeSupplier.PRIMITIVE_VOID_TYPE;	
		CallableBuilder builder = new CallableBuilder(type, block);		
		
		return builder.supplyClassInstanceCreation();
	}
	
}
