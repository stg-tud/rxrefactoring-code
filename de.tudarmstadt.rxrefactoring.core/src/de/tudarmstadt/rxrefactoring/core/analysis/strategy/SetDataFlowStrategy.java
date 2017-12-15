package de.tudarmstadt.rxrefactoring.core.analysis.strategy;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public interface SetDataFlowStrategy<V, T> extends DataFlowStrategy<V, Set<T>> {
	
	@Override
	default Set<T> entryResult() {
		return Collections.emptySet();
	}
	
	@Override
	default Set<T> initResult() {
		return Collections.emptySet();
	}
		
	@Override
	default Set<T> mergeAll(List<Set<T>> results) {
		Set<T> result = Sets.newHashSet();
		for (Set<T> set : results) {
			result.addAll(set);
		}
		return result;
	}

}
