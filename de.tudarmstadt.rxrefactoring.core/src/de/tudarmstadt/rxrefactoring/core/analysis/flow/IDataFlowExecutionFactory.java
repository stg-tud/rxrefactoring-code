package de.tudarmstadt.rxrefactoring.core.analysis.flow;

import de.tudarmstadt.rxrefactoring.core.analysis.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.IDataFlowTraversal;

public interface IDataFlowExecutionFactory<Vertex, Result, Output> {

	IDataFlowExecution<Output> create(IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal);
	
}
