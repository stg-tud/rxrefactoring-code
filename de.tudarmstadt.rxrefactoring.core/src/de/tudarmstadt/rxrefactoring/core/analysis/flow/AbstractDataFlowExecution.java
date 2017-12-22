package de.tudarmstadt.rxrefactoring.core.analysis.flow;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.analysis.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.IDataFlowTraversal;

abstract class AbstractDataFlowExecution<Vertex, Result, Output> implements IDataFlowExecutionFactory<Vertex, Result, Output> {

	abstract AnalysisExecution abstractCreate(IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal);
	
	@Override
	public IDataFlowExecution<Output> create(IDataFlowStrategy<Vertex, Result> strategy,
			IDataFlowTraversal<Vertex> traversal) {
		return abstractCreate(strategy, traversal);
	}
	
	protected abstract class AnalysisExecution implements Runnable, IDataFlowExecution<Output> {
		
		private final IDataFlowStrategy<Vertex, Result> strategy;
		private final IDataFlowTraversal<Vertex> traversal;
		
		
		protected abstract Result getResultOf(Vertex vertex);
		
		protected abstract boolean resultHasChanged(Vertex vertex, Result newResult);
		
		protected abstract void setResult(Vertex vertex, Result newResult);
		
		protected abstract Output getOutput();
		
		
		public AnalysisExecution(IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {
			Objects.requireNonNull(strategy);
			Objects.requireNonNull(traversal);
			
			this.traversal = traversal;
			this.strategy = strategy;
		}
		
		@Override
		public Output execute() {
			run();
			return getOutput();
		}
				

		@Override
		public void run() {
			//Queue of nodes that have to be processed
			Queue<Vertex> queue = Lists.newLinkedList();
			//Add the entry nodes to the queue.
			queue.addAll(traversal.entryNodes());
			
			while (!queue.isEmpty()) {
				Vertex currentVertex = queue.poll();	
				
				System.out.println("Processing " + currentVertex);
				
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
				System.out.println("\tincoming " + incomingResult);	
				//Compute the outgoing result by applying the transformation of the node.
				Result outgoingResult = strategy.transform(currentVertex, incomingResult);
				
				System.out.println("\toutgoing " + outgoingResult);	
				
				//If the result has changed, then add all successors to the queue.
										
				if (resultHasChanged(currentVertex, outgoingResult)) {
					setResult(currentVertex, outgoingResult);
					queue.addAll(traversal.successorsOf(currentVertex));
				}				
			}
		}
				
			

		
		
	};
	
	

}
