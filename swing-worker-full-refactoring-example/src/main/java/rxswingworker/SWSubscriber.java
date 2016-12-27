package rxswingworker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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
public abstract class SWSubscriber<ResultType, ProcessType>
		extends Subscriber<SWDto<ResultType, ProcessType>>
		implements RxSwingWorkerAPI<ResultType>
{

	private Observable<SWDto<ResultType, ProcessType>> observable;
	private Subscription subscription;
	private ResultType asyncResult;
	private PropertyChangeSupport propertyChangeSupport;
	private AtomicInteger progress;
	private AtomicBoolean cancelled;
	private SwingWorker.StateValue currentState;
	private CountDownLatch countDownLatch;

	/**
	 * Constructor: each observer has a reference to the observable object
	 *
	 * @param observable
	 */
	public SWSubscriber( Observable<SWDto<ResultType, ProcessType>> observable )
	{
		this.observable = observable;
		initialize();
	}

	public SWSubscriber()
	{
		initialize();
	}

	public void setObservable(Observable<SWDto<ResultType, ProcessType>> observable)
	{
		this.observable = observable;
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
	public final void onStart()
	{
		initialize();
		this.countDownLatch = new CountDownLatch( 1 );
		setState( SwingWorker.StateValue.STARTED );
	}

	/**
	 * Used to process that items after a
	 * {@link SwingWorker#publish(Object[])} has been invoked.
	 *
	 * @param dto
	 *            Data transfer object for chunks and asyncResult
	 */
	@Override
	public final void onNext( SWDto<ResultType, ProcessType> dto )
	{
		asyncResult = dto.getResult();
		if ( dto.isProgressValueAvailable() )
		{
			setProgress( dto.getProgressAndReset() );
		}

		dto.lockChunks();
		try
		{
			List<ProcessType> processedChunks = dto.getChunks();
			process( processedChunks );
			dto.removeChunks( processedChunks );
		}
		finally
		{
			dto.unlockChunks();
		}
	}

	/**
	 * Updates the {@link CountDownLatch} setting it to 0. Calls
	 * {@link this#done} and set the state to
	 * {@link SwingWorker.StateValue#DONE}.
	 */
	@Override
	public final void onCompleted()
	{
		countDownLatch.countDown();
		done( asyncResult );
		setState( SwingWorker.StateValue.DONE );

	}

	@Override
	public void onError(Throwable e)
	{

	}

	/**
	 * Executes the asynchronous task in {@link Schedulers#computation()}
	 * as long as the {@link this#observable} is not subscribed. Otherwise the
	 * invocation to this method is ignored. The operation is
	 * observed in {@link SwingScheduler#getInstance()}
	 */
	@Override
	public final void execute()
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
	public final void run()
	{
		if ( !isSubscribed() )
		{
			Scheduler scheduler = Schedulers.immediate();
			subscribeObservable( scheduler );
		}
	}

	/**
	 * Waits from the result of {@link SWEmitter#doInBackground()}
	 *
	 * @return return value of {@link SWEmitter#doInBackground()}
	 * @throws InterruptedException
	 *             if the thread is interrupted while waiting
	 */
	@Override
	public final ResultType get() throws InterruptedException, ExecutionException
	{
		countDownLatch.await();
		return asyncResult;
	}

	/**
	 * Waits from the result of {@link SWEmitter#doInBackground()}
	 * given a timeout and a unit
	 *
	 * @param timeout
	 *            timeout
	 * @param unit
	 *            time unit
	 * @return return value of {@link SWEmitter#doInBackground()}
	 * @throws InterruptedException
	 *             if the thread is interrupted while waiting
	 */
	@Override
	public final ResultType get( long timeout, TimeUnit unit ) throws InterruptedException, TimeoutException, ExecutionException
	{
		countDownLatch.await( timeout, unit );
		boolean timeExpired = countDownLatch.getCount() > 0;
		if ( timeExpired )
		{
			String message = this.getClass().getSimpleName() + ": Timeout exception. Result not present.";
			TimeoutException timeoutException = new TimeoutException( message );
			throw timeoutException;
		}
		return asyncResult;
	}

	/**
	 * @return state of this observer
	 */
	@Override
	public final SwingWorker.StateValue getState()
	{
		return this.currentState;
	}

	/**
	 *
	 * @return true if {@link this#done} has successfully been
	 *         executed. False otherwise.
	 */
	@Override
	public final boolean isDone()
	{
		return SwingWorker.StateValue.DONE.equals( currentState );
	}

	/**
	 *
	 * @return true if this observer has been cancel. False otherwise
	 */
	@Override
	public final boolean isCancelled()
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
	public final boolean cancel(boolean mayInterruptIfRunning )
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
	public final int getProgress()
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
	public final void addPropertyChangeListener( PropertyChangeListener listener )
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
	public final void removePropertyChangeListener( PropertyChangeListener listener )
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
	public final void firePropertyChange( String propertyName, Object oldValue, Object newValue )
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
	public final PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}

	// ### Protected Methods ###

	/**
	 * This method is invoked after {@link SWEmitter#doInBackground()} has completed.
	 * The method is invoked in {@link SwingScheduler#getInstance()}
	 *
	 */
	protected void done( ResultType asyncResult )
	{

	}

	/**
	 * This method is invoked every time {@link SWEmitter#publish(Object[])} is invoked.
	 *
	 * @param chunks
	 *            arguments passed to {@link SWEmitter#publish(Object[])}
	 */
	protected void process( List<ProcessType> chunks )
	{

	}

	/**
	 * This method updates the progress of the observer. It can only
	 * take values between 0 and 100.
	 *
	 * @param progress
	 *            progress to be set
	 */
	protected final void setProgress( int progress )
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

	private void initialize()
	{
		this.propertyChangeSupport = new PropertyChangeSupport( this );
		this.currentState = SwingWorker.StateValue.PENDING;
		this.progress = new AtomicInteger( 0 );
		this.cancelled = new AtomicBoolean( false );
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