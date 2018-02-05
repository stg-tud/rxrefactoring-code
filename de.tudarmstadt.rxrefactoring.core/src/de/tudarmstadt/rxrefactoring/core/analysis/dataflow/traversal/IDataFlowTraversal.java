package de.tudarmstadt.rxrefactoring.core.analysis.dataflow.traversal;

import java.util.Set;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;

public interface IDataFlowTraversal<Vertex> {

	public Set<Vertex> entryNodes(IControlFlowGraph<Vertex> cfg);
	
	public Set<Vertex> successorsOf(IControlFlowGraph<Vertex> cfg, Vertex vertex);
	
	public Set<Vertex> predecessorsOf(IControlFlowGraph<Vertex> cfg, Vertex vertex);
	
}
