package rxrefactoring;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

/**
 * Description: Basic class for test purposes <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class GeneralCase
{
	private static final int AMOUNT_OF_WORK = 10;
	private static final long TIME_FOR_WORK_UNIT = 2000L;

	private SwingWorker<String, Integer> swingWorker;

	public void someMethod()
	{
		swingWorker = new SwingWorker<String, Integer>()
		{

			@Override
			protected String doInBackground() throws Exception
			{
				System.out.println("Entering doInBackground() method");
				for ( int i = 0; i < AMOUNT_OF_WORK * 2; i = i + 2 )
				{
					publish(i, i + 1);
					Thread.sleep(TIME_FOR_WORK_UNIT);
				}
				System.out.println("doInBackground() finished successfully");
				return "Async Result";
			}

			@Override
			protected void process(List<Integer> chunks)
			{
				for ( Integer number : chunks )
				{
					System.out.println("Processing " + number);
					setProgress(number * 100 / (AMOUNT_OF_WORK * 2));
				}
			}

			@Override
			protected void done()
			{
				try
				{
					System.out.println("Entering done() method");
					String result = get();
					System.out.println("doInBackground() result = " + result);
				}
				catch ( InterruptedException e )
				{
					System.out.println("InterruptedException");
				}
				catch ( ExecutionException e )
				{
					System.out.println("ExecutionException");
				}
			}
		};

		SwingWorker<String, Integer> swingWorkerRef = swingWorker;
		SwingWorker swingWorkerRef2 = swingWorker;

		doSomething(swingWorkerRef);

		new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				return null;
			}
		};

		new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				return null;
			}
		}.execute();
	}

	private void doSomething(SwingWorker<String, Integer> anotherSwingWorker)
	{
		anotherSwingWorker.execute();
	}

	public void swingWorkerCalls()
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
