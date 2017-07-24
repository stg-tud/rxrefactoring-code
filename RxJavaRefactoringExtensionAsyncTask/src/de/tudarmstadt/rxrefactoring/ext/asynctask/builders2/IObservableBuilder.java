package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

public interface IObservableBuilder<T extends ASTNode> {

	public T create();
	
}
