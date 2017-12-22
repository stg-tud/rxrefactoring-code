package de.tudarmstadt.rxrefactoring.core.analysis.traversal;

import java.util.Set;

public interface IDataFlowTraversal<Vertex> {

	public Set<Vertex> entryNodes();
	
	public Set<Vertex> successorsOf(Vertex vertex);
	
	public Set<Vertex> predecessorsOf(Vertex vertex);
	
}
