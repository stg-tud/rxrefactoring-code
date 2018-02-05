package de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression;

import org.eclipse.jdt.core.dom.Expression;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.SimpleEdge;

public class ExpressionEdge extends SimpleEdge<Expression> {

	
	public ExpressionEdge(Expression head, Expression tail) {
		super(head, tail);
	}

	
}
