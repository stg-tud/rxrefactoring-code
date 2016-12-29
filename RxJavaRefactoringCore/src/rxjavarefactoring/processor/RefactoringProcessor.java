package rxjavarefactoring.processor;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;

import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractCollector;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.refactoring.AbstractRefactoringProcessor;
import rxjavarefactoring.framework.utils.RxLogger;

/**
 * Description: Refactors SwingWorkers by using
 * {@link AbstractRefactorWorker}s<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class RefactoringProcessor extends AbstractRefactoringProcessor
{
	private RxJavaRefactoringExtension extension;

	public RefactoringProcessor( RxJavaRefactoringExtension extension, AbstractCollector collector )
	{
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

		startWorkers( workers );
		executeChanges( monitor );
		RxLogger.info( this, collector.getInfo() );
		return null;
	}
}
