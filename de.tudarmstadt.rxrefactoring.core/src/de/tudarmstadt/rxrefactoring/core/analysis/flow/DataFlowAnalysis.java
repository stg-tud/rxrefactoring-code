package de.tudarmstadt.rxrefactoring.core.analysis.flow;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.BackwardTraversal;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.ForwardTraversal;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.IDataFlowTraversal;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.IDataFlowTraversalFactory;

public class DataFlowAnalysis<Vertex extends ASTNode, Result> {

	private final IDataFlowStrategy<Vertex, Result> strategy;
	private final IDataFlowTraversalFactory traversalFactory;
		
	
	private DataFlowAnalysis(IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversalFactory traversalFactory) {
		this.strategy = strategy;
		this.traversalFactory = traversalFactory;
	}
	
	
	public static final IDataFlowTraversalFactory TRAVERSAL_FORWARDS = new IDataFlowTraversalFactory() {		
		@Override
		public <Vertex> IDataFlowTraversal<Vertex> create(IControlFlowGraph<Vertex> cfg) {			
			return new ForwardTraversal<Vertex>(cfg);
		}
	};
	public static final IDataFlowTraversalFactory TRAVERSAL_BACKWARDS = new IDataFlowTraversalFactory() {		
		@Override
		public <Vertex> IDataFlowTraversal<Vertex> create(IControlFlowGraph<Vertex> cfg) {			
			return new BackwardTraversal<Vertex>(cfg);
		}
	};
	
	public IDataFlowExecutionFactory<Vertex, Result, Map<Vertex, Result>> mapExecutor() {
		return new MapDataFlowExecution<>();
	}
	
	public IDataFlowExecutionFactory<Vertex, Result, Void> astExecutor() {
		return new ASTDataFlowExecution<>();
	}
		
	public static <Vertex extends ASTNode, Result> DataFlowAnalysis<Vertex, Result> create(IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversalFactory traversalFactory) {
		return new DataFlowAnalysis<>(strategy, traversalFactory);
	}


	public <Output> Output apply(IControlFlowGraph<Vertex> cfg, IDataFlowExecutionFactory<Vertex, Result, Output> executionFactory) {
		IDataFlowExecution<Output> exec = executionFactory.create(strategy, traversalFactory.create(cfg));		
			
		return exec.execute();
	}
	
	
	
	
	
}
