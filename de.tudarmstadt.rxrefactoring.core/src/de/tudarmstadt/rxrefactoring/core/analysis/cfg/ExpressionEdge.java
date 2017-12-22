package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import org.eclipse.jdt.core.dom.Expression;

public class ExpressionEdge extends SimpleEdge<Expression> {

	
	public ExpressionEdge(Expression head, Expression tail) {
		super(head, tail);
	}

	
}
