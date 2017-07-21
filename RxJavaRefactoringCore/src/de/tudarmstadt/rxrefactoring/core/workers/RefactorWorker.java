package de.tudarmstadt.rxrefactoring.core.workers;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;

import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriterExecution;

public interface RefactorWorker extends Callable<WorkerStatus> {

	/**
	 * Refactors the given code.
	 */
	public WorkerStatus refactor();
	
	public void setRxMultipleUnitsWriter(UnitWriterExecution rxMultipleUnitsWriter);

	public void setMonitor(IProgressMonitor monitor);
	
	
	@Override
	default public WorkerStatus call() throws Exception {
		try	{
			Log.info( getClass(), "METHOD=call - Starting worker in thread: " + Thread.currentThread().getName() );
			return refactor();
		} catch ( Exception e ) {
			Log.error( getClass(), "METHOD=call", e );
			return WorkerStatus.ERROR;
		}
	}


}