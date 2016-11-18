package rxrefactoring_expected.anonymous;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase10
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
						// The whole try-catch block was removed
						String result = null;
						result = asyncResult;
						System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Result:" + result );
					}
				} )
				.timeout( 3L, TimeUnit.SECONDS )
				.onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>()
				{
					@Override
					public Observable<? extends String> call(Throwable throwable)
					{
						// Code handling for TimeoutException copied here
						System.err.println("Several Exceptions Possible");
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
