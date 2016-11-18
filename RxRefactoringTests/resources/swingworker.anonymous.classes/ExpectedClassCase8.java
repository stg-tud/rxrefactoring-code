package rxrefactoring;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase8
{
	public void start()
	{
		Observable
				.fromCallable( new Callable<String>()
				{
					@Override
					public String call() throws Exception
					{
						longRunningOperation();
						return "DONE";
					}
				} ).subscribeOn( Schedulers.computation() )
				.observeOn( Schedulers.immediate() )
				.doOnNext( new Action1<String>()
				{
					@Override
					public void call( String asyncResult )
					{
						try
						{
							String result = asyncResult;
							System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
							Thread.sleep( 1000L );
						}
						catch ( Exception e )
						{
							// Catch-clause not deleted because Thread.sleep(1000L) also throws an Exception
							// not only get()
							System.err.println("Exception");
						}
					}
				} )
				.timeout( 3L, TimeUnit.SECONDS )
				.onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>()
				{
					@Override
					public Observable<? extends String> call(Throwable throwable)
					{
						// The catch block is repeated here because both exceptions (Timeout and
						// Interrupted) are handled the same
						System.err.println("Exception");
						return Observable.empty();
					}
				})
				.subscribe();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}