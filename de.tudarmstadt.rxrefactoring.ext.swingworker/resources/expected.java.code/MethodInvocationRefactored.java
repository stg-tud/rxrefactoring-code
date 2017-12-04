package rxrefactoring;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;

public class MethodInvocation
{
	private SWSubscriber<String, Integer> rxObserver;

	public void swingWorkerInvocations()
	{
		rxObserver.addPropertyChangeListener( null );
		rxObserver.cancelObservable( true );
		rxObserver.executeObservable();
		rxObserver.firePropertyChange( "propertyName", "oldValue", "newValue" );

		try
		{
			String asyncResult = rxObserver.get();
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch ( ExecutionException e )
		{
			e.printStackTrace();
		}

		try
		{
			String asyncResult = rxObserver.get( 1000L, TimeUnit.SECONDS );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch ( TimeoutException e )
		{
			e.printStackTrace();
		}
		catch ( ExecutionException e )
		{
			e.printStackTrace();
		}

		int progress = rxObserver.getProgress();

		PropertyChangeSupport propertyChangeSupport = rxObserver.getPropertyChangeSupport();

		SwingWorker.StateValue state = rxObserver.getState();

		boolean cancelled = rxObserver.isCancelled();

		boolean done = rxObserver.isDone();

		rxObserver.removePropertyChangeListener( null );

		rxObserver.runObservable();
	}
}