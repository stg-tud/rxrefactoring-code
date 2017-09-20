package de.tudarmstadt.rxrefactoring.core;


public interface IWorkerTree {

	/**
	 * Adds a new top level worker. It gets null as input.
	 * 
	 * @param worker
	 *            The worker to add.
	 * 
	 * @return A reference to the location of the worker in the worker tree. This
	 *         can be used to define workers that use the results of this worker.
	 */
	public <Y> IWorkerRef<Void, Y> addWorker(IWorker<Void, Y> worker);

	/**
	 * Adds a new worker that uses the result of another worker.
	 * 
	 * @param parent
	 *            The node of the worker that this worker relies on.
	 * @param worker
	 *            The worker to add.
	 * 
	 * @return A reference to the location of the worker in the worker tree. This
	 *         can be used to define workers that use the results of this worker.
	 */
	public <X, Y> IWorkerRef<X, Y> addWorker(IWorkerRef<?, X> parent, IWorker<X, Y> worker);
}
