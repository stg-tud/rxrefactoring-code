package de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

public interface IDataFlowStrategy<Vertex, Result> {
	
	/**
	 * The analysis result for the entry nodes of the control flow graph.
	 * @return The result of an entry node. May not be null.
	 */
	@NonNull Result entryResult(); 
	
	/**
	 * The initial analysis result for non-entry nodes of the control flow graph.
	 * @return The initial result of a non-entry node. May not be null.
	 */
	@NonNull Result initResult(); 
	
	/**
	 * Merges all results in a collection to obtain a new non-null
	 * result.
	 * 
	 * @param results A non-null, collection with at least 2 entries of results to merge.
	 * @return A new non-null result.
	 */
	@NonNull Result mergeAll(@NonNull Collection<Result> results);
	 
	/**
	 * Creates a new result by transforming an input result.
	 * 
	 * @param vertex The node that transforms the result, e.g. a statement or expression.
	 * @param input The input result.
	 * @return A new non-null result.
	 */
	@NonNull Result transform(@NonNull Vertex vertex, @NonNull Result input);
}
