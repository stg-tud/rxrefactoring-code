package complete_swingworker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/30/2016
 */
public class FullSwingWorkerExample
{
	private SwingWorker<String, Integer> mySwingWorker;

	public FullSwingWorkerExample( long timeout, int amountOfWork )
	{
		mySwingWorker = new SwingWorker<String, Integer>()
		{
			private static final String SEPARATOR = " - ";

			// private method in SwingWorker
			// private void setState(StateValue state) {
			// StateValue old = this.state;
			// this.state = state;
			// firePropertyChange("state", old, state);
			// }

			// setState(StateValue.STARTED) is always called before doInBackground()
			// refactor only if getState is used, otherwise it is not needed
			@Override
			protected String doInBackground() throws Exception
			{
				printMessage( "Entering doInBackground() method" );
				for ( int i = 0; i < amountOfWork * 2; i = i + 2 )
				{
					Thread.sleep( 2000L );
					publish( i, i + 1 );
				}
				printMessage( "doInBackground() finished successfully" );
				return "Async Result";
			}

			@Override
			protected void process( List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					printMessage( "Processing " + number );
					// setProgress calls firePropertyChange("progress", old, state);
					// in this case we can use a progress subscriber
					// only refactor is setProgress and getProgress are used
					setProgress( number * 100 / ( amountOfWork * 2 ) );
				}
			}

			// setState(StateValue.DONE) is always called before done()
			// refactor only if getState is used, otherwise it is not needed
			@Override
			protected void done()
			{
				try
				{
					printMessage( "Entering done() method" );
					String result = get( timeout, TimeUnit.SECONDS );
					printMessage( "doInBackground() result = " + result );
				}
				catch ( InterruptedException e )
				{
					printMessage( "InterruptedException" );
				}
				catch ( ExecutionException e )
				{
					printMessage( "ExecutionException" );
				}
				catch ( TimeoutException e )
				{
					printMessage( "TimeoutException" );
				}
			}

			private void printMessage( String message )
			{
				System.out.println( "[" + Thread.currentThread().getName() + "]" + SEPARATOR + message );
			}
		};
	}

	// PROPERTIES: default "state", "progress"
	public void addPropertyChangeListenerToSwingWorker( PropertyChangeListener listener )
	{
		mySwingWorker.addPropertyChangeListener( listener );
	}

	public void firePropertyChangeInSwingWorker( String propertyName, Object oldValue, Object newValue )
	{
		mySwingWorker.firePropertyChange( propertyName, oldValue, newValue );
	}

	public PropertyChangeSupport getSwingWorkerPropertyChangeSupport()
	{
		return mySwingWorker.getPropertyChangeSupport();
	}

	public void removePropertyChangeListerFromSwingWorker( PropertyChangeListener listener )
	{
		mySwingWorker.removePropertyChangeListener( listener );
	}

	// WORKFLOW
	public boolean cancelSwingWorker( boolean mayInterruptIfRunning )
	{
		return mySwingWorker.cancel( mayInterruptIfRunning );
	}

	public void executeSwingWorker()
	{
		mySwingWorker.execute();
	}

	public void runSwingWorker()
	{
		mySwingWorker.run();
	}

	public int getSwingWorkerProgress()
	{
		return mySwingWorker.getProgress();
	}

	public SwingWorker.StateValue getSwingWorkerState()
	{
		// public enum StateValue {
		// /**
		// * Initial {@code SwingWorker} state.
		// */
		// PENDING,
		// /**
		// * {@code SwingWorker} is {@code STARTED}
		// * before invoking {@code doInBackground}.
		// */
		// STARTED,
		//
		// /**
		// * {@code SwingWorker} is {@code DONE}
		// * after {@code doInBackground} method
		// * is finished.
		// */
		// DONE
		// }
		return mySwingWorker.getState();
	}

	public boolean isSwingWorkerCancelled()
	{
		return mySwingWorker.isCancelled();
	}

	public boolean isSwingWorkerDone()
	{
		return mySwingWorker.isDone();
	}

	// GET RESULT
	public String getBlockingFromSwingWorker() throws ExecutionException, InterruptedException
	{
		return mySwingWorker.get();
	}

	public String getBlockingFromSwingWorker( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		return mySwingWorker.get( timeout, unit );
	}
}
