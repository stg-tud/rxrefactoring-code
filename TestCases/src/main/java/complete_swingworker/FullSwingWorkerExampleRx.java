package complete_swingworker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import rx.subjects.BehaviorSubject;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/30/2016
 */
public class FullSwingWorkerExampleRx
{
	private StatefulRxObservable statefulRxObservable;

	public FullSwingWorkerExampleRx( long timeout, int amountOfWork )
	{
		statefulRxObservable = new StatefulRxObservable( timeout, amountOfWork );
	}

	public class StatefulRxObservable
	{
		// Default fields
		public static final int STATE_PENDING = 0;
		public static final int STATE_STARTED = 1;
		public static final int STATE_DONE = 2;

		private PropertyChangeSupport propertyChangeSupport;
		private AtomicInteger progress;
		private AtomicBoolean cancelled;
		private AtomicBoolean done;
		private AtomicInteger currentState;
		private Observable<String> observable;
		private Subscription subscription;

		// dynamic fields
		private static final String SEPARATOR = " - ";

		// variables that are declared outside of the SwingWorker should be passed in the constructor
		private long timeout;
		private int amountOfWork;

		public StatefulRxObservable( long timeout, int amountOfWork )
		{
			this.timeout = timeout;
			this.amountOfWork = amountOfWork;
			this.currentState = new AtomicInteger( STATE_PENDING );
			this.progress = new AtomicInteger( 0 );
			this.done = new AtomicBoolean();
			this.cancelled = new AtomicBoolean();
			this.propertyChangeSupport = new PropertyChangeSupport( this );
			initializeAsyncObservable();
		}

		public Observable<String> getAsyncObservable()
		{
			return observable;
		}

		public int getState()
		{
			return this.currentState.get();
		}

		public boolean isDone()
		{
			return this.done.get();
		}

		public boolean isCancelled()
		{
			return this.cancelled.get();
		}

		public int getProgress()
		{
			return this.progress.get();
		}

		public Subscription subscribe()
		{
			this.subscription = this.observable.subscribe();
			return this.subscription;
		}

		public Subscription subscribeOnCurrentThread()
		{
			subscription = this.observable.subscribeOn( Schedulers.immediate() ).subscribe();
			return this.subscription;
		}

		public boolean cancel( boolean mayInterruptIfRunning )
		{
			if ( this.subscription != null && mayInterruptIfRunning )
			{
				this.subscription.unsubscribe();
				this.cancelled.set( true );
				return true;
			}
			else
			{
				return false;
			}
		}

		public String getResult()
		{
			if ( subscription != null )
			{
				subscription.unsubscribe();
			}
			return this.observable.toBlocking().single();
		}

		public String getResult( long timeout, TimeUnit unit )
		{
			if ( subscription != null )
			{
				subscription.unsubscribe();
			}
			return this.observable.timeout( timeout, unit ).toBlocking().single();
		}

		public void addPropertyChangeListener( PropertyChangeListener listener )
		{
			this.propertyChangeSupport.addPropertyChangeListener( listener );
		}

		public void removePropertyChangeListener( PropertyChangeListener listener )
		{
			this.propertyChangeSupport.removePropertyChangeListener( listener );
		}

		public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
		{
			this.propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
		}

		public PropertyChangeSupport getPropertyChangeSupport()
		{
			return propertyChangeSupport;
		}

		private void setProgress( int progress )
		{
			if ( progress < 0 || progress > 100 )
			{
				throw new IllegalArgumentException( "the value should be from 0 to 100" );
			}
			if ( this.progress.equals( progress ) )
			{
				return;
			}
			synchronized ( this )
			{
				int oldProgress = this.progress.get();
				propertyChangeSupport.firePropertyChange( "progress", oldProgress, progress );
				this.progress.set( progress );
			}
		}

		private void setState( int state )
		{
			synchronized ( this )
			{
				int oldState = this.currentState.get();
				propertyChangeSupport.firePropertyChange( "state", oldState, state );
				this.currentState.set( state );
			}
		}

