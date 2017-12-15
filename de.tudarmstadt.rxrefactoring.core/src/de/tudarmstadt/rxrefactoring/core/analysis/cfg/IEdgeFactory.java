package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

public interface IEdgeFactory<V, E extends IEdge<V>> {

	public E create(V from, V to);
}
