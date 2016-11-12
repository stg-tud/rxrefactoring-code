package rxjavarefactoring.processors.asynctask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import rxjavarefactoring.framework.RxLogger;
import rxjavarefactoring.framework.RxMultipleChangeWriter;
import rxjavarefactoring.processors.CuCollector;
import rxjavarefactoring.processors.WorkerStatus;
import rxjavarefactoring.processors.asynctask.workers.AnonymAsyncTaskWorker;

/**
 * Description: Refactors AsyncTasks by using {@link rxjavarefactoring.framework.AbstractRefactorWorker}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class AsyncTaskProcessor extends Refactoring
{
	private CuCollector collector;

	public AsyncTaskProcessor( CuCollector asyncTaskCollector )
	{
		this.collector = asyncTaskCollector;
	}

	@Override
	public String getName()
	{
		return "Convert AsyncTasks To RxObservable";
	}

	@Override
	public RefactoringStatus checkInitialConditions( IProgressMonitor iProgressMonitor ) throws CoreException, OperationCanceledException
	{
		return null;
	}

	@Override
	public RefactoringStatus checkFinalConditions( IProgressMonitor iProgressMonitor ) throws CoreException, OperationCanceledException
	{
		return null;
	}

	@Override
	public Change createChange( IProgressMonitor monitor ) throws CoreException, OperationCanceledException
	{
		RxLogger.info( this, "METHOD=createChange - Starting refactoring" );
		RxMultipleChangeWriter rxMultipleChangeWriter = new RxMultipleChangeWriter();

		// Create Workers
		AnonymAsyncTaskWorker anonymAsyncTaskWorker = new AnonymAsyncTaskWorker( collector, monitor, rxMultipleChangeWriter );
		// TODO: Create other workers in the package "workers" and use them here
		// AnonymAsyncTaskWorker anonymAsyncTaskWorker = new
		// AnonymAsyncTaskWorker( collector, monitor, rxMultipleChangeWriter );
		// AnonymAsyncTaskWorker anonymAsyncTaskWorker = new
		// AnonymAsyncTaskWorker( collector, monitor, rxMultipleChangeWriter );
		// AnonymAsyncTaskWorker anonymAsyncTaskWorker = new
		// AnonymAsyncTaskWorker( collector, monitor, rxMultipleChangeWriter );

		Set<Callable<WorkerStatus>> workers = new HashSet<>();
		workers.add( anonymAsyncTaskWorker );
		// TODO: Add workers to the set here so that they are invoked concurrently.
		// workers.add(anonymAsyncTaskWorker);
		// workers.add(anonymAsyncTaskWorker);
		// workers.add(anonymAsyncTaskWorker);

		// Start workers
		ExecutorService executor = Executors.newWorkStealingPool();
		try
		{
			executor.invokeAll( workers );
		}
		catch ( InterruptedException e )
		{
			RxLogger.error( this, "createChange: Interrupted", e );
		}

		rxMultipleChangeWriter.executeChanges( monitor );
		monitor.done();
		return null;
	}
}
