package thesis_example;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/25/2017
 */
public class AsyncCodeFuture extends AsyncOperations
{
	public static void main( String[] args ) throws Exception
	{
		Callable<Integer> task = () -> computeResult();

		ExecutorService executor = Executors.newFixedThreadPool( 4 );
		Future<Integer> future = executor.submit( task );

		performOperation();
		// computeResult and performOperation run concurrently

		Integer asyncResult = future.get();// blocks until task has completed
		/*
			Some code here that uses async Result
		 */

    }

    public static void rx()
	{
		Observable.fromCallable( () -> computeResult())
				.subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.immediate())
				.doOnNext(asyncResult -> {
					asyncResult++;
					/*
						Some code here that uses async Result
					 */
				})
				.subscribe();

		performOperation();
	}
}
