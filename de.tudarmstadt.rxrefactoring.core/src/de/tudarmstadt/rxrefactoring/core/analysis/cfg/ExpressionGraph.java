package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
