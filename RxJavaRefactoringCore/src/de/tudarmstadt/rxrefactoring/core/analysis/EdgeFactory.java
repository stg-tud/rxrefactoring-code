package de.tudarmstadt.rxrefactoring.core.analysis;

public interface EdgeFactory<V, E extends Edge<V>> {

	public E create(V from, V to);
}
