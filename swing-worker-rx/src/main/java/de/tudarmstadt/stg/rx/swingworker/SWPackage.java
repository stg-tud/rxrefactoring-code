package de.tudarmstadt.stg.rx.swingworker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description: Data transfer object to allow the interaction
 * between {@link SWEmitter} and {@link SWSubscriber}
 * This class is public so that it can be used when declaring observables.
 * However all methods are only package visible because the are only supposed
 * to be used by {@link SWEmitter} and {@link SWSubscriber}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public final class SWPackage<ReturnType, ProcessType>
{
	private static final int DEFAULT_PROGRESS = -1;
	private ReturnType asyncResult;
	private List<ProcessType> chunks;
	private AtomicInteger progress;
	private ReentrantLock processingLock;
	private Object asyncResultLock;

	SWPackage(ReentrantLock processingLock )
	{
		this.progress = new AtomicInteger( DEFAULT_PROGRESS );
		this.chunks = new ArrayList<ProcessType>();
		this.asyncResult = null;
		this.processingLock = processingLock;
		this.asyncResultLock = new Object();
	}

	/**
	 * Updates the chunk
	 * 
	 * @param chunks
	 *            chunks of data to be added
	 * @return
	 */
	SWPackage<ReturnType, ProcessType> setChunks(ProcessType... chunks )
	{
		lockChunks();
		try
		{
			this.chunks.addAll( Arrays.asList( chunks ) );
			return this;
		}
		finally
		{
			unlockChunks();
		}
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
	SWPackage<ReturnType, ProcessType> setResult(ReturnType asyncResult )
	{
		synchronized ( asyncResultLock )
		{
			this.asyncResult = asyncResult;
		}
		return this;
	}

	/**
	 * Getter to retrieve the chunks in {@link SWSubscriber#onNext(SWPackage)}
	 * 
	 * @return the chunks as list
	 */
	public List<ProcessType> getChunks()
	{
		lockChunks();
		try
		{
			List<ProcessType> chunksCloned = new ArrayList<ProcessType>();
			chunksCloned.addAll( chunks );
			return chunksCloned;
		}
		finally
		{
			unlockChunks();
		}
	}

	/**
	 * Retrieves the async result from the channel.
	 * See {@link SWSubscriber#onNext(SWPackage)}
	 * 
	 * @return
	 */
	public ReturnType getResult()
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
	SWPackage<ReturnType, ProcessType> setProgress(int progress )
	{
		this.progress.set( progress );
		return this;
	}

	/**
	 * To identify whether a progress value was sent and processed.
	 * See {@link SWSubscriber#onNext(SWPackage)}
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
	 * See {@link SWSubscriber#onNext(SWPackage)}
	 * 
	 * @return
	 */
	int getProgressAndReset()
	{
		int progress = this.progress.get();
		this.progress.set( DEFAULT_PROGRESS ); // reset progress after each get
		return progress;
	}

	public ReentrantLock getProcessingLock()
	{
		return processingLock;
	}

	private void lockChunks()
	{
		processingLock.lock();
	}

	private void unlockChunks()
	{
		processingLock.unlock();
	}
}
