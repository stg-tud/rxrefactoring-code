package de.tudarmstadt.rxrefactoring.core.analysis.flow;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.analysis.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.IDataFlowTraversal;

public class MapDataFlowExecution<Vertex, Result> extends AbstractDataFlowExecution<Vertex, Result, Map<Vertex, Result>>{

	@Override
	AbstractDataFlowExecution<Vertex, Result, Map<Vertex, Result>>.AnalysisExecution abstractCreate(
			IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {
		return new AnalysisExecution(strategy, traversal) {

			private final Map<Vertex, Result> output = Maps.newHashMap();

			@Override
			protected Result getResultOf(Vertex vertex) {
				return output.get(vertex);
			}

			@Override
			protected
			boolean resultHasChanged(Vertex vertex, Result newResult) {
				return !Objects.equals(output.get(vertex), newResult);
			}

			@Override
			protected
			void setResult(Vertex vertex, Result newResult) {				
				output.put(vertex, newResult);
			}

			@Override
			protected
			Map<Vertex, Result> getOutput() {				
				return output;
			}
		};
	}

	

}
