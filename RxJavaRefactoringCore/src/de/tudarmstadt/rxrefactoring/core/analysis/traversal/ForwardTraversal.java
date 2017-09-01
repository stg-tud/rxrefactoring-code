package de.tudarmstadt.rxrefactoring.core.analysis.traversal;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Statement;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ControlFlowGraph;

public class ForwardTraversal implements DataFlowTraversal<Statement> {

	private final ControlFlowGraph cfg;
	
	public ForwardTraversal(ControlFlowGraph cfg) {
		this.cfg = cfg;
	}
	
	
	@Override
	public Set<Statement> entryNodes() {		
		return cfg.entryNodes();
	}

	@Override
	public Set<Statement> successorsOf(Statement vertex) {
		return cfg.outgoingEdgesOf(vertex).stream().map(edge -> edge.getTail()).collect(Collectors.toSet());
	}

	@Override
	public Set<Statement> predecessorsOf(Statement vertex) {
		return cfg.incomingEdgesOf(vertex).stream().map(edge -> edge.getHead()).collect(Collectors.toSet());
	}

}
