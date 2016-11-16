package rxjavarefactoring.processors.swingworker;

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
import rxjavarefactoring.processors.swingworker.workers.AnonymSwingWorkerWorker;

/**
 * Description: Refactors SwingWorkers by using
 * {@link AbstractRefactorWorker}s<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class SwingWorkerProcessor extends AbstractProcessor<CuCollector>
{
	public SwingWorkerProcessor( CuCollector collector, String name )
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
		AnonymSwingWorkerWorker anonymSwingWorkerWorker = new AnonymSwingWorkerWorker( collector, monitor, rxMultipleChangeWriter );

		Set<Callable<WorkerStatus>> workers = new HashSet<>();
		workers.add( anonymSwingWorkerWorker );

		startWorkers( workers );
		executeChanges( monitor );
		return null;
	}
}
