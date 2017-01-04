package de.tudarmstadt.stg.rx.swingworker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import rx.Subscriber;

/**
 * Description: Data transfer object to allow the interaction
 * between {@link SWEmitter} and {@link SWSubscriber}
 * This class is public so that it can be used when declaring observables.
 * However all methods are only package visible because the are only supposed
 * to be used by {@link SWEmitter} and {@link SWSubscriber}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public final class SWDto<ReturnType, ProcessType>
{
	private static final int DEFAULT_PROGRESS = -1;
	private ReturnType asyncResult;
	private List<ProcessType> chunks;
	private AtomicInteger progress;
	private ReentrantLock processingLock;
	private Object asyncResultLock;
	private AtomicBoolean handshake;

	SWDto()
	{
		this.progress = new AtomicInteger( DEFAULT_PROGRESS );
		this.chunks = new ArrayList<ProcessType>();
		this.asyncResult = null;
		this.processingLock = new ReentrantLock();
		asyncResultLock = new Object();
	}

	/**
	 * Updates the chunk
	 * 
	 * @param chunks
	 *            chunks of data to be added
	 * @return
	 */
	SWDto<ReturnType, ProcessType> send( ProcessType... chunks )
	{
		synchronized ( this.chunks )
		{
			this.chunks.addAll( Arrays.asList( chunks ) );
		}
		return this;
	}

	/**
	 * Sets a result for the asynchronous operation
	 * 
	 * @param asyncResult
	 *            result to be set
	 * @return this object so that it can be used in {@link rx.Subscriber#onNext(Object)} }.
	 *         See {@link SWEmitter#setProgress(int)}
	 *         and {@link SWEmitter#publish(Object[])}
	 */
	SWDto<ReturnType, ProcessType> setResult( ReturnType asyncResult )
	{
		synchronized ( asyncResultLock )
		{
			this.asyncResult = asyncResult;
		}
		return this;
	}

	/**
	 * Getter to retrieve the chunks in {@link SWSubscriber#onNext(SWDto)}
	 * 
	 * @return the chunks as list
	 */
	List<ProcessType> getChunks()
	{
		synchronized ( this.chunks )
		{
			List<ProcessType> chunksCloned = new ArrayList<ProcessType>();
			chunksCloned.addAll( chunks );
			return chunksCloned;
		}
	}

	/**
	 * To remove chunks after they have been processed.
	 * See {@link SWSubscriber#onNext(SWDto)}
	 * 
	 * @param chunks
	 */
	void removeChunks( List<ProcessType> chunks )
	{
		synchronized ( this.chunks )
		{
			this.chunks.removeAll( chunks );
		}
	}

	/**
	 * Retrieves the async result cached in this Dto.
	 * See {@link SWSubscriber#onNext(SWDto)}
	 * 
	 * @return
	 */
	ReturnType getResult()
	{
		synchronized ( asyncResultLock )
		{
			return this.asyncResult;
		}
	}

	/**
	 * To send the progress from {@link SWEmitter} to
	 * {@link SWSubscriber}.
	 * See {@link SWEmitter#setProgress(int)}
	 * 
	 * @param progress
	 * @return
	 */
	SWDto<ReturnType, ProcessType> setProgress( int progress )
	{
		this.progress.set( progress );
		return this;
	}

	/**
	 * To identify whether a progress value was sent and processed.
	 * See {@link SWSubscriber#onNext(SWDto)}
	 * 
	 * @return
	 */
	boolean isProgressValueAvailable()
	{
		return progress.get() != DEFAULT_PROGRESS;
	}

	/**
	 * To get the progress for processing and reset its value so it is
	 * not processed multiple times.
	 * See {@link SWSubscriber#onNext(SWDto)}
	 * 
	 * @return
	 */
	int getProgressAndReset()
	{
		int progress = this.progress.get();
		this.progress.set( DEFAULT_PROGRESS ); // reset progress after each get
		return progress;
	}

	void lockChunks()
	{
		processingLock.lock();
	}

	void unlockChunks()
	{
		processingLock.unlock();
	}

	boolean getHandshake()
	{
		return handshake.get();
	}

	SWDto<ReturnType, ProcessType> setHandshake(boolean handshake)
	{
		this.handshake.set(handshake);
		return this;
	}
}
