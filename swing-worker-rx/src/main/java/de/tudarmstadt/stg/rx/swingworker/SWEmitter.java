package de.tudarmstadt.stg.rx.swingworker;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;

import rx.Emitter;
import rx.functions.Action1;

/**
 * Description: This class should be use to create an observable.
 * The class interacts with {@link SWSubscriber} by using a
 * thread safe data transfer object {@link SWPackage}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public abstract class SWEmitter<ReturnType, ProcessType> implements Action1<Emitter<SWPackage<ReturnType, ProcessType>>>
{
	private Emitter<? super SWPackage<ReturnType, ProcessType>> emitter;
	private AtomicBoolean running = new AtomicBoolean( false );
	private ReentrantLock processingLock;

	/**
	 * Manages the workflow of a SwingWorker by setting up the data
	 * transfer object {@link SWPackage}{@literal <}ReturnType, ProcessType{@literal >}
	 * that is used for sending progress, chunks of data and finally the async result
	 * to the emitter.
	 * 
	 * @param emitter
	 *            emitter
	 */
	@Override
	public final void call( Emitter<SWPackage<ReturnType, ProcessType>> emitter )
	{

		if ( running.get() )
		{
			return;
		}
		running.set( true );

		this.emitter = emitter;
		try
		{
			this.processingLock = new ReentrantLock();
			this.emitter.onNext( createPackage() );
			ReturnType asyncResult = doInBackground();
			this.emitter.onNext( createPackage().setResult( asyncResult ) );
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
	 * Sends chunks of data to the emitter ({@link SWSubscriber}).
	 * 
	 * @param chunks
	 *            data chunks
	 */
	protected final void publish( ProcessType... chunks )
	{
		this.emitter.onNext( createPackage().setChunks( chunks ) );
	}

	/**
	 * Sends the progress to the emitter ({@link SWSubscriber}.
	 * 
	 * @param progress
	 *            progress
	 */
	protected final void setProgress( int progress )
	{
		this.emitter.onNext( createPackage().setProgress( progress ) );
	}

	private SWPackage<ReturnType, ProcessType> createPackage()
	{
		return new SWPackage<ReturnType, ProcessType>( processingLock );
	}
}
