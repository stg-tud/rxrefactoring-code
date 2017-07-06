package de.tudarmstadt.refactoringrx.core.processors;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;

import de.tudarmstadt.refactoringrx.core.RxJavaRefactoringExtension;
import de.tudarmstadt.refactoringrx.core.collect.AbstractCollector;
import de.tudarmstadt.refactoringrx.core.collect.Collector;
import de.tudarmstadt.refactoringrx.core.utils.RxLogger;
import de.tudarmstadt.refactoringrx.core.workers.AbstractRefactorWorker;

/**
 * Description: Refactors SwingWorkers by using
 * {@link AbstractRefactorWorker}s<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class RefactoringProcessor extends AbstractRefactoringProcessor
{
	private RxJavaRefactoringExtension extension;

	public RefactoringProcessor( RxJavaRefactoringExtension extension, Collector collector ) {
		super( collector );
		this.extension = extension;
	}

	@Override
	public Change createChange( IProgressMonitor monitor ) throws CoreException, OperationCanceledException
	{
		Set<AbstractRefactorWorker> workers;
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
			RxLogger.notifyExceptionInClient( throwable );
			return null;
		}

		for ( AbstractRefactorWorker worker : workers )
		{
			worker.setMonitor( monitor );
			worker.setRxMultipleUnitsWriter( rxMultipleUnitsWriter );
		}

		try {
			startWorkers( workers );
			executeChanges( monitor );
			//RxLogger.showInConsole( this, collector.getInfo() );
		} catch ( Exception e ) {
			//RxLogger.showInConsole( this, collector.getError() );
			RxLogger.error(this, "Error occured during execution of workers.", e);
		}
		return null;
	}
}
