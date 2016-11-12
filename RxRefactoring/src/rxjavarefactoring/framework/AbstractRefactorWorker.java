package rxjavarefactoring.framework;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;

import rxjavarefactoring.processors.CuCollector;
import rxjavarefactoring.processors.WorkerStatus;

/**
 * Description: Forces definition of the minimum parameters required to execute
 * a refactoring task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public abstract class AbstractRefactorWorker implements Callable<WorkerStatus>
{
	protected final RxMultipleChangeWriter rxMultipleChangeWriter;
	protected final IProgressMonitor monitor;
	protected final CuCollector collector;

	public AbstractRefactorWorker(
			CuCollector collector,
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
		return null;
	}
}
