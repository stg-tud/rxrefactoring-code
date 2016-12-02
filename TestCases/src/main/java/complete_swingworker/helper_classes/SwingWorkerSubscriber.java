package complete_swingworker.helper_classes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public abstract class SwingWorkerSubscriber<ResultType, ProcessType> extends Subscriber<SwingWorkerSubscriberDto<ResultType, ProcessType>>
{
	public enum State
	{
		PENDING, STARTED, DONE
	}

	private PropertyChangeSupport propertyChangeSupport;
	private AtomicInteger progress;
	private AtomicBoolean cancelled;
	private AtomicBoolean done;
	private State currentState;
	private ResultType asyncResult;

	public SwingWorkerSubscriber()
	{
		this.propertyChangeSupport = new PropertyChangeSupport( this );
		this.progress = new AtomicInteger( 0 );
		this.cancelled = new AtomicBoolean( false );
		this.done = new AtomicBoolean();
		this.currentState = State.PENDING;
	}

	protected abstract void done( ResultType asyncResult );

	protected abstract void process( List<ProcessType> dto );

	@Override
	public void onStart()
	{
		setState( State.STARTED );
	}

	@Override
	public void onNext( SwingWorkerSubscriberDto<ResultType, ProcessType> dto )
	{
		asyncResult = dto.getResult();
		List<ProcessType> processedChunks = dto.getChunks();
		process( processedChunks );
		dto.removeChunks( processedChunks );
	}

	@Override
	public void onCompleted()
	{
		done( asyncResult );
		setState( State.DONE );
	}

	public ResultType getResult(Observable<SwingWorkerSubscriberDto<ResultType, ProcessType>> observable)
	{
		observable.toBlocking().subscribe(this);
		return asyncResult;
	}

	public ResultType getResult(Observable<SwingWorkerSubscriberDto<ResultType, ProcessType>> observable, long timeout, TimeUnit unit)
	{
		observable.timeout( timeout, unit ).toBlocking().subscribe(this);
		return asyncResult;
	}

	public State getState()
	{
		return this.currentState;
	}

	public boolean isDone()
	{
		return this.done.get();
	}

	public void setDone( boolean done )
	{
		this.done.set( done );
	}

	public boolean isCancelled()
	{
		return this.cancelled.get();
	}

	public void cancel()
	{
		this.cancelled.set( true );
	}

	public int getProgress()
	{
		return this.progress.get();
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		synchronized ( this )
		{
			this.propertyChangeSupport.addPropertyChangeListener( listener );
		}
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		synchronized ( this )
		{
			this.propertyChangeSupport.removePropertyChangeListener( listener );
		}
	}

	public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
	{
		synchronized ( this )
		{
			this.propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
		}
	}

	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}

	public void setProgress( int progress )
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

	public void setState( State state )
	{
		synchronized ( this )
		{
			State oldState = this.currentState;
			propertyChangeSupport.firePropertyChange( "state", oldState, state );
			this.currentState = state;
		}
	}
}
