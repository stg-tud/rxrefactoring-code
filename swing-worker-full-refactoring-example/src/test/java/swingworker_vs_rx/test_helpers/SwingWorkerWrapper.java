package swingworker_vs_rx.test_helpers;

import rxswingworker.RxSwingWorkerAPI;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class SwingWorkerWrapper<ReturnType, ProcessType> implements RxSwingWorkerAPI<ReturnType>
{
	SwingWorker<ReturnType, ProcessType> swingWorker;

	public SwingWorkerWrapper( SwingWorker<ReturnType, ProcessType> swingWorker )
	{
		this.swingWorker = swingWorker;
	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		swingWorker.addPropertyChangeListener( listener );
	}

	@Override
	public boolean cancel( boolean mayInterruptIfRunning )
	{
		return swingWorker.cancel( mayInterruptIfRunning );
	}

	@Override
	public void execute()
	{
		swingWorker.execute();
	}

	@Override
	public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
	{
		swingWorker.firePropertyChange( propertyName, oldValue, newValue );
	}

	@Override
	public ReturnType get()  throws InterruptedException, ExecutionException
	{
		try
		{
			return swingWorker.get();
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch ( ExecutionException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ReturnType get( long timeout, TimeUnit unit ) throws InterruptedException, TimeoutException, ExecutionException
	{
		try
		{
			return swingWorker.get( timeout, unit );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch ( ExecutionException e )
		{
			e.printStackTrace();
		}
		catch ( TimeoutException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getProgress()
	{
		return swingWorker.getProgress();
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return swingWorker.getPropertyChangeSupport();
	}

	@Override
	public SwingWorker.StateValue getState()
	{
		return swingWorker.getState();
	}

	@Override
	public boolean isCancelled()
	{
		return swingWorker.isCancelled();
	}

	@Override
	public boolean isDone()
	{
		return swingWorker.isDone();
	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		swingWorker.removePropertyChangeListener( listener );
	}

	@Override
	public void run()
	{
		swingWorker.run();
	}
}
