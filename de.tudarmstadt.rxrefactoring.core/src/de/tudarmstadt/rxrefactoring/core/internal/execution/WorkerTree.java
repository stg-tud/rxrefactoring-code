package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.ProjectStatus;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.ProjectSummary;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

/**
 * This class defines hierarchies of workers that may rely on the results of
 * each other.
 * 
 * @author mirko
 *
 */
public class WorkerTree implements IWorkerTree {

	private final ProjectUnits units;

	private final ProjectSummary summary;

	WorkerTree(@NonNull ProjectUnits units, @NonNull ProjectSummary summary) {
		this.units = units;
		this.summary = summary;
	}

	/**
	 * A node in the worker tree containing the worker.
	 * 
	 * @author mirko
	 *
	 * @param <Input>  The input to the worker.
	 * @param <Output> The output to the worker.
	 */
	private class WorkerNode<Input, Output> implements IWorkerRef<Input, Output> {
		private final String workerName;
		private final WorkerNode<?, Input> parent;

		private IWorker<Input, Output> worker;

		private boolean hasResult = false;
		private @Nullable Output result;

		WorkerNode(IWorker<Input, Output> worker, WorkerNode<?, Input> parent) {
			this.worker = worker;
			this.parent = parent;
			this.workerName = worker.getName();
		}

		public boolean hasResult() {
			return hasResult;
		}

		Output getResult() {
			if (!hasResult())
				throw new IllegalStateException("You have to run this worker node before retrieving its result.");

			return result;
		}

		@Override
		public String toString() {
			return String.format("WorkerNode(worker = %s, parent = %s)", workerName,
					parent == null ? "null" : parent.workerName);
		}

		public Output run(WorkerSummary workerSummary, RefactorScope scope) throws Exception {
			if (!hasResult()) {
				Input in = parent == null ? null : parent.getResult();
				result = worker.refactor(units, in, workerSummary, scope);
				hasResult = true;
				// Dereference the worker to allow garbage collection
				worker = null;
			}
			return result;
		}
	}

	private final @NonNull WorkerNode<Void, Void> root = new WorkerNode<>(new NullWorker(), null);

	@SuppressWarnings("null")
	private final @NonNull List<WorkerNode<?, ?>> workers = Lists.newLinkedList();

	/**
	 * Adds a new top level worker. It gets null as input.
	 * 
	 * @param worker The worker to add.
	 * 
	 * @return A reference to the location of the worker in the worker tree. This
	 *         can be used to define workers that use the results of this worker.
	 */
	public <Y> @NonNull WorkerNode<Void, Y> addWorker(@NonNull IWorker<Void, Y> worker) {
		return addWorker(root, worker);
	}

	/**
	 * Adds a new worker that uses the result of another worker.
	 * 
	 * @param parent The node of the worker that this worker relies on.
	 * @param worker The worker to add.
	 * 
	 * @return A reference to the location of the worker in the worker tree. This
	 *         can be used to define workers that use the results of this worker.
	 */
	public <X, Y> @NonNull WorkerNode<X, Y> addWorker(@NonNull IWorkerRef<?, X> parent, @NonNull IWorker<X, Y> worker) {
		if (!(parent instanceof WorkerNode)) {
			throw new IllegalArgumentException("worker references in this tree have to be of type WorkerNode");
		}

		WorkerNode<X, Y> node = new WorkerNode<>(worker, (WorkerNode<?, X>) parent);
		workers.add(node);
		return node;
	}

	public int size() {
		return workers.size() + 1; // All workers plus the root worker
	}

