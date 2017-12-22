package de.tudarmstadt.rxrefactoring.core.analysis.traversal;

import java.util.Set;
import java.util.stream.Collectors;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;

public class ForwardTraversal<V> implements IDataFlowTraversal<V> {

	private final IControlFlowGraph<V> cfg;
	
	public ForwardTraversal(IControlFlowGraph<V> cfg) {
		this.cfg = cfg;
	}
	
	
	@Override
	public Set<V> entryNodes() {		
		return cfg.entryNodes();
	}

	@Override
	public Set<V> successorsOf(V vertex) {
		return cfg.outgoingEdgesOf(vertex).stream().map(edge -> edge.getTail()).collect(Collectors.toSet());
	}

	@Override
	public Set<V> predecessorsOf(V vertex) {
		return cfg.incomingEdgesOf(vertex).stream().map(edge -> edge.getHead()).collect(Collectors.toSet());
	}

}
