package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

public interface IEdge<V> {
	public V getHead();

	public V getTail();
}
