package rxrefactoring;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase2
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
						String result = asyncResult;
						System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Result:" + result );
					}
				} )
				.subscribe();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 2000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}
