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

import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxMultipleUnitsWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
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
	protected final RxMultipleUnitsWriter rxMultipleUnitsWriter;

	public AbstractRefactoringProcessor( AbstractCollector collector )
	{
		this.collector = (T) collector;
		rxMultipleUnitsWriter = new RxMultipleUnitsWriter();
		RxSingleUnitWriterMapHolder.initializeUnitWriters();
		DynamicIdsMapHolder.reset();
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

	/**
	 * Returns a map that contains the final source code for each compilation unit
	 * that was refactored.
	 * 
	 * @return compilation unit vs refactored code
	 */
	public Map<ICompilationUnit, String> getICompilationUnitVsNewSourceCodeMap()
	{
		return rxMultipleUnitsWriter.getIcuVsNewSourceCodeMap();
	}

	/**
	 * Start workers concurrently
	 * 
	 * @param workers
	 *            workers to be started
	 * @param <T>
	 *            collector type
	 */
	protected <T extends AbstractCollector> void startWorkers( Set<AbstractRefactorWorker<T>> workers )
	{
		Set<Callable<WorkerStatus>> workerSet = new HashSet<>();
		workers.stream().forEach( worker -> workerSet.add( worker ) );

		ExecutorService executor = Executors.newWorkStealingPool();
		try
		{
			executor.invokeAll(workerSet);
		}
		catch ( InterruptedException e )
		{
			RxLogger.error( this, "createChange: Interrupted", e );
		}
	}

	/**
	 * Executes the changes registered in {@link this#rxMultipleUnitsWriter}
	 * 
	 * @param monitor
	 *            progress monitor
	 */
	protected void executeChanges( IProgressMonitor monitor )
	{
		rxMultipleUnitsWriter.executeChanges( monitor );
		monitor.done();
	}
}
