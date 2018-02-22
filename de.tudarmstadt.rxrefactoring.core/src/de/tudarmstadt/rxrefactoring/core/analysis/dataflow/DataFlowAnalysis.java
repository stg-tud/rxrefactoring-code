package de.tudarmstadt.rxrefactoring.core.analysis.dataflow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.traversal.BackwardTraversal;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.traversal.ForwardTraversal;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.traversal.IDataFlowTraversal;

/**
 * This class implements generic dataflow analyses on control flow graphs.
 * The concrete analysis is defined by the strategy as well as the graph traversal.
 *
 * @author mirko
 *
 * @param <Vertex> The vertices of the control flow graph.
 * @param <Result> The result of the analysis per node.
 */
public class DataFlowAnalysis<Vertex extends ASTNode, Result> {

	private final IDataFlowStrategy<Vertex, Result> strategy;
	private final IDataFlowTraversal<Vertex> traversal;


	protected DataFlowAnalysis(IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {
		this.strategy = strategy;
		this.traversal = traversal;
	}

	/**
	 * Traverses the control flow graph from entries to exits.
	 */
	@SuppressWarnings("unchecked")
	public static <V> IDataFlowTraversal<V> traversalForwards() {
		return TRAVERSAL_FORWARDS;
	}

	@SuppressWarnings("rawtypes")
	private static final IDataFlowTraversal TRAVERSAL_FORWARDS = new ForwardTraversal();

	/**
	 * Traverses the control flow graph from exits to entries.
	 */
	@SuppressWarnings("unchecked")
	public static <V> IDataFlowTraversal<V> traversalBackwards() {
		return TRAVERSAL_BACKWARDS;
	}

	@SuppressWarnings("rawtypes")
	private static final IDataFlowTraversal TRAVERSAL_BACKWARDS = new BackwardTraversal();



	/**
	 * This executor produces a map that maps each vertex to the analysis result at that vertex.
	 *
	 * @return An executor to use with {@link DataFlowAnalysis#apply(IControlFlowGraph, IDataFlowExecutionFactory)}
	 */
	public IDataFlowExecutionFactory<Vertex, Result, Map<Vertex, Result>> mapExecutor() {
		return new MapDataFlowExecution<>();
	}

	/**
	 * This executor stores the analysis result directly in the AST nodes as a property. The property name is {@link ASTDataFlowExecution#DEFAULT_PROPERTY}.
	 *
	 * @return An executor to use with {@link DataFlowAnalysis#apply(IControlFlowGraph, IDataFlowExecutionFactory)}
	 */
	public IDataFlowExecutionFactory<Vertex, Result, Void> astExecutor() {
		return new ASTDataFlowExecution<>();
	}

	/**
	 * This executor stores the analysis result directly in the AST nodes as a property.
	 *
	 * @param propertyName The name of the property of the AST.
	 *
	 * @return An executor to use with {@link DataFlowAnalysis#apply(IControlFlowGraph, IDataFlowExecutionFactory)}
	 */
	public IDataFlowExecutionFactory<Vertex, Result, Void> astExecutor(String propertyName) {
		return new ASTDataFlowExecution<>(propertyName);
	}

	/**
	 * Creates a new dataflow analysis from a strategy and traversal specification. Use {@link DataFlowAnalysis#apply(IControlFlowGraph, IDataFlowExecutionFactory)}
	 * in order to run the analysis on a control flow graph.
	 *
	 * @param strategy The strategy that defines the analysis.
	 * @param traversal Specifies in which order the control flow graph is traversed.
	 * @return An object that encapsulates the defined analysis.
	 */
	public static <Vertex extends ASTNode, Result> DataFlowAnalysis<Vertex, Result> create(IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {
		return new DataFlowAnalysis<>(strategy, traversal);
	}

	/**
	 * Executes the dataflow analysis for the provided control flow graph. The execution is carried
	 * out by the provided execution factory. The framework provides two execution factories:
	 * {@link DataFlowAnalysis#astExecutor()} and {@link DataFlowAnalysis#mapExecutor()}.
	 *
	 * @param cfg The control flow graph where the analysis is executed on.
	 * @param executionFactory The execution specification that is used to run the analysis.
	 *
	 * @return The result output as defined by the execution specification.
	 */
	public <Output> Output apply(IControlFlowGraph<Vertex> cfg, IDataFlowExecutionFactory<Vertex, Result, Output> executionFactory) {
		IDataFlowExecution<Output> exec = executionFactory.create(cfg, strategy, traversal);

		return exec.execute();
	}

	/**
	 *
	 * @author mirko
	 *
	 * @param <Output>
	 */
	static interface IDataFlowExecution<Output> {
		Output execute();
	}

	/**
	 *
	 * @author mirko
	 *
	 * @param <Vertex>
	 * @param <Result>
	 * @param <Output>
	 */
	protected static interface IDataFlowExecutionFactory<Vertex, Result, Output> {
		IDataFlowExecution<Output> create(IControlFlowGraph<Vertex> cfg, IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal);
	}


	/**
	 *
	 * @author mirko
	 *
	 * @param <Vertex>
	 * @param <Result>
	 * @param <Output>
	 */
	static abstract class AbstractDataFlowExecution<Vertex, Result, Output> implements IDataFlowExecutionFactory<Vertex, Result, Output> {

		abstract AnalysisExecution abstractCreate(IControlFlowGraph<Vertex> cfg, IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal);

		@Override
		public IDataFlowExecution<Output> create(IControlFlowGraph<Vertex> cfg, IDataFlowStrategy<Vertex, Result> strategy,
				IDataFlowTraversal<Vertex> traversal) {
			return abstractCreate(cfg, strategy, traversal);
		}

		protected abstract class AnalysisExecution implements Runnable, IDataFlowExecution<Output> {

			private final IControlFlowGraph<Vertex> cfg;
			private final IDataFlowStrategy<Vertex, Result> strategy;
			private final IDataFlowTraversal<Vertex> traversal;


			protected abstract Result getResultOf(Vertex vertex);

			protected abstract boolean resultHasChanged(Vertex vertex, Result newResult);

			protected abstract void setResult(Vertex vertex, Result newResult);

			protected abstract Output getOutput();


			public AnalysisExecution(IControlFlowGraph<Vertex> cfg, IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {

				this.cfg = Objects.requireNonNull(cfg);
				this.traversal = Objects.requireNonNull(traversal);
				this.strategy =  Objects.requireNonNull(strategy);
;
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
				Set<Vertex> entries = traversal.entryNodes(cfg);
				queue.addAll(entries);

				while (!queue.isEmpty()) {
					Vertex currentVertex = queue.poll();

					Collection<Vertex> predecessors = traversal.predecessorsOf(cfg, currentVertex);

					//Compute the incoming result as merge of all outgoing results from
					//predecessors.
					Result incomingResult;
					switch (predecessors.size()) {
					case 0 :
						incomingResult = entries.contains(currentVertex) ? strategy.entryResult() : strategy.initResult();
						break;
					case 1 :
						incomingResult = getResultOrInit(predecessors.iterator().next());
						break;
					default :
						List<Result> incomingResults =
							predecessors.stream().map(node -> getResultOrInit(node)).collect(Collectors.toList());
						incomingResult = strategy.mergeAll(incomingResults);
					}
					//Compute the outgoing result by applying the transformation of the node.
					Result outgoingResult = strategy.transform(currentVertex, incomingResult);

					//If the result has changed, then add all successors to the queue.
					if (resultHasChanged(currentVertex, outgoingResult)) {
						setResult(currentVertex, outgoingResult);
						queue.addAll(traversal.successorsOf(cfg, currentVertex));
					}
				}
			}

			private Result getResultOrInit(Vertex node) {
				Result res = getResultOf(node);
				if (res == null)
					return strategy.initResult();
				else
					return res;
			}

		};
	}


	public static class ASTDataFlowExecution<Vertex extends ASTNode, Result> extends AbstractDataFlowExecution<Vertex, Result, Void>{

		public final static String DEFAULT_PROPERTY = "dataflow-result";

		private final String propertyName;

		ASTDataFlowExecution(String propertyName) {
			this.propertyName = propertyName;
		}

		ASTDataFlowExecution() {
			this(DEFAULT_PROPERTY);
		}


		String getPropertyName() {
			return propertyName;
		}


		@Override
		AbstractDataFlowExecution<Vertex, Result, Void>.AnalysisExecution abstractCreate(
				IControlFlowGraph<Vertex> cfg, IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {
			return new AnalysisExecution(cfg, strategy, traversal) {

				@Override
				protected
				Result getResultOf(Vertex vertex) {
					return (Result) vertex.getProperty(propertyName);
				}

				@Override
				protected
				boolean resultHasChanged(Vertex vertex, Result newResult) {
					Result res = getResultOf(vertex);
					return !Objects.equals(res, newResult);
				}

				@Override
				protected
				void setResult(Vertex vertex, Result newResult) {
					vertex.setProperty(propertyName, newResult);
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

	static class MapDataFlowExecution<Vertex, Result> extends AbstractDataFlowExecution<Vertex, Result, Map<Vertex, Result>>{

		@Override
		AbstractDataFlowExecution<Vertex, Result, Map<Vertex, Result>>.AnalysisExecution abstractCreate(
				IControlFlowGraph<Vertex> cfg, IDataFlowStrategy<Vertex, Result> strategy, IDataFlowTraversal<Vertex> traversal) {
			return new AnalysisExecution(cfg, strategy, traversal) {

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



}
