package rxjavarefactoring.framework.refactoring;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;

import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxMultipleChangeWriter;
import rxjavarefactoring.processors.WorkerStatus;

/**
 * Description: Abstract refactor worker. It forces definition of the minimum
 * parameters required to execute a refactoring task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public abstract class AbstractRefactorWorker<T extends AbstractCollector> implements Callable<WorkerStatus>
{
	protected final RxMultipleChangeWriter rxMultipleChangeWriter;
	protected final IProgressMonitor monitor;
	protected final T collector;

	public AbstractRefactorWorker(
			T collector,
			IProgressMonitor monitor,
			RxMultipleChangeWriter rxMultipleChangeWriter )
	{
		this.collector = collector;
		this.monitor = monitor;
		this.rxMultipleChangeWriter = rxMultipleChangeWriter;
	}

	@Override
	public WorkerStatus call() throws Exception
	{
		try
		{
			RxLogger.info( this, "METHOD=call - Starting worker in thread: " + Thread.currentThread().getName() );
			return refactor();
		}
		catch ( Exception e )
		{
			RxLogger.error( this, "METHOD=call", e );
			return WorkerStatus.ERROR;
		}
	}

	protected abstract WorkerStatus refactor();
}
