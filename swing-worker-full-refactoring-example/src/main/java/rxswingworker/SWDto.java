package rxswingworker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Subscriber;

/**
 * Description: Data transfer object to allow the interaction
 * between {@link OnSubscribeFromSwingWorker} and {@link SWSubscriber}
 * This class is public so that it can be used when declaring observables.
 * However all methods are only package visible because the are only supposed
 * to be used by {@link OnSubscribeFromSwingWorker} and {@link SWSubscriber}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public final class SWDto<ReturnType, ProcessType>
{
	private static final int DEFAULT_PROGRESS = -1;
	private ReturnType asyncResult;
	private List<ProcessType> chunks;
	private AtomicInteger progress;

	SWDto()
	{
		this.progress = new AtomicInteger( DEFAULT_PROGRESS );
		this.chunks = new ArrayList<ProcessType>();
		this.asyncResult = null;
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
	 *         See {@link OnSubscribeFromSwingWorker#call(Subscriber)}, {@link OnSubscribeFromSwingWorker#setProgress(int)}
	 *         and {@link OnSubscribeFromSwingWorker#publish(Object[])}
	 */
	SWDto<ReturnType, ProcessType> setResult( ReturnType asyncResult )
	{
		synchronized ( this )
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
		synchronized ( this )
		{
			return this.asyncResult;
		}
	}

	/**
	 * To send the progress from {@link OnSubscribeFromSwingWorker} to
	 * {@link SWSubscriber}.
	 * See {@link OnSubscribeFromSwingWorker#setProgress(int)}
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
}