	/**
	 * Executes the workers in this worker tree. <br>
	 * <br>
	 * This method should not be used by clients.
	 */
	@Deprecated
	public void runOnSameThread() {

		Deque<WorkerNode<?, ?>> stack = new LinkedList<>();
		stack.add(root);

		while (!stack.isEmpty()) {
			WorkerNode<?, ?> current = stack.pop();

			WorkerSummary workerSummary = current == root ? WorkerSummary.createNullSummary()
					: summary.reportWorker(current.worker);

			Log.info(getClass(), "Run worker: " + current.worker.getName());

			try {
				current.run(workerSummary, null); // TODO really not called?

				for (WorkerNode<?, ?> node : workers) {
					if (node.parent == current)
						stack.push(node);
				}

				workerSummary.setStatus(WorkerStatus.COMPLETED);

			} catch (Exception e) {
				workerSummary.setStatus(WorkerStatus.ERROR);
				Log.error(getClass(), "execution of " + current.worker.getName(), e);
			}
		}
	}

	private void traverseChildren(WorkerNode parent, Consumer<WorkerNode> f) {
		for (WorkerNode<?, ?> node : workers) {
			if (node.parent == parent) {
				f.accept(node);
				traverseChildren(node, f);
			}
		}
	}

	/**
	 * Defines the environment in which a refactoring can be executed.
	 * 
	 * @author mirko
	 *
	 */
	private class WorkerTreeExecution {
		private final ListeningExecutorService executor;
		private final CountDownLatch latch;

		public WorkerTreeExecution(ExecutorService executor) {

			if (executor instanceof ListeningExecutorService) {
				this.executor = (ListeningExecutorService) executor;
			} else {
				this.executor = MoreExecutors.listeningDecorator(executor);
			}

			latch = new CountDownLatch(size());
		}

		/**
		 * Starts the execution of a worker node and all its child nodes (recursively).
		 * 
		 * @param workerNode    The node to execute.
		 * 
		 * @param workerSummary The summary to use for this worker node.
		 */
		public void execute(final WorkerNode<?, ?> workerNode, final WorkerSummary workerSummary, RefactorScope scope) {
			Log.info(WorkerTree.class, "Start execution: " + workerNode);
			Futures.addCallback(executor.submit(() -> workerNode.run(workerSummary, scope)),
					new FutureCallback<Object>() {
						@Override
						public void onFailure(Throwable arg0) {
							workerSummary.setStatus(WorkerStatus.ERROR);
							workerSummary.setThrowable(arg0);
							summary.reportStatus(ProjectStatus.ERROR);

							Log.error(WorkerTree.class,
									"An error occured while executing worker: " + workerNode.workerName, arg0);

							traverseChildren(workerNode, node -> {
								Log.error(WorkerTree.class, "Skipped " + node.workerName);

								WorkerSummary ws = summary.reportWorker(node.worker);
								ws.setStatus(WorkerStatus.SKIPPED);

								latch.countDown();
							});
							latch.countDown();
						}

						@Override
						public void onSuccess(Object arg0) {
							workerSummary.setStatus(WorkerStatus.COMPLETED);

							Log.info(WorkerTree.class,
									"Finished execution: " + workerNode + " (remaining: " + latch.getCount() + ")");
							for (WorkerNode<?, ?> node : workers) {
								if (node.parent == workerNode)
									execute(node, summary.reportWorker(node.worker), scope);
							}
							latch.countDown();
						}
					}, executor);
		}

		/**
		 * Wait until the execution of the worker node and all its child nodes has been
		 * finished.
		 * 
		 * @throws InterruptedException if the execution is interrupted externally.
		 */
		public void waitFinished() throws InterruptedException {
			latch.await();
			executor.shutdown();
			executor.awaitTermination(300, TimeUnit.SECONDS);
		}

	}

	public void run(ExecutorService executor, RefactorScope scope) throws InterruptedException {
		WorkerTreeExecution execution = new WorkerTreeExecution(executor);
		WorkerSummary workerSummary = WorkerSummary.createNullSummary();
		execution.execute(root, workerSummary, scope);
		execution.waitFinished();

	}
	
	public void run(ExecutorService executor) throws InterruptedException {
		WorkerTreeExecution execution = new WorkerTreeExecution(executor);
		WorkerSummary workerSummary = WorkerSummary.createNullSummary();
		execution.execute(root, workerSummary, null);
		execution.waitFinished();

	}
}
