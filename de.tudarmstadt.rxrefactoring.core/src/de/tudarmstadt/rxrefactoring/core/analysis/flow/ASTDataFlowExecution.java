package de.tudarmstadt.rxrefactoring.core.analysis.flow;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.analysis.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.traversal.IDataFlowTraversal;

public class ASTDataFlowExecution<Vertex extends ASTNode, Result> extends AbstractDataFlowExecution<Vertex, Result, Void>{

	public static final String PROPERTY_NAME = "dataflow-result";
	
	@Override
	AbstractDataFlowExecution<Vertex, Result, Void>.AnalysisExecution abstractCreate(
			IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {
		return new AnalysisExecution(strategy, traversal) {

			@Override
			protected
			Result getResultOf(Vertex vertex) {
				Result res = (Result) vertex.getProperty(PROPERTY_NAME);
				
				if (res == null) {
					return strategy.initResult();
				}
				
				return res;
			}

			@Override
			protected
			boolean resultHasChanged(Vertex vertex, Result newResult) {
				return !Objects.equals(getResultOf(vertex), newResult);
			}

			@Override
			protected
			void setResult(Vertex vertex, Result newResult) {				
				vertex.setProperty("dataflow-result", newResult);
				System.out.println("set property in " + vertex);
			}

			@Override
			protected
			Void getOutput() {				
				return null;
			}
		};
	}

	

}
