package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Statement;
import org.jgrapht.DirectedGraph;

public interface IControlFlowGraph<Vertex> extends DirectedGraph<Vertex, IEdge<Vertex>> {

	default Set<Vertex> entryNodes() {
		return vertexSet().stream().filter(stmt -> inDegreeOf(stmt) == 0).collect(Collectors.toSet());		
	}
	
	default Set<Vertex> exitNodes() {
		return vertexSet().stream().filter(stmt -> outDegreeOf(stmt) == 0).collect(Collectors.toSet());		
	}
}
