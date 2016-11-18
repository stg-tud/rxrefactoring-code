package rxrefactoring_expected.anonymous;

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
							e.printStackTrace();
						}
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

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}
