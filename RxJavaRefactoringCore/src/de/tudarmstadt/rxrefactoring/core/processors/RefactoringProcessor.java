package de.tudarmstadt.rxrefactoring.core.processors;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;

import de.tudarmstadt.rxrefactoring.core.RxRefactoringExtension;
import de.tudarmstadt.rxrefactoring.core.collect.AbstractCollector;
import de.tudarmstadt.rxrefactoring.core.collect.Collector;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractRefactorWorker;
import de.tudarmstadt.rxrefactoring.core.workers.RxRefactorWorker;

/**
 * Description: Refactors SwingWorkers by using
 * {@link AbstractRefactorWorker}s<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class RefactoringProcessor extends AbstractRefactoringProcessor
{
	private RxRefactoringExtension extension;

	public RefactoringProcessor( RxRefactoringExtension extension, Collector collector ) {
		super( collector );
		this.extension = extension;
	}

	@Override
	public Change createChange( IProgressMonitor monitor ) throws CoreException, OperationCanceledException
	{
		Iterable<RxRefactorWorker> workers;
		try
		{
			workers = extension.getRefactoringWorkers( collector );
			if ( workers == null )
			{
				new IllegalArgumentException( "getRefactoringWorkers must return not null" );
			}
		}
		catch ( Throwable throwable )
		{
			Log.notifyExceptionInClient( throwable );
			return null;
		}

		for ( RxRefactorWorker worker : workers ) {
			worker.setMonitor( monitor );
			worker.setRxMultipleUnitsWriter( rxMultipleUnitsWriter );
		}

		try {
			startWorkers( workers );
			executeChanges( monitor );
			//RxLogger.showInConsole( this, collector.getInfo() );
		} catch ( Exception e ) {
			//RxLogger.showInConsole( this, collector.getError() );
			Log.error(this, "Error occured during execution of workers.", e);
		}
		return null;
	}
}
