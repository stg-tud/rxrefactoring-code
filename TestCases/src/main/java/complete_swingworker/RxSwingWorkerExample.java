package complete_swingworker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import complete_swingworker.helper_classes.SwingWorkerRxOnSubscribe;
import complete_swingworker.helper_classes.SwingWorkerSubscriber;
import complete_swingworker.helper_classes.SwingWorkerSubscriberDto;
import rx.Observable;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public class RxSwingWorkerExample
{
	private SwingWorkerSubscriber<String, Integer> observer;

	public RxSwingWorkerExample( long timeout, int amountOfWork )
	{
		Observable<SwingWorkerSubscriberDto<String, Integer>> observable = Observable.create( new SwingWorkerRxOnSubscribe<String, Integer>()
		{
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

		} );

		observer = new SwingWorkerSubscriber<String, Integer>( observable )
		{
            @Override
			protected void process( List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					printMessage( "Processing " + number );
					setProgress( number * 100 / ( amountOfWork * 2 ) );
				}
			}

			@Override
			protected void done( String asyncResult )
			{
				printMessage( "Entering done() method" );
				String result = asyncResult;
				printMessage( "doInBackground() result = " + result );
			}

            @Override
            public void onError(Throwable throwable)
            {
                throwable.printStackTrace();
            }
        };
	}

	private void printMessage( String message )
	{
		System.out.println( "[" + Thread.currentThread().getName() + "]" + " - " + message );
	}

	// PROPERTIES: default "state", "progress"
	public void addPropertyChangeListenerToSwingWorker( PropertyChangeListener listener )
	{
		observer.addPropertyChangeListener( listener );
	}

	public void firePropertyChangeInSwingWorker( String propertyName, Object oldValue, Object newValue )
	{
		observer.firePropertyChange( propertyName, oldValue, newValue );
	}

	public PropertyChangeSupport getSwingWorkerPropertyChangeSupport()
	{
		return observer.getPropertyChangeSupport();
	}

	public void removePropertyChangeListerFromSwingWorker( PropertyChangeListener listener )
	{
		observer.removePropertyChangeListener( listener );
	}

	// WORKFLOW
	public boolean cancelSwingWorker( boolean mayInterruptIfRunning )
	{
		return observer.cancel( mayInterruptIfRunning );
	}

	public void executeSwingWorker() throws InterruptedException
    {
		this.observer.execute();
	}

	public void runSwingWorker()
	{
		this.observer.run();
	}

	public int getSwingWorkerProgress()
	{
		return observer.getProgress();
	}

	public SwingWorkerSubscriber.State getSwingWorkerState()
	{
		return observer.getState();
	}

	public boolean isSwingWorkerCancelled()
	{
		return observer.isCancelled();
	}

	public boolean isSwingWorkerDone()
	{
		return observer.isDone();
	}

	// GET RESULT
	public String getBlockingFromSwingWorker() throws ExecutionException, InterruptedException
	{
		return observer.get( );
	}

	public String getBlockingFromSwingWorker( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		return observer.get(timeout, unit );
	}
}
