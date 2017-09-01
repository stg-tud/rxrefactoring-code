package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Statement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.DataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.DataFlowTraversal;

public abstract class DataFlowAnalysis<Result> implements Function<ControlFlowGraph, Map<Statement, Result>> {
	
	public abstract DataFlowStrategy<Result> newDataFlowStrategy();
	
	public abstract DataFlowTraversal<Statement> newDataFlowTraversal(ControlFlowGraph cfg);
	
	@Override
	public Map<Statement, Result> apply(ControlFlowGraph cfg) {
		return new AnalysisExecution(cfg).call();
	}
	
	/**
	 * Executes the analysis with the CFG given as context.
	 * 
	 * @author mirko
	 *
	 */
	private class AnalysisExecution implements Runnable, Callable<Map<Statement, Result>> {
		
		private final DataFlowStrategy<Result> strategy;
		private final DataFlowTraversal<Statement> traversal;
		
		private final Map<Statement, Result> outgoingResults = Maps.newHashMap();
		
		public AnalysisExecution(ControlFlowGraph cfg) {
			Objects.requireNonNull(cfg);			
			this.traversal = newDataFlowTraversal(cfg);
			this.strategy = newDataFlowStrategy();
		}
				
		@Override
		public Map<Statement, Result> call() {
			run();		
			return outgoingResults;
		}

		@Override
		public void run() {
			//Queue of nodes that have to be processed
			Queue<Statement> queue = Lists.newLinkedList();
			//Add the entry nodes to the queue.
			queue.addAll(traversal.entryNodes());
			
			while (!queue.isEmpty()) {
				Statement currentStatement = queue.poll();				
				Collection<Statement> predecessors = traversal.predecessorsOf(currentStatement);
				
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
				Result outgoingResult = strategy.transform(currentStatement, incomingResult);
				
				//If the result has changed, then add all successors to the queue.
				Result previousResult = outgoingResults.put(currentStatement, outgoingResult);
				if (!Objects.equals(previousResult, outgoingResult)) {
					queue.addAll(traversal.successorsOf(currentStatement));
				}				
			}		
		}
				
		
		private Result getResultOf(Statement statement) {
			if (outgoingResults.containsKey(statement)) {
				return outgoingResults.get(statement);
			} else {
				Result r = strategy.initResult();
				outgoingResults.put(statement, r);
				return r;
			}
		}
		
	}
	
	
	
	
	
	
}
