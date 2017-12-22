package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import org.eclipse.jdt.core.dom.Expression;
import org.jgrapht.graph.AbstractBaseGraph;

public class ExpressionGraph extends AbstractBaseGraph<Expression, IEdge<Expression>>
implements IControlFlowGraph<Expression> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1446111823987955045L;

	protected ExpressionGraph() {
		super((v1, v2) -> new ExpressionEdge(v1, v2), false, true);
	}

	

}
