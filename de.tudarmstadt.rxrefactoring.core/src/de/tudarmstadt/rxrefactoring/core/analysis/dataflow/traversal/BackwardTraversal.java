package de.tudarmstadt.rxrefactoring.core.analysis.dataflow.traversal;

import java.util.Set;
import java.util.stream.Collectors;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;

public class BackwardTraversal<Vertex> implements IDataFlowTraversal<Vertex> {

	@Override
	public Set<Vertex> entryNodes(IControlFlowGraph<Vertex> cfg) {		
		return cfg.exitNodes();
	}

	@Override
	public Set<Vertex> successorsOf(IControlFlowGraph<Vertex> cfg, Vertex vertex) {
		return cfg.incomingEdgesOf(vertex).stream().map(edge -> edge.getHead()).collect(Collectors.toSet());
	}

	@Override
	public Set<Vertex> predecessorsOf(IControlFlowGraph<Vertex> cfg, Vertex vertex) {		
		return cfg.outgoingEdgesOf(vertex).stream().map(edge -> edge.getTail()).collect(Collectors.toSet());
	}

}
