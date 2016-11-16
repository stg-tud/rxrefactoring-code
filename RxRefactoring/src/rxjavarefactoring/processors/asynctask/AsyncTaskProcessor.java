package rxjavarefactoring.processors.asynctask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import rxjavarefactoring.framework.refactoring.AbstractProcessor;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.processors.CuCollector;
import rxjavarefactoring.processors.WorkerStatus;
import rxjavarefactoring.processors.asynctask.workers.AnonymAsyncTaskWorker;

/**
 * Description: Refactors AsyncTasks by using
 * {@link AbstractRefactorWorker}s<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class AsyncTaskProcessor extends AbstractProcessor<CuCollector>
{
	public AsyncTaskProcessor( CuCollector collector, String name )
	{
		super( collector, name );
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
		// TODO: Add workers to the set here so that they are invoked
		// concurrently.
		// workers.add(anonymAsyncTaskWorker);
		// workers.add(anonymAsyncTaskWorker);
		// workers.add(anonymAsyncTaskWorker);

		startWorkers( workers );
		executeChanges( monitor );
		return null;
	}
}