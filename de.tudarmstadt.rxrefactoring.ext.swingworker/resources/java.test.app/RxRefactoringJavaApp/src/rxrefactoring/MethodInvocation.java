package rxrefactoring;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

public class MethodInvocation
{
	private SwingWorker<String, Integer> swingWorker;

	public void swingWorkerInvocations()
	{
		swingWorker.addPropertyChangeListener(null);
		swingWorker.cancel(true);
		swingWorker.execute();
		swingWorker.firePropertyChange("propertyName", "oldValue", "newValue");

		try
		{
			String asyncResult = swingWorker.get();
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
			String asyncResult = swingWorker.get(1000L, TimeUnit.SECONDS);
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

		int progress = swingWorker.getProgress();

		PropertyChangeSupport propertyChangeSupport = swingWorker.getPropertyChangeSupport();

		SwingWorker.StateValue state = swingWorker.getState();

		boolean cancelled = swingWorker.isCancelled();

		boolean done = swingWorker.isDone();

		swingWorker.removePropertyChangeListener(null);

		swingWorker.run();
	}
}