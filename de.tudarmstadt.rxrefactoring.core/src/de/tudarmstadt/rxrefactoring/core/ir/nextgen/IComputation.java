package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.Block;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;

public interface IComputation {

	@Nullable IInput getInput();
	
	@NonNull NodeSupplier<? extends Block> getBody();
	
}
