package swingworker_vs_rx.test_helpers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import rxswingworker.RxSwingWorkerAPI;
import rxswingworker.SWSubscriber;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class RxSwingWorkerWrapper<ReturnType, ProcessType> implements RxSwingWorkerAPI<ReturnType>
{
	SWSubscriber<ReturnType, ProcessType> observer;

	public RxSwingWorkerWrapper( SWSubscriber<ReturnType, ProcessType> observer )
	{
		this.observer = observer;
	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		observer.addPropertyChangeListener( listener );
	}

	@Override
	public boolean cancel( boolean mayInterruptIfRunning )
	{
		return observer.cancel( mayInterruptIfRunning );
	}

	@Override
	public void execute()
	{
		observer.execute();
	}

	@Override
	public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
	{
		observer.firePropertyChange( propertyName, oldValue, newValue );
	}

	@Override
	public ReturnType get()
	{
		try
		{
			return observer.get();
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ReturnType get( long timeout, TimeUnit unit )
	{
		try
		{
			return observer.get( timeout, unit );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getProgress()
	{
		return observer.getProgress();
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return observer.getPropertyChangeSupport();
	}

	@Override
	public SwingWorker.StateValue getState()
	{
		return observer.getState();
	}

	@Override
	public boolean isCancelled()
	{
		return observer.isCancelled();
	}

	@Override
	public boolean isDone()
	{
		return observer.isDone();
	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		observer.removePropertyChangeListener( listener );
	}

	@Override
	public void run()
	{
		observer.run();
	}
}
