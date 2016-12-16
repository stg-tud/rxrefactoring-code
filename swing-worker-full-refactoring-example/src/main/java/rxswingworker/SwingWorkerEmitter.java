package rxswingworker;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

import rx.Emitter;
import rx.functions.Action1;

/**
 * Description: This class should be use to create an observable.
 * The class interacts with {@link SwingWorkerSubscriber} by using a
 * thread safe data transfer object {@link SwingWorkerDto}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public abstract class SwingWorkerEmitter<ReturnType, ProcessType> implements Action1<Emitter<SwingWorkerDto<ReturnType, ProcessType>>>
{
	private Emitter<? super SwingWorkerDto<ReturnType, ProcessType>> emitter;
	private SwingWorkerDto<ReturnType, ProcessType> dto;
	private AtomicBoolean running = new AtomicBoolean( false );

	/**
	 * Manages the workflow of a SwingWorker by setting up the data
	 * transfer object {@link SwingWorkerDto <ReturnType, ProcessType>}
	 * that is used for sending progress, chunks of data and finally the async result
	 * to the emitter.
	 * 
	 * @param emitter
	 *            emitter
	 */
	@Override
	public final void call( Emitter<SwingWorkerDto<ReturnType, ProcessType>> emitter )
	{

		if ( running.get() )
		{
			return;
		}
		running.set( true );

		this.emitter = emitter;
		try
		{
			this.dto = new SwingWorkerDto<ReturnType, ProcessType>();
			ReturnType asyncResult = doInBackground();
			this.emitter.onNext( this.dto.setResult( asyncResult ) );
			this.emitter.onCompleted();
		}
		catch ( Exception throwable )
		{
			this.emitter.onError( throwable );
		}
		finally
		{
			running.set( false );
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
	 * Sends chunks of data to the emitter ({@link SwingWorkerSubscriber}).
	 * 
	 * @param chunks
	 *            the data to be send
	 */
	protected void publish( ProcessType... chunks )
	{
		this.emitter.onNext( this.dto.send( chunks ) );
	}

	/**
	 * Sends the progress to the emitter ({@link SwingWorkerSubscriber}.
	 * 
	 * @param progress
	 *            progress to be sent
	 */
	protected void setProgress( int progress )
	{
		this.emitter.onNext( this.dto.setProgress( progress ) );
	}
}
