package rxjavarefactoring.framework.refactoring;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxMultipleChangeWriter;
import rxjavarefactoring.processor.WorkerStatus;

/**
 * Description: Abstract processor. It forces defining the minimum requirements
 * for a processor<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public abstract class AbstractRefactoringProcessor<T extends AbstractCollector> extends Refactoring
{
	protected final T collector;
	protected final RxMultipleChangeWriter rxMultipleChangeWriter;

	public AbstractRefactoringProcessor( AbstractCollector collector )
	{
		this.collector = (T) collector;
		rxMultipleChangeWriter = new RxMultipleChangeWriter();
	}

	@Override
	public String getName()
	{
		return collector.getCollectorName();
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

	public Map<ICompilationUnit, String> getICompilationUnitVsNewSourceCodeMap()
	{
		return rxMultipleChangeWriter.getIcuVsNewSourceCodeMap();
	}

	protected <T extends AbstractCollector> void startWorkers( Set<AbstractRefactorWorker<T>> workers )
	{
		Set<Callable<WorkerStatus>> workerSet = new HashSet<>();
		workers.stream().forEach( worker -> workerSet.add( worker ) );

		ExecutorService executor = Executors.newWorkStealingPool();
		try
		{
			executor.invokeAll( workerSet );
		}
		catch ( InterruptedException e )
		{
			RxLogger.error( this, "createChange: Interrupted", e );
		}
	}

	protected void executeChanges( IProgressMonitor monitor )
	{
		rxMultipleChangeWriter.executeChanges( monitor );
		monitor.done();
	}
}
