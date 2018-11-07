package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;

/**
 * Defines an input to a reactive computation.
 *  
 * @author mirko
 *
 */
public interface IInput {
	
	@NonNull NodeSupplier<? extends Type> getType();
	
	@NonNull NodeSupplier<? extends SimpleName> getName();

}
