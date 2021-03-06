package de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.Sets;

/**
 * Convenience class for dataflow analyses that use a subset lattice.
 *
 * @author mirko
 */
public interface SetDataFlowStrategy<Vertex, Result> extends IDataFlowStrategy<Vertex, Set<Result>> {
	
	@SuppressWarnings("null")
	@Override
	default @NonNull Set<Result> entryResult() {
		return Collections.emptySet();
	}
	
	@SuppressWarnings("null")
	@Override
	default @NonNull Set<Result> initResult() {
		return Collections.emptySet();
	}
		
	@Override
	default @NonNull Set<Result> mergeAll(@NonNull Iterable<Set<Result>> results) {
		Set<Result> result = Sets.newHashSet();
		results.forEach(result::addAll);		
		return result;
	}

}
