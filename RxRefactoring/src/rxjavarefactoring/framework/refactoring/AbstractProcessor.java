package rxjavarefactoring.framework.refactoring;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.Refactoring;

import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxMultipleChangeWriter;
import rxjavarefactoring.processors.WorkerStatus;

/**
 * Description: Abstract processor. It forces defining the minimum requirements
 * for a processor<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public abstract class AbstractProcessor<T extends AbstractCollector> extends Refactoring
{
	private final String name;

	protected final T collector;
	protected final RxMultipleChangeWriter rxMultipleChangeWriter;

	public AbstractProcessor( T collector, String name )
	{
		this.name = name;
		this.collector = collector;
		rxMultipleChangeWriter = new RxMultipleChangeWriter();
	}

	@Override
	public String getName()
	{
		return name;
	}

	public Map<ICompilationUnit, String> getICompilationUnitVsNewSourceCodeMap()
	{
		return rxMultipleChangeWriter.getIcuVsNewSourceCodeMap();
	}

	protected void startWorkers( Set<Callable<WorkerStatus>> workers )
	{
		ExecutorService executor = Executors.newWorkStealingPool();
		try
		{
			executor.invokeAll( workers );
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
