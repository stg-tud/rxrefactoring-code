package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

public interface EdgeFactory<V, E extends Edge<V>> {

	public E create(V from, V to);
}
