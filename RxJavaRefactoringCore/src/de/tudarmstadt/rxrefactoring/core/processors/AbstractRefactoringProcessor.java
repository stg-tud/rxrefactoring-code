package de.tudarmstadt.rxrefactoring.core.processors;

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

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
import de.tudarmstadt.rxrefactoring.core.collect.AbstractCollector;
import de.tudarmstadt.rxrefactoring.core.collect.Collector;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractRefactorWorker;
import de.tudarmstadt.rxrefactoring.core.workers.RxRefactorWorker;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriterExecution;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;

/**
 * Description: Abstract processor. It forces defining the minimum requirements
 * for a processor<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public abstract class AbstractRefactoringProcessor<CollectorType extends Collector> extends Refactoring {
	protected final CollectorType collector;
	protected final UnitWriterExecution rxMultipleUnitsWriter;

	public AbstractRefactoringProcessor( CollectorType collector )
	{
		this.collector = (CollectorType) collector;
		rxMultipleUnitsWriter = new UnitWriterExecution();
		UnitWriters.initializeUnitWriters();
		IdManager.reset();
	}

	@Override
	public String getName()	{
		return collector.getName();
	}

	@Override
	public RefactoringStatus checkInitialConditions( IProgressMonitor iProgressMonitor ) throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public RefactoringStatus checkFinalConditions( IProgressMonitor iProgressMonitor ) throws CoreException, OperationCanceledException {
		return null;
	}

	/**
	 * Start workers concurrently
	 * 
	 * @param workers
	 *            workers to be started
	 * @param <T>
	 *            collector type
	 */
	protected <T extends AbstractCollector> void startWorkers( Iterable<RxRefactorWorker> workers )	{
		Set<Callable<WorkerStatus>> workerSet = new HashSet<>();		
		
		workers.forEach( worker -> workerSet.add( worker ) );

		ExecutorService executor = Executors.newWorkStealingPool();
		try
		{
			executor.invokeAll(workerSet);
		}
		catch ( InterruptedException e )
		{
			Log.error( getClass(), "createChange: Interrupted", e );
		}
	}

	/**
	 * Executes the changes registered in {@link this#rxMultipleUnitsWriter}
	 * 
	 * @param monitor
	 *            progress monitor
	 */
	protected void executeChanges( IProgressMonitor monitor ) {
		rxMultipleUnitsWriter.execute( monitor );
		monitor.done();
	}
}
