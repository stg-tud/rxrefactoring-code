package de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression;

import java.util.Objects;

import org.eclipse.jdt.core.dom.Expression;
import org.jgrapht.graph.AbstractBaseGraph;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

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
	
	public static ExprAccess createAccess(Expression expr) {
		ExpressionGraph graph = new ExpressionGraph();		
		return addTo(expr, graph);
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
		
		public final ImmutableMultimap<ExceptionIdentifier, Expression> exceptions;
		
		private ExprAccess(Expression entry, Expression exit, ImmutableMultimap<ExceptionIdentifier, Expression> exceptions) {
			super();
			this.entry = Objects.requireNonNull(entry);
			this.exit = Objects.requireNonNull(exit);
			this.exceptions = Objects.requireNonNull(exceptions);
		}		
		
		static ExprAccess create(Expression entry, Expression exit, ImmutableMultimap<ExceptionIdentifier, Expression>... exceptMaps) {
			
			if (exceptMaps.length == 1) {
				return new ExprAccess(entry, exit, exceptMaps[0]);
			}
			
			ImmutableMultimap.Builder<ExceptionIdentifier, Expression> b = ImmutableMultimap.builder();
			
			for(Multimap<ExceptionIdentifier, Expression> map : exceptMaps)
				b.putAll(map);
			
			return new ExprAccess(entry, exit, b.build());
		}
		
		static ExprAccess create(Expression entry, Expression exit) {
			return create(entry, exit, ImmutableMultimap.of());
		}
						
		ExprAccess withExit(Expression exit) {
			return create(entry, exit, exceptions);
		}	
		
		ExprAccess withExceptions(ImmutableMultimap<ExceptionIdentifier, Expression>... exceptMaps) {			
			return create(entry, exit, exceptMaps);
		}
		
		@Override
		public String toString() {
			return "ExprAccess(entry=" + entry + ", exit=" + exit + ", exceptions=" + exceptions + ")";
		}	
		
		ImmutableMultimap<ExceptionIdentifier, Expression> getExceptions() {
			return exceptions;
		}
	}

	

}
