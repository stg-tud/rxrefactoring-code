package de.tudarmstadt.rxrefactoring.core.analysis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ControlFlowGraph<V, E extends Edge<V>> {

	protected final Multimap<V, V> edges = HashMultimap.create();
	
	public ControlFlowGraph() {	}
}
