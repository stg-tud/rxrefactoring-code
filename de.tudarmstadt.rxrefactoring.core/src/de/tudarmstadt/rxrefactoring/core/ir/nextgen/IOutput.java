package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;

public interface IOutput {

	@NonNull NodeSupplier<? extends Type> getType();
	
	@NonNull NodeSupplier<? extends SimpleName> getName();
}
