package rxswingworker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;

/**
 * Description: This class replaces the {@link SwingWorker}. All methods
 * from {@link SwingWorker are available}<br>
 * The class behaves like the {@link SwingWorker}. See the JavaDocs of each
 * method for more information.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public abstract class SwingWorkerSubscriber<ResultType, ProcessType>
		extends Subscriber<SwingWorkerSubscriberDto<ResultType, ProcessType>>
		implements RxSwingWorkerAPI<ResultType>
{

	private Observable<SwingWorkerSubscriberDto<ResultType, ProcessType>> observable;
	private Subscription subscription;
	private ResultType asyncResult;
	private PropertyChangeSupport propertyChangeSupport;
	private AtomicInteger progress;
	private AtomicBoolean cancelled;
	private AtomicBoolean done;
	private SwingWorker.StateValue currentState;
	private CountDownLatch countDownLatch;

	/**
	 * Constructor: each observer has a reference to the observable object
	 *
	 * @param observable
	 */
	public SwingWorkerSubscriber( Observable<SwingWorkerSubscriberDto<ResultType, ProcessType>> observable )
	{
		this.observable = observable;
		this.propertyChangeSupport = new PropertyChangeSupport( this );
		this.currentState = SwingWorker.StateValue.PENDING;
		initializeStates();
	}

	/**
	 * Initializes the state of the asynchronous task every time
	 * that this observer is subscribed.
	 * <ol>
	 * <li>progress = 0</li>
	 * <li>cancelled = false</li>
	 * <li>done = false</li>
	 * <li>state = {@link SwingWorker.StateValue#STARTED}</li>
	 * </ol>
	 * <p>
	 * The {@link CountDownLatch}
	 * is initially set to one. When {@link this#onCompleted()} is
	 * invoked the {@link CountDownLatch} is set to 0 to indicate
	 * that a result is present. This is important for {@link this#get()}
	 * and {@link this#get(long, TimeUnit)}
	 */
	@Override
	public void onStart()
	{
		initializeStates();
		this.countDownLatch = new CountDownLatch( 1 );
		setState( SwingWorker.StateValue.STARTED );
	}

	/**
	 * Used to process that items after a
	 * {@link SwingWorker#publish(Object[])} has been invoked. This
	 * class is not supposed to be overridden by subclasses. Therefore
	 * it was set as {@link Deprecated}. Use {@link this#process(List)}
	 * to process the items sent after by
	 * {@link SwingWorkerRxOnSubscribe#publish(Object[])}
	 *
	 * @param dto
	 *            Data transfer object for chunks and asyncResult
	 */
	@Override
	@Deprecated
	public void onNext( SwingWorkerSubscriberDto<ResultType, ProcessType> dto )
	{
		asyncResult = dto.getResult();
		if ( dto.isProgressValueAvailable() )
		{
			setProgress( dto.getProgressAndReset() );
		}
		List<ProcessType> processedChunks = dto.getChunks();
		process( processedChunks );
		dto.removeChunks( processedChunks );
	}

	/**
	 * Updates the {@link CountDownLatch} setting it to 0. Calls
	 * {@link this#done(Object)} and set the state to
	 * {@link SwingWorker.StateValue#DONE}. This class should be
	 * overridden by subclasses. Use {@link this#done(Object)} instead
	 */
	@Override
	@Deprecated
	public void onCompleted()
	{
		countDownLatch.countDown();
		done( asyncResult );
		setState( SwingWorker.StateValue.DONE );

	}

	/**
	 * Executes the asynchronous task in {@link Schedulers#computation()}
	 * as long as the {@link this#observable} is not subscribed. Otherwise the
	 * invocation to this method is ignored. The operation is
	 * observed in {@link SwingScheduler#getInstance()}
	 */
	@Override
	public void execute()
	{
		if ( !isSubscribed() )
		{
			Scheduler scheduler = Schedulers.computation();
			subscribeObservable( scheduler );
		}
	}

	/**
	 * Executes the asynchronous task in {@link Schedulers#immediate()}
	 * (The current thread) as long as the {@link this#observable}
	 * is not subscribed. Otherwise the invocation to this method is ignored.
	 * The operation is observed in {@link SwingScheduler#getInstance()}
	 */
	@Override
	public void run()
	{
		if ( !isSubscribed() )
		{
			Scheduler scheduler = Schedulers.immediate();
			subscribeObservable( scheduler );
		}
	}

	/**
	 * Waits from the result of {@link SwingWorkerRxOnSubscribe#doInBackground()}
	 *
	 * @return return value of {@link SwingWorkerRxOnSubscribe#doInBackground()}
	 * @throws InterruptedException
	 *             if the thread is interrupted while waiting
	 */
	@Override
	public ResultType get() throws InterruptedException
	{
		countDownLatch.await();
		return asyncResult;
	}

	/**
	 * Waits from the result of {@link SwingWorkerRxOnSubscribe#doInBackground()}
	 * given a timeout and a unit
	 *
	 * @param timeout
	 *            timeout
	 * @param unit
	 *            time unit
	 * @return return value of {@link SwingWorkerRxOnSubscribe#doInBackground()}
	 * @throws InterruptedException
	 *             if the thread is interrupted while waiting
	 */
	@Override
	public ResultType get( long timeout, TimeUnit unit ) throws InterruptedException
	{
		countDownLatch.await( timeout, unit );
		boolean timeExpired = countDownLatch.getCount() > 0;
		if ( timeExpired )
		{
			String message = this.getClass().getSimpleName() + ": Timeout exception. Result not present.";
			TimeoutException timeoutException = new TimeoutException( message );
			this.onError( timeoutException );
			return null;
		}
		return asyncResult;
	}

	/**
	 * @return state of this observer
	 */
	@Override
	public SwingWorker.StateValue getState()
	{
		return this.currentState;
	}

	/**
	 * 
	 * @return true if {@link this#done(Object)} has successfully been
	 *         executed. False otherwise.
	 */
	@Override
	public boolean isDone()
	{
		return this.done.get();
	}

	/**
	 * 
	 * @return true if this observer has been cancel. False otherwise
	 */
	@Override
	public boolean isCancelled()
	{
		return this.cancelled.get();
	}

	/**
	 * unsubscribes this observer if it is subscribed and {@param mayInterruptIfRunning}
	 * is true. The method returns false if this observer has already been cancelled, or
	 * if the tasks has already completed normally.
	 * 
	 * @param mayInterruptIfRunning
	 *            to indicate whether a running observer should be
	 *            unsubscribed or not
	 * @return true if this observer was successfully unsubscribed, false otherwise
	 */
	@Override
	public boolean cancel( boolean mayInterruptIfRunning )
	{
		if ( cancelled.get() || currentState.equals( SwingWorker.StateValue.DONE ) )
		{
			return false;
		}
		else if ( !this.isUnsubscribed() && mayInterruptIfRunning )
		{
			this.unsubscribe();
			this.cancelled.set( true );
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 *
	 * @return returns the current progress of the task. This progress
	 *         must be set by {@link this#setProgress(int)}
	 */
	@Override
	public int getProgress()
	{
		return this.progress.get();
	}

	/**
	 * Adds a property change listener to this observer
	 * 
	 * @param listener
	 *            listener to be added
	 */
	@Override
	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		synchronized ( this )
		{
			this.propertyChangeSupport.addPropertyChangeListener( listener );
		}
	}

	/**
	 * Removes a property change listener from this observer
	 * 
	 * @param listener
	 *            listener to be removed
	 */
	@Override
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		synchronized ( this )
		{
			this.propertyChangeSupport.removePropertyChangeListener( listener );
		}
	}

	/**
	 * Fires a proparty change
	 * 
	 * @param propertyName
	 *            name of the property
	 * @param oldValue
	 *            previous value of the property
	 * @param newValue
	 *            new value of the property
	 */
	@Override
	public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
	{
		synchronized ( this )
		{
			this.propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
		}
	}

	/**
	 *
	 * @return the {@link PropertyChangeListener} of this observer
	 */
	@Override
	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}

	// ### Protected Methods ###

	/**
	 * This method is invoked after {@link SwingWorkerRxOnSubscribe#doInBackground()} has completed.
	 * The method is invoked in {@link SwingScheduler#getInstance()}
	 * 
	 * @param asyncResult
	 *            result from {@link SwingWorkerRxOnSubscribe#doInBackground()}
	 */
	protected abstract void done( ResultType asyncResult );

	/**
	 * This method is invokated everytime {@link SwingWorkerRxOnSubscribe#publish(Object[])} is invoked.
	 * 
	 * @param chunks
	 *            arguments passed to {@link SwingWorkerRxOnSubscribe#publish(Object[])}
	 */
	protected abstract void process( List<ProcessType> chunks );

	/**
	 * This method updates the progress of the observer. It can only
	 * take values between 0 and 100.
	 * 
	 * @param progress
	 *            progress to be set
	 */
	protected void setProgress( int progress )
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

	// ### Private Methods ###

	private void initializeStates()
	{
		this.progress = new AtomicInteger( 0 );
		this.cancelled = new AtomicBoolean( false );
		this.done = new AtomicBoolean( false );
	}

	private boolean isSubscribed()
	{
		return subscription != null && !subscription.isUnsubscribed();
	}

	private void subscribeObservable( Scheduler scheduler )
	{
		this.subscription = this.observable
				.observeOn( SwingScheduler.getInstance() )
				.subscribeOn( scheduler )
				.subscribe( this );
	}

	private void setState( SwingWorker.StateValue state )
	{
		synchronized ( this )
		{
			SwingWorker.StateValue oldState = this.currentState;
			propertyChangeSupport.firePropertyChange( "state", oldState, state );
			this.currentState = state;
		}
	}
}
