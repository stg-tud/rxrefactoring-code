package rxrefactoring.anonymous.simple;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase3
{
	public void start()
	{
		Observable
				.fromCallable( new Callable<String>()
				{
					@Override
					public String call() throws Exception
					{
						// code to be execute in a background thread
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
						// as in case 2, the try-catch block is no longer needed
						String result = asyncResult;
						System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Result:" + result );
					}
				} )
				.timeout( 3L, TimeUnit.SECONDS ) // equivalent to get(3L, TimeUnit.SECONDS) in done()
				.onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>()
				{
					@Override
					public Observable<? extends String> call(Throwable throwable)
					{
						// timeout in rxJava throws an error. Therefore this call must be added
						// the statements of the catch clause are copied here
						System.err.println("Exception");
						return Observable.empty();
					}
				})
				.subscribe();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 2000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}