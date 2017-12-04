package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;

public class NewFutureTaskComputation implements IComputation {

	private ClassInstanceCreation newFutureTask;

	@Override
	public @NonNull IInput getInput() {
		return null;
	}

	@Override
	public @NonNull NodeSupplier<Block> getBody() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
