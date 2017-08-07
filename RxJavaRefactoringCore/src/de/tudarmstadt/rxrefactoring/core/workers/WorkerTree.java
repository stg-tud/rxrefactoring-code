package de.tudarmstadt.rxrefactoring.core.workers;

import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.ProjectSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;

/**
 * This class defines hierarchies of workers that may rely
 * on the results of each other.
 * 
 * @author mirko
 *
 */
public class WorkerTree {

	private final ProjectUnits units;
	
	private final ProjectSummary summary;
	
	public WorkerTree(ProjectUnits units, ProjectSummary summary) {
		this.units = units;
		this.summary = summary;
	}	
	
	
	public class WorkerNode<Input, Output> {
		private final IWorker<Input, Output> worker;
		private final WorkerNode<?,Input> parent;
		
		private boolean hasResult = false;
		private Output result;
		
		WorkerNode(IWorker<Input, Output> worker, WorkerNode<?,Input> parent) {
			this.worker = worker;
			this.parent = parent;
		}
		
		Output getResult() {
			if (!hasResult) throw new IllegalStateException("You have to run this worker node before retrieving its result.");			
			return result;
		}
		
		void run(WorkerSummary workerSummary) throws Exception {
			if (!hasResult) {
				Input in = parent == null ? null : parent.getResult();		
				//TODO: Pass the correct summary.
				result = worker.refactor(in, units, workerSummary); 
			}
		}
	}
	
	private final WorkerNode<Void, Void> root = new WorkerNode<>(new NullWorker(), null);
	
	private final List<WorkerNode<?,?>> workers = Lists.newLinkedList();
	
	/**
	 * Adds a new top level worker. It gets null as input.
	 * 
	 * @param worker The worker to add.
	 * 
	 * @return A reference to the location of the worker
	 * in the worker tree. This can be used to define workers
	 * that use the results of this worker.
	 */
	public <Y> WorkerNode<Void,Y> addWorker(IWorker<Void,Y> worker) {		
		WorkerNode<Void,Y> node = new WorkerNode<>(worker, root);
		workers.add(node);
		return node;
	}
	
	/**
	 * Adds a new worker that uses the result of another worker.
	 * 
	 * @param parent The node of the worker that this worker relies on.
	 * @param worker The worker to add.
	 * 
	 * @return A reference to the location of the worker
	 * in the worker tree. This can be used to define workers
	 * that use the results of this worker.
	 */
	public <X,Y> WorkerNode<X,Y> addWorker(WorkerNode<?,X> parent, IWorker<X,Y> worker) {		
		WorkerNode<X,Y> node = new WorkerNode<>(worker, parent);
		workers.add(node);
		return node;
	}
	
	public void run() {
		
		Stack<WorkerNode<?,?>> stack = new Stack<>();		
		stack.add(root);
		
		while (!stack.isEmpty()) {			
			WorkerNode<?,?> current = stack.pop();	
			WorkerSummary workerSummary = summary.reportWorker(current.worker);
			
			Log.info(getClass(), "Run worker: " + current.worker.getName());
			
			try {
				current.run(workerSummary);
				
				for (WorkerNode<?,?> node : workers) {
					if (node.parent == current)
						stack.push(node);
				}
				
				workerSummary.setStatus(WorkerStatus.COMPLETED);
				
			} catch (Exception e) {
				workerSummary.setStatus(WorkerStatus.ERROR);
				Log.error(getClass(), "## Error during the execution of " + current.worker.getName() + " ##");
				e.printStackTrace();
				Log.error(getClass(), "## End ##");
			}				
		}
		
	}
	
	
}
