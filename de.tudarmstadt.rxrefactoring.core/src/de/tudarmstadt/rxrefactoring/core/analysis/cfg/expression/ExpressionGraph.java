package de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression;

import org.eclipse.jdt.core.dom.Expression;
import org.jgrapht.graph.AbstractBaseGraph;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IEdge;

public class ExpressionGraph extends AbstractBaseGraph<Expression, IEdge<Expression>>
implements IControlFlowGraph<Expression> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1446111823987955045L;

	protected ExpressionGraph() {
		super((v1, v2) -> new ExpressionEdge(v1, v2), false, true);
	}
	
	
	public static ExpressionGraph createFrom(Expression expr) {
		ExpressionGraph graph = new ExpressionGraph();
		addTo(expr, graph);
		return graph;
	}
	
	/**
	 * Adds the expression graph to an existing graph. The expression subgraph is not
	 * connected the other graph.
	 * 
	 * @param expr The expression which graph should be added.
	 * @param graph The graph to add the expression to.
	 * @return The entry and exit nodes of the expression graph. These can be used
	 * to connect the expression to the existing graph.
	 */
	public static ExprAccess addTo(Expression expr, IControlFlowGraph<? super Expression> graph) {
		ExpressionGraphBuilder builder = new ExpressionGraphBuilder(graph);
		return builder.from(expr);		
	}
	
	/**
	 * Defines the entry and exit (sub-)expressions of an expression. <br>
	 * Example:
	 * 
	 * The expression {@code a + b} has the expression graph {@code a} &#8594; {@code b} &#8594; {@code _ + _}.
	 * The entry is {@code a} and the exit is {@code _ + _}.
	 * 
	 * @author mirko
	 *
	 */
	public static class ExprAccess {
		public final Expression entry;
		public final Expression exit;
					
		ExprAccess(Expression entry, Expression exit) {
			super();
			this.entry = entry;
			this.exit = exit;
		}		
		
		@Override
		public String toString() {
			return "ExprAccess(entry=" + entry + ", exit=" + exit + ")";
		}
	}

	

}