		private Observable<String> initializeAsyncObservable()
		{
			this.currentState.set( STATE_PENDING );
			this.progress.set( 0 );
			this.done.set( false );
			this.cancelled.set( false );
			this.done.set( false );

			setState( STATE_PENDING );
			BehaviorSubject<List<Integer>> publishSubject = BehaviorSubject.create( new ArrayList<>() );

			publishSubject.observeOn(SwingScheduler.getInstance())
					.doOnNext(new Action1<List<Integer>>()
					{
						@Override
						public void call(List<Integer> chunks)
						{
							for ( Integer number : chunks )
							{
								printMessage( "Processing " + number );
								setProgress( number * 100 / ( amountOfWork * 2 ) );
							}
						}
					}).subscribe();

			observable = Observable
					.fromCallable( new Callable<String>()
					{
						@Override
						public String call() throws Exception
						{
							printMessage( "Entering doInBackground() method" );
							for ( int i = 0; i < amountOfWork * 2; i = i + 2 )
							{
								Thread.sleep( 2000L );
								publishSubject.onNext( Arrays.asList( i, i + 1 ) );
							}
							printMessage( "doInBackground() finished successfully" );
							return "Async Result";
						}
					} )
					.observeOn( SwingScheduler.getInstance() )
					.subscribeOn( Schedulers.computation() )
					.doOnSubscribe( () -> setState( STATE_STARTED ) )
					.doOnNext( new Action1<String>()
					{
						@Override
						public void call( String asyncResult )
						{
							setState( STATE_DONE );
							printMessage( "Entering done() method" );
							String result = asyncResult;
							printMessage( "doInBackground() result = " + result );
						}
					} )
					.timeout( timeout, TimeUnit.SECONDS )
					.onErrorResumeNext( new Func1<Throwable, Observable<? extends String>>()
					{
						@Override
						public Observable<? extends String> call( Throwable throwable )
						{
							// The catch-clause block of the removed try-catch block is copied here
							printMessage( "TimeoutException" );
							Exceptions.propagate( throwable );
							return Observable.empty();
						}
					} )
					.doOnCompleted( () -> done.set( true ) );
			return observable;
		}

		private void printMessage( String message )
		{
			System.out.println( "[" + Thread.currentThread().getName() + "]" + SEPARATOR + message );
		}
	}

	// PROPERTIES: default "state", "progress"
	public void addPropertyChangeListenerToSwingWorker( PropertyChangeListener listener )
	{
		statefulRxObservable.addPropertyChangeListener( listener );
	}

	public void firePropertyChangeInSwingWorker( String propertyName, Object oldValue, Object newValue )
	{
		statefulRxObservable.firePropertyChange( propertyName, oldValue, newValue );
	}

	public PropertyChangeSupport getSwingWorkerPropertyChangeSupport()
	{
		return statefulRxObservable.getPropertyChangeSupport();
	}

	public void removePropertyChangeListerFromSwingWorker( PropertyChangeListener listener )
	{
		statefulRxObservable.removePropertyChangeListener( listener );
	}

	// WORKFLOW
	public boolean cancelSwingWorker( boolean mayInterruptIfRunning )
	{
		return statefulRxObservable.cancel( mayInterruptIfRunning );
	}

	public void executeSwingWorker()
	{
		statefulRxObservable.subscribe();
	}

	public void runSwingWorker()
	{
		statefulRxObservable.subscribeOnCurrentThread();
	}

	public int getSwingWorkerProgress()
	{
		return statefulRxObservable.getProgress();
	}

	public int getSwingWorkerState()
	{
		return statefulRxObservable.getState();
	}

	public boolean isSwingWorkerCancelled()
	{
		return statefulRxObservable.isCancelled();
	}

	public boolean isSwingWorkerDone()
	{
		return statefulRxObservable.isDone();
	}

	// GET RESULT
	public String getBlockingFromSwingWorker() throws ExecutionException, InterruptedException
	{
		return statefulRxObservable.getResult();
	}

	public String getBlockingFromSwingWorker( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		return statefulRxObservable.getResult( timeout, unit );
	}
}
