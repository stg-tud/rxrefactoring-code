package de.tudarmstadt.rxrefactoring.core.analysis.traversal;

import java.util.Set;

public interface DataFlowTraversal<Vertex> {

	public Set<Vertex> entryNodes();
	
	public Set<Vertex> successorsOf(Vertex vertex);
	
	public Set<Vertex> predecessorsOf(Vertex vertex);
	
}
