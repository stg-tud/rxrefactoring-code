package de.tudarmstadt.rxrefactoring.core.analysis;

public class DefaultEdge<V> implements Edge<V> {

	private final V head;
	private final V tail;

	public DefaultEdge(V head, V tail) {
		this.head = head;
		this.tail = tail;
	}

	@Override
	public V getHead() {
		return head;
	}

	@Override
	public V getTail() {
		return tail;
	}

}
