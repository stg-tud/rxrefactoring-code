package rxswingworker;

import rx.Observable;
import rx.Subscriber;

import javax.swing.*;

/**
 * Description: This class should be use to create an observable.
 * The class interacts with {@link SwingWorkerSubscriber} by using a
 * thread safe data transfer object {@link SwingWorkerSubscriberDto}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public abstract class SwingWorkerRxOnSubscribe<ReturnType, ProcessType> implements Observable.OnSubscribe<SwingWorkerSubscriberDto<ReturnType, ProcessType>>
{
	private Subscriber<? super SwingWorkerSubscriberDto<ReturnType, ProcessType>> observer;
	private SwingWorkerSubscriberDto<ReturnType, ProcessType> dto;

	/**
	 * Manages the workflow of a SwingWorker by setting up the data
	 * transfer object {@link SwingWorkerSubscriberDto<ReturnType, ProcessType>}
	 * that is used for sending progress, chunks of data and finally the async result
	 * to the observer. This class should not be overridden by subclasses. Therefore
	 * is is marked as {@link Deprecated}
	 * 
	 * @param observer
	 *            observer
	 */
	@Override
	@Deprecated
	public void call( Subscriber<? super SwingWorkerSubscriberDto<ReturnType, ProcessType>> observer )
	{
		this.observer = observer;
		try
		{
			if ( !this.observer.isUnsubscribed() )
			{
				this.dto = new SwingWorkerSubscriberDto<ReturnType, ProcessType>();
				this.observer.onStart();
				ReturnType asyncResult = doInBackground();
				this.observer.onNext( dto.setResult( asyncResult ) );
				this.observer.onCompleted();
			}
		}
		catch ( Exception throwable )
		{
			observer.onError( throwable );
		}
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
	 * Sends chunks of data to the observer ({@link SwingWorkerSubscriber}).
	 * 
	 * @param chunks
	 *            the data to be send
	 */
	protected void publish( ProcessType... chunks )
	{
		this.observer.onNext( this.dto.send( chunks ) );
	}

	/**
	 * Sends the progress to the observer ({@link SwingWorkerSubscriber}.
	 * 
	 * @param progress
	 *            progress to be sent
	 */
	protected void setProgress( int progress )
	{
		this.observer.onNext( this.dto.setProgress( progress ) );
	}
}
