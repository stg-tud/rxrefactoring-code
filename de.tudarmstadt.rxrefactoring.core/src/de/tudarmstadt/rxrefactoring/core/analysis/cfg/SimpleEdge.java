package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

public class SimpleEdge<V> implements Edge<V> {

	private final V head;
	private final V tail;

	public SimpleEdge(V head, V tail) {
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
	
	@Override
	public String toString() {
		return "Edge(" + head + ", " + tail + ")";
	}

}
