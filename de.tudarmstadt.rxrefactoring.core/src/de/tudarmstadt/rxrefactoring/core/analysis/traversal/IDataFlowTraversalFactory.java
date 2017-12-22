package de.tudarmstadt.rxrefactoring.core.analysis.traversal;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;

public interface IDataFlowTraversalFactory {
	
	<Vertex> IDataFlowTraversal<Vertex> create(IControlFlowGraph<Vertex> cfg);
	
}
