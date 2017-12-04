package de.tudarmstadt.rxrefactoring.core.ir.nextgen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.Expression;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;

public class FutureCodeGenerator extends CodeGenerator {

	public FutureCodeGenerator(@NonNull ReactiveObject object) {
		super(object);
	}

	@Override
	public boolean validate() {						
		return getObject().hasInput() && getObject().numberOfOutputs() == 1;
	}
	
	public NodeSupplier<Expression> supplyFutureCreation() {
		return unit -> {
			return null;
		};
	}

}
