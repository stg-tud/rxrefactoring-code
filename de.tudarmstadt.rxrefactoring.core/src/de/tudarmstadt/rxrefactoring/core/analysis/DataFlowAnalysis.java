package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.DataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.DataFlowTraversal;

public abstract class DataFlowAnalysis<Vertex, Result> implements Function<IControlFlowGraph<Vertex>, Map<Vertex, Result>> {
	
	public abstract DataFlowStrategy<Vertex, Result> newDataFlowStrategy();
	
	public abstract DataFlowTraversal<Vertex> newDataFlowTraversal(IControlFlowGraph<Vertex> cfg);
	
	@Override
	public Map<Vertex, Result> apply(IControlFlowGraph<Vertex> cfg) {
		return new AnalysisExecution(cfg).call();
	}
	
	/**
	 * Executes the analysis with the CFG given as context.
	 * 
	 * @author mirko
	 *
	 */
	private class AnalysisExecution implements Runnable, Callable<Map<Vertex, Result>> {
		
		private final DataFlowStrategy<Vertex, Result> strategy;
		private final DataFlowTraversal<Vertex> traversal;
		
		private final Map<Vertex, Result> outgoingResults = Maps.newHashMap();
		
		public AnalysisExecution(IControlFlowGraph<Vertex> cfg) {
			Objects.requireNonNull(cfg);			
			this.traversal = newDataFlowTraversal(cfg);
			this.strategy = newDataFlowStrategy();
		}
				
		@Override
		public Map<Vertex, Result> call() {
			run();		
			return outgoingResults;
		}

		@Override
		public void run() {
			//Queue of nodes that have to be processed
			Queue<Vertex> queue = Lists.newLinkedList();
			//Add the entry nodes to the queue.
			queue.addAll(traversal.entryNodes());
			
			while (!queue.isEmpty()) {
				Vertex currentVertex = queue.poll();				
				Collection<Vertex> predecessors = traversal.predecessorsOf(currentVertex);
				
				//Compute the incoming result as merge of all outgoing results from
				//predecessors.
				Result incomingResult;
				switch (predecessors.size()) {
				case 0 :
					incomingResult = strategy.entryResult();
					break;
				case 1 :
					incomingResult = getResultOf(predecessors.iterator().next());
					break;
				default :
					List<Result> incomingResults =
						predecessors.stream().map(stmt -> getResultOf(stmt)).collect(Collectors.toList());				
					incomingResult = strategy.mergeAll(incomingResults);
				}
						
				//Compute the outgoing result by applying the transformation of the node.
				Result outgoingResult = strategy.transform(currentVertex, incomingResult);
				
				//If the result has changed, then add all successors to the queue.
				Result previousResult = outgoingResults.put(currentVertex, outgoingResult);
				if (!Objects.equals(previousResult, outgoingResult)) {
					queue.addAll(traversal.successorsOf(currentVertex));
				}				
			}		
		}
				
		
		private Result getResultOf(Vertex vertex) {
			if (outgoingResults.containsKey(vertex)) {
				return outgoingResults.get(vertex);
			} else {
				Result r = strategy.initResult();
				outgoingResults.put(vertex, r);
				return r;
			}
		}
		
	}
	
	
	
	
	
	
}
