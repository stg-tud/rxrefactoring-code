package de.tudarmstadt.rxrefactoring.core.workers;

import org.eclipse.core.runtime.IProgressMonitor;

import de.tudarmstadt.rxrefactoring.core.collect.Collector;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriterExecution;

/**
 * Description: Abstract refactor worker. It forces definition of the minimum
 * parameters required to execute a refactoring task<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public abstract class AbstractRefactorWorker<CollectorType extends Collector> implements RxRefactorWorker {
	
	protected UnitWriterExecution execution;
	protected IProgressMonitor monitor;
	protected final CollectorType collector;

	public AbstractRefactorWorker(CollectorType collector) {
		this.collector = collector;
	}

	public void setRxMultipleUnitsWriter(UnitWriterExecution execution)	{
		this.execution = execution;
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}	
}
