package rxrefactoring;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWDto;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;

/**
 * Description: Basic class for test purposes <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class GeneralCase
{
	private static final int AMOUNT_OF_WORK = 10;
	private static final long TIME_FOR_WORK_UNIT = 2000L;

	private SWSubscriber<String, Integer> rxObserver;

	public void someMethod()
	{
		rx.Observable<SWDto<String, Integer>> rxObservable = rx.Observable.fromEmitter(new SWEmitter<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				System.out.println( "Entering doInBackground() method" );
				for ( int i = 0; i < AMOUNT_OF_WORK * 2; i = i + 2 )
				{
					publish( i, i + 1 );
					Thread.sleep( TIME_FOR_WORK_UNIT );
				}
				System.out.println( "doInBackground() finished successfully" );
				return "Async Result";
			}
		}, Emitter.BackpressureMode.BUFFER );

		rxObserver = new SWSubscriber<String, Integer>( rxObservable )
		{
			@Override
			protected void process( List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					System.out.println( "Processing " + number );
					setProgress( number * 100 / ( AMOUNT_OF_WORK * 2 ) );
				}
			}

			@Override
			protected void done( )
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

		SWSubscriber<String, Integer> rxObserverRef = rxObserver;
		SWSubscriber rxObserverRef2 = rxObserver;

		doSomething(rxObserverRef);

		rx.Observable<SWDto<String, Integer>> rxObservable1 = rx.Observable.fromEmitter(new SWEmitter<String, Integer>()
		{

			@Override
			protected String doInBackground() throws Exception
			{
				return null;
			}
		}, Emitter.BackpressureMode.BUFFER);

		new SWSubscriber<String, Integer>(rxObservable1){};

		rx.Observable<SWDto<String, Integer>> rxObservable2 = rx.Observable.fromEmitter(new SWEmitter<String, Integer>()
		{

			@Override
			protected String doInBackground() throws Exception
			{
				return null;
			}
		}, Emitter.BackpressureMode.BUFFER);

		new SWSubscriber<String, Integer>(rxObservable2){}.executeObservable();
	}

	private void doSomething(SWSubscriber<String, Integer> anotherRxObserver)
	{
		anotherRxObserver.executeObservable();
	}

	public void swingWorkerCalls()
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