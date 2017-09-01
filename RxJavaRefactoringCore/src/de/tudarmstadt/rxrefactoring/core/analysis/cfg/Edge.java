package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

public interface Edge<V> {
	public V getHead();

	public V getTail();
}
