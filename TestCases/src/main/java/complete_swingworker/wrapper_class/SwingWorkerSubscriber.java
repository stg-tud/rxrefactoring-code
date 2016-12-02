package complete_swingworker.wrapper_class;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Subscriber;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public abstract class SwingWorkerSubscriber<ResultType, ProcessType> extends Subscriber<SwingWorkerSubscriberDto<ResultType, ProcessType>>
{
	public static final int STATE_PENDING = 0;
	public static final int STATE_STARTED = 1;
	public static final int STATE_DONE = 2;

	private PropertyChangeSupport propertyChangeSupport;
	private AtomicInteger progress;
	private AtomicBoolean cancelled;
	private AtomicBoolean done;
	private AtomicInteger currentState;
	private ResultType asyncResult;

	public SwingWorkerSubscriber()
	{
		this.propertyChangeSupport = new PropertyChangeSupport( this );
		this.progress = new AtomicInteger( 0 );
		this.cancelled = new AtomicBoolean();
		this.done = new AtomicBoolean();
		this.currentState = new AtomicInteger( STATE_PENDING );
	}

	@Override
	public void onStart()
	{
		setState( STATE_STARTED );
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
		setState( STATE_DONE );
	}

	protected abstract void done( ResultType asyncResult );

	protected abstract void process( List<ProcessType> dto );

	public int getState()
	{
		return this.currentState.get();
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

	public void setCancelled( boolean cancelled )
	{
		this.cancelled.set( cancelled );
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

	public void setState( int state )
	{
		synchronized ( this )
		{
			int oldState = this.currentState.get();
			propertyChangeSupport.firePropertyChange( "state", oldState, state );
			this.currentState.set( state );
		}
	}
}
