package de.tudarmstadt.rxrefactoring.core.processors;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;

import de.tudarmstadt.rxrefactoring.core.RefactoringExtension;
import de.tudarmstadt.rxrefactoring.core.collect.AbstractCollector;
import de.tudarmstadt.rxrefactoring.core.collect.Collector;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractWorker;
import de.tudarmstadt.rxrefactoring.core.workers.RefactorWorker;

/**
 * Description: Refactors SwingWorkers by using
 * {@link AbstractWorker}s<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class RefactoringProcessor extends AbstractRefactoringProcessor
{
	private RefactoringExtension extension;

	public RefactoringProcessor( RefactoringExtension extension, Collector collector ) {
		super( collector );
		this.extension = extension;
	}

	@Override
	public Change createChange( IProgressMonitor monitor ) throws CoreException, OperationCanceledException
	{
		Iterable<RefactorWorker> workers;
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
			Log.errorInClient(getClass(), throwable);
			return null;
		}

		for ( RefactorWorker worker : workers ) {
			worker.setMonitor( monitor );
			worker.setRxMultipleUnitsWriter( rxMultipleUnitsWriter );
		}

		try {
			startWorkers( workers );
			executeChanges( monitor );
			//RxLogger.showInConsole( this, collector.getInfo() );
		} catch ( Exception e ) {
			//RxLogger.showInConsole( this, collector.getError() );
			Log.error(getClass(), "Error occured during execution of workers.", e);
		}
		return null;
	}
}
