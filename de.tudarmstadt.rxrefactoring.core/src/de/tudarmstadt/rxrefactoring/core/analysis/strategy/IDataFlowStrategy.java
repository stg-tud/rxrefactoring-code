package de.tudarmstadt.rxrefactoring.core.analysis.strategy;

import java.util.List;

public interface IDataFlowStrategy<Vertex, Result> {
	
	/**
	 * The analysis result for the entry nodes of the CFG.
	 * @return The result of an entry node. May not be null.
	 */
	Result entryResult(); 
	/**
	 * The initial analysis result for non-entry nodes of the CFG.
	 * @return The initial result of a non-entry node. May not be null.
	 */
	Result initResult(); 
	
	/**
	 * Merges all results in a collection to obtain a new non-null
	 * result.
	 * 
	 * @param results A non-null, collection with at least 2 entries of results to merge.
	 * @return A new non-null result.
	 */
	Result mergeAll(List<Result> results);
	 
	/**
	 * Creates a new result by transforming an input result.
	 * 
	 * @param statement The statement that transforms the result.
	 * @param input The input result.
	 * @return A new non-null result.
	 */
	Result transform(Vertex vertex, Result input);
}
