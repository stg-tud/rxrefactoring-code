package rxswingworker;

import javax.swing.*;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;

/**
 * Description: This class should be use to create an observable.
 * The class interacts with {@link SWSubscriber} by using a
 * thread safe data transfer object {@link SWDto}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public abstract class OnSubscribeFromSwingWorker<ReturnType, ProcessType> implements Observable.OnSubscribe<SWDto<ReturnType, ProcessType>>, Producer
{
	private Subscriber<? super SWDto<ReturnType, ProcessType>> observer;
	private SWDto<ReturnType, ProcessType> dto;
	private long requestCount;

	/**
	 * Manages the workflow of a SwingWorker by setting up the data
	 * transfer object {@link SWDto <ReturnType, ProcessType>}
	 * that is used for sending progress, chunks of data and finally the async result
	 * to the observer.
	 * 
	 * @param observer
	 *            observer
	 */
	@Override
	public final void call( Subscriber<? super SWDto<ReturnType, ProcessType>> observer )
	{
		this.observer = observer;
		try
		{
			if ( !this.observer.isUnsubscribed() )
			{
				observer.setProducer( this );
				this.dto = new SWDto<ReturnType, ProcessType>();
				this.observer.onStart();
				ReturnType asyncResult = doInBackground();
				this.observer.onNext( this.dto.setResult( asyncResult ) );
				this.observer.onCompleted();
			}
		}
		catch ( Exception throwable )
		{
			this.observer.onError( throwable );
		}
	}

	/**
	 * The maximum number of items that are produced can be for example influenced by
	 * calling the method {@link Observable#take(int)} on the observable.
	 * {@link this#requestCount} is being used to store the number of items being requested
	 * so that only the methods {@link this#publish(Object[])} and {@link this#setProgress(int)}
	 * are ignore in case that n is smaller than the number total number of items.<br>
	 * {@link this#requestCount} is used to guarantee that the
	 * final {@link SWSubscriber#onNext(SWDto)} is always called
	 * 
	 * @param n
	 *            maximum number of items
	 */
	@Override
	public void request( long n )
	{
		requestCount = n;
	}

	/**
	 * To be implemented by subclasses. This method corresponds to
	 * the {@link SwingWorker#doInBackground()} method.
	 * 
	 * @return the result of the asynchronous operation
	 * @throws Exception
	 *             a general exception that could be thrown
	 */
	protected abstract ReturnType doInBackground() throws Exception;

	/**
	 * Sends chunks of data to the observer ({@link SWSubscriber}).
	 * 
	 * @param chunks
	 *            the data to be send
	 */
	protected void publish( ProcessType... chunks )
	{
		// To make sure that the last emission is always the one that sets
		// the result of the asyn operation
		if ( requestCount > 1 )
		{
			requestCount--;
			this.observer.onNext( this.dto.send( chunks ) );
		}
	}

	/**
	 * Sends the progress to the observer ({@link SWSubscriber}.
	 * 
	 * @param progress
	 *            progress to be sent
	 */
	protected void setProgress( int progress )
	{
		// To make sure that the last emission is always the one that sets
		// the result of the asyn operation
		if ( requestCount > 1 )
		{
			requestCount--;
			this.observer.onNext( this.dto.setProgress( progress ) );
		}
	}
}
