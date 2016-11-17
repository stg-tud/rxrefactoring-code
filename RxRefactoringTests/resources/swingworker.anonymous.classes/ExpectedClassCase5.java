package rxrefactoring;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase5
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
						String anotherGet = AnonymousClassCase5.this.get( "another get method invocation" );
						System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Result:" + result );
					}
				} )
				.timeout( 3L, TimeUnit.SECONDS )
				.onErrorReturn( new Func1<Throwable, String>()
				{
					@Override
					public String call( Throwable throwable )
					{
						return null;
					}
				} )
				.subscribe();
	}

	private String get( String string )
	{
		return string.toUpperCase();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}