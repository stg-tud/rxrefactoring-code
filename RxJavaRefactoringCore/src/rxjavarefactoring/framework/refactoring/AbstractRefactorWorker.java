package rxjavarefactoring.framework.refactoring;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;

import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxMultipleChangeWriter;
import rxjavarefactoring.processor.WorkerStatus;

/**
 * Description: Abstract refactor worker. It forces definition of the minimum
 * parameters required to execute a refactoring task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public abstract class AbstractRefactorWorker<T extends AbstractCollector> implements Callable<WorkerStatus>
{
	protected RxMultipleChangeWriter rxMultipleChangeWriter;
	protected IProgressMonitor monitor;
	protected final T collector;

	public AbstractRefactorWorker( T collector )
	{
		this.collector = collector;
	}

	public void setRxMultipleChangeWriter( RxMultipleChangeWriter rxMultipleChangeWriter )
	{
		this.rxMultipleChangeWriter = rxMultipleChangeWriter;
	}

	public void setMonitor( IProgressMonitor monitor )
	{
		this.monitor = monitor;
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
