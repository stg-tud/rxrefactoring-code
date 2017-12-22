package de.tudarmstadt.rxrefactoring.core.analysis.traversal;

import java.util.Set;
import java.util.stream.Collectors;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;

public class BackwardTraversal<V> implements IDataFlowTraversal<V> {

	private final IControlFlowGraph<V> cfg;
	
	public BackwardTraversal(IControlFlowGraph<V> cfg) {
		this.cfg = cfg;
	}
		
	@Override
	public Set<V> entryNodes() {		
		return cfg.exitNodes();
	}

	@Override
	public Set<V> successorsOf(V vertex) {
		return cfg.incomingEdgesOf(vertex).stream().map(edge -> edge.getHead()).collect(Collectors.toSet());
	}

	@Override
	public Set<V> predecessorsOf(V vertex) {		
		return cfg.outgoingEdgesOf(vertex).stream().map(edge -> edge.getTail()).collect(Collectors.toSet());
	}

}
